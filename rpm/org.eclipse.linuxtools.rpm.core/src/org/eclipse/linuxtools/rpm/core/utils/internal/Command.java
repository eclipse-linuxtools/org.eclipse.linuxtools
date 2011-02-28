/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.utils.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.internal.Messages;

/**
 * A utility class for executing commands using @link java.lang.Runtime.exec.
 *
 */
public class Command {
	
    /**
     * Method exec.
     * This method executes a Linux command passed to it from other methods.  It executes
     * the command, reads the output from the command and passes back a status.  This method
     * is used when several output lines is expected from a command.  If one line or less is
     * expected and the developer wants the output of the command, use the getInfo method.
     * @param command - a string containing a Linux command
     * @param successCode - what the successful status value from the command should be (normally 0)
     * @return standard output from execution
     * @throws CoreException if error occurs
     */
    /****************************************************************************/
    public static String exec(String command, int successCode) throws CoreException {
        Runtime r = Runtime.getRuntime();
        Process p = null;
        int returnCode;
        String line = ""; //$NON-NLS-1$
        String line2 = ""; //$NON-NLS-1$
        // prepare buffers for process output and error streams
        StringBuffer err = new StringBuffer();
        StringBuffer out = new StringBuffer();

        try {
			p = r.exec((String) command);
            // create thread for reading inputStream (process' stdout)
            StreamReaderThread outThread = new StreamReaderThread(p
                    .getInputStream(), out);
            // create thread for reading errorStream (process' stderr)
            StreamReaderThread errThread = new StreamReaderThread(p
                    .getErrorStream(), err);
            // start both threads
            outThread.start();
            errThread.start();

            //wait for process to end
			returnCode = p.waitFor();
            //finish reading whatever's left in the buffers
            outThread.join();
            errThread.join();
			
			if(returnCode != successCode) {
				throw new Exception();
			}
        } catch (Exception e) {
            String throw_message = Messages
                    .getString("RPMCore.Error_executing__97") + command + //$NON-NLS-1$
                    Messages.getString("LinuxShellCmds.7") + err.toString(); //$NON-NLS-1$
            IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1, throw_message,
                    null);
            throw new CoreException(error);
        }
        return out.toString();
    }
}
