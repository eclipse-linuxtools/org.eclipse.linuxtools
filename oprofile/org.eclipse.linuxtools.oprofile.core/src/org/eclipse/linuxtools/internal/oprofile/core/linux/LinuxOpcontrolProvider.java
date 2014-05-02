/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.properties.LinuxtoolsPathProperty;

/**
 * A class which encapsulates running opcontrol.
 */
public class LinuxOpcontrolProvider implements IOpcontrolProvider {
    private static final String OPCONTROL_EXECUTABLE = "opcontrol";

    private static final int SUDO_TIMEOUT = 2000;

    /**
     *  Location of opcontrol security wrapper
     */
    private static final String OPCONTROL_REL_PATH = "natives/linux/scripts/" + OPCONTROL_EXECUTABLE; //$NON-NLS-1$

    private static boolean isInstalled;

    /**
     *  Initialize the Oprofile kernel module and oprofilefs
     */
    private static final String OPD_INIT_MODULE = "--init"; //$NON-NLS-1$

    // Setup daemon collection arguments
    private static final String OPD_SETUP = "--setup"; //$NON-NLS-1$
    private static final String OPD_HELP = "--help"; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE = "--separate="; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_SEPARATOR = ","; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_NONE = "none"; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_LIBRARY = "library"; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_KERNEL = "kernel"; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_THREAD = "thread"; //$NON-NLS-1$
    private static final String OPD_SETUP_SEPARATE_CPU = "cpu"; //$NON-NLS-1$

    private static final String OPD_SETUP_EVENT = "--event="; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_TRUE = "1"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_FALSE = "0"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_DEFAULT = "default"; //$NON-NLS-1$

    private static final String OPD_SETUP_IMAGE = "--image="; //$NON-NLS-1$

    private static final String OPD_CALLGRAPH_DEPTH = "--callgraph="; //$NON-NLS-1$

    // Kernel image file options
    private static final String OPD_KERNEL_NONE = "--no-vmlinux"; //$NON-NLS-1$
    private static final String OPD_KERNEL_FILE = "--vmlinux="; //$NON-NLS-1$

    // Start the daemon process without starting data collection
    private static final String OPD_START_DAEMON = "--start-daemon"; //$NON-NLS-1$

    // Start collecting profiling data
    private static final String OPD_START_COLLECTION = "--start"; //$NON-NLS-1$

    // Flush the collected profiling data to disk
    private static final String OPD_DUMP = "--dump"; //$NON-NLS-1$

    // Stop data collection
    private static final String OPD_STOP_COLLECTION = "--stop"; //$NON-NLS-1$

    // Stop data collection and stop daemon
    private static final String OPD_SHUTDOWN = "--shutdown"; //$NON-NLS-1$

    // Clear out data from current session
    private static final String OPD_RESET = "--reset"; //$NON-NLS-1$

    // Unload the oprofile kernel module and oprofilefs
    private static final String OPD_DEINIT_MODULE = "--deinit"; //$NON-NLS-1$

    // Logging verbosity. Specified with setupDaemon.
    //--verbosity=all generates WAY too much stuff in the log
    private String verbosity = ""; //$NON-NLS-1$


    public LinuxOpcontrolProvider() {
    }

    /**
     * Unload the kernel module and oprofilefs
     * @throws OpcontrolException
     */
    @Override
    public void deinitModule() throws OpcontrolException {
        runOpcontrol(OPD_DEINIT_MODULE);
    }

    /**
     * Dump collected profiling data
     * @throws OpcontrolException
     */
    @Override
    public void dumpSamples() throws OpcontrolException {
        runOpcontrol(OPD_DUMP);
    }

    /**
     * Loads the kernel module and oprofilefs
     * @throws OpcontrolException
     */
    @Override
    public void initModule() throws OpcontrolException {
        runOpcontrol(OPD_INIT_MODULE);
    }

    /**
     * Clears out data from current session
     * @throws OpcontrolException
     */
    @Override
    public void reset() throws OpcontrolException {
        runOpcontrol(OPD_RESET);
    }

    /**
     * Saves the current ("default") session
     * @param name    the name to which to save the session
     * @throws OpcontrolException
     */
    @Override
    public void saveSession(String name) throws OpcontrolException {
        SessionManager sessMan;
        try {
            sessMan = new SessionManager(SessionManager.SESSION_LOCATION);
            for (String event : sessMan.getSessionEvents(SessionManager.CURRENT)){
                sessMan.addSession(name, event);
                String oldFile = SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + event + SessionManager.CURRENT;
                String newFile = SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + event + name;
                Process p = Runtime.getRuntime().exec("cp " + oldFile + " " + newFile);
                p.waitFor();
            }
            sessMan.write();
        } catch (FileNotFoundException e) {
            //intentionally blank
            //during a save, the session file will exist
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the session with the specified name for the specified event
     * @param sessionName The name of the session to delete
     * @param eventName The name of the event containing the session
     * @throws OpcontrolException
     */
    @Override
    public void deleteSession (String sessionName, String eventName) throws OpcontrolException {
        File file = new File (SessionManager.OPXML_PREFIX + SessionManager.MODEL_DATA + eventName + sessionName);
        file.delete();
        SessionManager sessMan = new SessionManager(SessionManager.SESSION_LOCATION);
        sessMan.removeSession(sessionName, eventName);
        sessMan.write();
    }

    /**
     * Give setup aruments
     * @param args    list of parameters for daemon
     * @throws OpcontrolException
     */
    @Override
    public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException {
        // Convert options & events to arguments for opcontrol
        ArrayList<String> args = new ArrayList<>();
        args.add(OPD_SETUP);
        optionsToArguments(args, options);
        if (!Oprofile.getTimerMode()) {
            if (events == null || events.length == 0) {
                args.add(OPD_SETUP_EVENT + OPD_SETUP_EVENT_DEFAULT);
            } else {
                for (int i = 0; i < events.length; ++i) {
                    eventToArguments(args, events[i]);
                }
            }
        }
        runOpcontrol(args);
    }

    /**
     * Stop data collection and remove daemon
     * @throws OpcontrolException
     */
    @Override
    public void shutdownDaemon() throws OpcontrolException {
        runOpcontrol(OPD_SHUTDOWN);
    }

    /**
     * Start data collection (will start daemon if necessary)
     * @throws OpcontrolException
     */
    @Override
    public void startCollection() throws OpcontrolException {
        runOpcontrol(OPD_START_COLLECTION);
    }

    /**
     * Start daemon without starting profiling
     * @throws OpcontrolException
     */
    @Override
    public void startDaemon() throws OpcontrolException {
        runOpcontrol(OPD_START_DAEMON);
    }

    /**
     * Stop data collection
     * @throws OpcontrolException
     */
    @Override
    public void stopCollection() throws OpcontrolException {
        runOpcontrol(OPD_STOP_COLLECTION);
    }

    /**
     * Check status. returns true if any status was returned
     * @throws OpcontrolException
     */
    @Override
    public boolean status() throws OpcontrolException {
        return runOpcontrol(OPD_HELP);
    }

    // Convenience function
    /**
     * Run opcontrol process
     * @param cmd opcontrol daemon argument
     * @return true if any output was produced on the error stream, false otherwise
     * @throws OpcontrolException
     */
    private boolean runOpcontrol(String cmd) throws OpcontrolException {
        ArrayList<String> list = new ArrayList<>();
        list.add(cmd);
        return runOpcontrol(list);
    }

    // Will add opcontrol program to beginning of args
    // args: list of opcontrol arguments (not including opcontrol program itself)
    /**
     * Run opcontrol process
     * @param args list of opcontrol command and arguments
     * @return true if any output was produced on the error stream. Unfortunately
     * this appears to currently be the only way we can tell if user correctly
     * entered the password
     */
    private boolean runOpcontrol(ArrayList<String> args) throws OpcontrolException {
        IProject project = Oprofile.OprofileProject.getProject();


        args.add(0, findOpcontrolExecutable());

        // Verbosity hack. If --start or --start-daemon, add verbosity, if set
        String cmd = args.get(1);
        if (verbosity.length() > 0 && (cmd.equals (OPD_START_COLLECTION) || cmd.equals(OPD_START_DAEMON))) {
            args.add(verbosity);
        }

        String[] cmdArray = new String[args.size()];
        args.toArray(cmdArray);

        // Print what is passed on to opcontrol
        if (OprofileCorePlugin.isDebugMode()) {
            printOpcontrolCmd(cmdArray);
        }

        Process p = createOpcontrolProcess(cmdArray, project);
        return checkOpcontrolProcess(p);

    }

    /**
     * Create opcontrol process
     * @param cmdArray array of opcontrol command and arguments
     * @param project project to be profiled
     * @return p opcontrol process
     * @throws OpcontrolException
     * @since 1.1
     */
    protected Process createOpcontrolProcess(String[] cmdArray, IProject project) throws OpcontrolException {
        Process p = null;
        try {

            if (!LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
                p = RuntimeProcessFactory.getFactory().sudoExec(cmdArray, project);
            } else if (isInstalled){
                p = Runtime.getRuntime().exec(cmdArray);
            } else{
                throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolProvider", null)); //$NON-NLS-1$
            }

        } catch (IOException ioe) {
            throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolRun", ioe)); //$NON-NLS-1$
        }

        return p;
    }

    /**
     * Check opcontrol process
     * @param p opcontrol process
     * @return true if any output was produced on the error stream, false otherwise
     * @throws OpcontrolException
     * @since 1.1
     */
    private boolean checkOpcontrolProcess(Process p) throws OpcontrolException {
        if (p != null) {
            String errOutput = ""; //$NON-NLS-1$
            String output = "", s; //$NON-NLS-1$
            try (BufferedReader errout = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));
                    BufferedReader stdout = new BufferedReader(
                            new InputStreamReader(p.getInputStream()))) {
                while ((s = errout.readLine()) != null) {
                    errOutput += s + "\n"; //$NON-NLS-1$
                }
                // Unfortunately, when piped through consolehelper stderr output
                // is redirected to stdout. Need to read stdout and do some
                // string matching in order to give some better advice as to how
                // to
                // alleviate the nmi_watchdog problem. See RH BZ #694631
                while ((s = stdout.readLine()) != null) {
                    output += s + "\n"; //$NON-NLS-1$
                }
                stdout.close();
                errout.close();

                int ret = p.waitFor();
                if (ret != 0) {
                    OpControlErrorHandler errHandler = OpControlErrorHandler
                            .getInstance();
                    OpcontrolException ex = errHandler.handleError(output,
                            errOutput);
                    throw ex;
                }

                if (errOutput.length() != 0) {
                    return true;
                }

            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Print to stdout what is passed on to opcontrol.
     *
     * @param cmdArray
     */
    private void printOpcontrolCmd(String[] cmdArray) {
        StringBuffer buf = new StringBuffer();
        for (String token: cmdArray) {
            buf.append(token);
            buf.append(" ");
        }
        System.out.println(OprofileCorePlugin.DEBUG_PRINT_PREFIX + buf.toString());
    }

    /**
     * Search for opcontrol executable on the system
     * @return a string path to opcontrol executable
     * @since 1.1
     */
    private String findOpcontrolExecutable() {
        IProject project = Oprofile.OprofileProject.getProject();
        if (!LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project).equals("")){
            return OPCONTROL_EXECUTABLE;
        }

        URL url = FileLocator.find(Platform.getBundle(OprofileCorePlugin
                .getId()), new Path(OPCONTROL_REL_PATH), null);

        if (url != null) {
            try {
                isInstalled = true;
                return FileLocator.toFileURL(url).getPath();
            } catch (IOException ignore) {
            }
        } else {
            isInstalled = false;
            return OPCONTROL_EXECUTABLE;
        }

        return null;
    }

    /**
     * Convert the event into arguments for opcontrol
     * @param args List of arguments
     * @param event The event to be passed as argument to opcontrol
     */
    private void eventToArguments(ArrayList<String> args, OprofileDaemonEvent event) {
        // Event spec: "EVENT:count:mask:profileKernel:profileUser"
        StringBuilder spec = new StringBuilder();
        spec.append(OPD_SETUP_EVENT);
        spec.append(event.getEvent().getText());
        spec.append(OPD_SETUP_EVENT_SEPARATOR);
        spec.append(event.getResetCount());
        spec.append(OPD_SETUP_EVENT_SEPARATOR);
        spec.append(event.getEvent().getUnitMask().getMaskValue());
        spec.append(OPD_SETUP_EVENT_SEPARATOR);
        spec.append((event.getProfileKernel() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
        spec.append(OPD_SETUP_EVENT_SEPARATOR);
        spec.append((event.getProfileUser() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
        args.add(spec.toString());
    }

    /**
     *  Convert the options into arguments for opcontrol
     * @param args List of arguments
     * @param options The launch options to oprofile daemon
     */
    private void optionsToArguments(ArrayList<String> args, OprofileDaemonOptions options) {
        // Add separate flags
        int mask = options.getSeparateProfilesMask();

        StringBuilder separate = new StringBuilder();
        separate.append(OPD_SETUP_SEPARATE);

        if (mask == OprofileDaemonOptions.SEPARATE_NONE) {
            separate.append(OPD_SETUP_SEPARATE_NONE);
        } else {
            //note that opcontrol will nicely ignore the trailing comma
            if ((mask & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
                separate.append(OPD_SETUP_SEPARATE_LIBRARY + OPD_SETUP_SEPARATE_SEPARATOR);
            if ((mask & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
                separate.append(OPD_SETUP_SEPARATE_KERNEL + OPD_SETUP_SEPARATE_SEPARATOR);
            if ((mask & OprofileDaemonOptions.SEPARATE_THREAD) != 0)
                separate.append(OPD_SETUP_SEPARATE_THREAD + OPD_SETUP_SEPARATE_SEPARATOR);
            if ((mask & OprofileDaemonOptions.SEPARATE_CPU) != 0)
                separate.append(OPD_SETUP_SEPARATE_CPU + OPD_SETUP_SEPARATE_SEPARATOR);
        }
        args.add(separate.toString());

        // Add kernel image
        if (options.getKernelImageFile() == null || options.getKernelImageFile().length() == 0) {
            args.add(OPD_KERNEL_NONE);
        } else {
            args.add(OPD_KERNEL_FILE + options.getKernelImageFile());
        }

        //image filter -- always non-null
        args.add(OPD_SETUP_IMAGE + options.getBinaryImage());

        //callgraph depth
        args.add(OPD_CALLGRAPH_DEPTH + options.getCallgraphDepth());
    }


    /**
     * Checks if the user has permissions to execute opcontrol as root without providing password
     * and if opcontrol exists in the indicated path
     * @param project The project to be profiled
     * @return true if the user has sudo permission to run opcontrol, false otherwise
     * @throws OpcontrolException if opcontrol not installed
     * @since 1.1
     */
    @Override
    public boolean hasPermissions(IProject project) throws OpcontrolException {
        String linuxtoolsPath = LinuxtoolsPathProperty.getInstance().getLinuxtoolsPath(project);

        try {
            String opcontrolPath = null;
            if(linuxtoolsPath.equals("")){
                if(!isInstalled()){
                    throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolProvider", null));
                } else{
                    return true;
                }

            } else if(linuxtoolsPath.endsWith("/")){
                opcontrolPath = linuxtoolsPath + "opcontrol";
            } else {
                opcontrolPath = linuxtoolsPath + "/opcontrol";
            }

            // Check if user has sudo permissions without password by running sudo -l.
            final Process p = RuntimeProcessFactory.getFactory().exec("sudo -l", project);
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

             t.start();
             t.join(SUDO_TIMEOUT);

             String[] sudoLines = buffer.toString().split("\n");
             for (String s : sudoLines) {
                 if(s.contains(opcontrolPath) && s.contains("NOPASSWD")){
                        return true;
                 }
            }
             System.out.println(buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if opcontrol is installed on the system
     * @return true if opcontrol is installed, otherwise false
     */
    private boolean isInstalled(){
        findOpcontrolExecutable();
        return isInstalled;
    }
}