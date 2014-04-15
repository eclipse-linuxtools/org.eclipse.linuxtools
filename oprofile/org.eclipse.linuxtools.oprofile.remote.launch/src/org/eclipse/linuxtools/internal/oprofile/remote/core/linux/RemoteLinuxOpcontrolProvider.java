package org.eclipse.linuxtools.internal.oprofile.remote.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.linux.LinuxOpcontrolProvider;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;

/**
 * A class which encapsulates running opcontrol.
 * @since 1.1
 */
public class RemoteLinuxOpcontrolProvider extends LinuxOpcontrolProvider {

    private static final String OPCONTROL_EXECUTABLE = "opcontrol"; //$NON-NLS-1$

    private static final int SUDO_TIMEOUT = 5000;

    public RemoteLinuxOpcontrolProvider() {
    }


    @Override
    protected Process createOpcontrolProcess(String[] cmdArray, IProject project) throws OpcontrolException {
        Process p = null;
        try {
            p = RuntimeProcessFactory.getFactory().sudoExec(cmdArray, project);
        } catch (IOException ioe) {
            throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolRun", ioe)); //$NON-NLS-1$
        }

        return p;
    }

    /**
     * Checks if the user has permissions to execute opcontrol as root without providing password
     * and if opcontrol exists in the indicated path
     * @param project
     * @return
     */
    @Override
    public boolean hasPermissions(IProject project) {
        String linuxtoolsPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);

        try {
            String opcontrolPath = null;
            if(linuxtoolsPath.isEmpty()){
                opcontrolPath = RuntimeProcessFactory.getFactory().whichCommand(OPCONTROL_EXECUTABLE, project);
            } else if(linuxtoolsPath.endsWith("/")){ //$NON-NLS-1$
                opcontrolPath = linuxtoolsPath + "opcontrol"; //$NON-NLS-1$
            } else {
                opcontrolPath = linuxtoolsPath + "/opcontrol"; //$NON-NLS-1$
            }

            if(opcontrolPath.isEmpty()){
                return false;
            }

            // Check if user has sudo permissions without password by running sudo -l.
            final Process p = RuntimeProcessFactory.getFactory().exec("sudo -l", project); //$NON-NLS-1$
            final StringBuffer buffer = new StringBuffer();

            if(p == null){
                return false;
            }

            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String s = null;
                        while ((s = input.readLine()) != null) {
                            buffer.append(s);
                            buffer.append('\n');
                        }
                        p.waitFor();
                        p.destroy();
                    } catch (InterruptedException|IOException e) {
                        e.printStackTrace();
                    }
                }
            };

             t.start();
             t.join(SUDO_TIMEOUT);

             String[] sudoLines = buffer.toString().split("\n"); //$NON-NLS-1$
             for (String s : sudoLines) {
                 if(s.contains(opcontrolPath) && s.contains("NOPASSWD")){ //$NON-NLS-1$
                        return true;
                 }
            }
             System.out.println(buffer.toString());
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
