/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.utils.BufferedProcessInputStream;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.FrameworkUtil;

/**
 * This class will execute the actual createrepo command.
 */
public class Createrepo {

    /*
     * Default commands that every execution will have.
     */
    private static final String[] DEFAULT_ARGUMENTS = {
        CreaterepoPreferenceConstants.PREF_VERBOSE, CreaterepoPreferenceConstants.PREF_PROFILE};

    /**
     * Holds the command line switches.
     */
    private List<String> commandSwitches;

    /**
     * Initialize the command switches to be "createrepo" and
     * default command arguments.
     */
    public Createrepo() {
        commandSwitches = new ArrayList<>();
        commandSwitches.add(ICreaterepoConstants.CREATEREPO_COMMAND);
        for (String arg : DEFAULT_ARGUMENTS) {
            commandSwitches.add(ICreaterepoConstants.DASH.concat(arg));
        }
    }

    /**
     * Execute a createrepo command with custom arguments. The target directory
     * will always be the current project's content folder and will automatically be
     * added before execution. A blank list will result in the default createrepo execution.
     *
     * @param os Direct execution stream to this.
     * @param project The project.
     * @param commands A list of command switches to execute with the createrepo command.
     * @return The status of the execution.
     * @throws CoreException Occurs when error trying to execute the command.
     */
    public IStatus execute(final OutputStream os, CreaterepoProject project, List<String> commands) throws CoreException {
        IStatus available = checkIfAvailable();
        if (!available.isOK()) {
            return available;
        }
        IStatus validVersion = isCorrectVersion();
        if (!validVersion.isOK()) {
            return validVersion;
        }
        commandSwitches.addAll(commands);
        commandSwitches.add(project.getContentFolder().getLocation().toOSString());
        /* Display what the execution looks like */
        String commandString = ICreaterepoConstants.EMPTY_STRING;
        for (String arg : commandSwitches) {
            commandString = commandString.concat(arg + " "); //$NON-NLS-1$
        }
        commandString = commandString.concat("\n"); //$NON-NLS-1$
        try {
            os.write(commandString.getBytes());
            return Utils.runCommand(os, project.getProject(), commandSwitches.toArray(new String[commandSwitches.size()]));
        } catch (IOException e) {
            IStatus status = new Status(
                    IStatus.ERROR,
                    FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                    NLS.bind(Messages.Createrepo_errorExecuting, commandString), e);
            throw new CoreException(status);
        }
    }

    /**
     * Check if the createrepo command is available in the system.
     *
     * @return The status of whether or not createrepo was found.
     * @throws CoreException Occurs when createrepo is not found or executing the command failed.
     */
    public static IStatus checkIfAvailable() throws CoreException {
        try {
            BufferedProcessInputStream bpis = Utils.runCommandToInputStream("which", ICreaterepoConstants.CREATEREPO_COMMAND); //$NON-NLS-1$
            // error executing "which createrepo", most likely due to it not being found
            if (bpis.getExitValue() == 1) {
                return new Status(IStatus.ERROR,
                        FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                        Messages.Createrepo_errorCommandNotFound, null);
            }
            return Status.OK_STATUS;
        } catch (IOException e) {
            IStatus status = new Status(
                    IStatus.WARNING,
                    FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                    Messages.Createrepo_errorTryingToFindCommand, e);
            throw new CoreException(status);
        } catch (InterruptedException e) {
            IStatus status = new Status(
                    IStatus.CANCEL,
                    FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                    Messages.Createrepo_jobCancelled, e);
            throw new CoreException(status);
        }
    }

    /**
     * Grab the version of the system's createrepo command and if it
     * is >= 0.9.8 and also has a yum version >= 3.2.23, then program
     * can continue on executing.
     *
     * 0.9.8 = latest development release
     *         requires: yum version >= 3.2.23
     *
     * @return True if version is supported, false otherwise.
     * @throws CoreException Occurs when failure to get version number.
     */
    public static IStatus isCorrectVersion() throws CoreException {
        final String CREATEREPO_VALID_VERSION = "0.9.8"; //$NON-NLS-1$
        final String YUM_VALID_VERSION = "3.2.23"; //$NON-NLS-1$
        String createrepoVersion = ICreaterepoConstants.EMPTY_STRING;
        String yumVersion = ICreaterepoConstants.EMPTY_STRING;
        try {
            String repoOutput = Utils.runCommandToString(ICreaterepoConstants.CREATEREPO_COMMAND, "--version").trim(); //$NON-NLS-1$
            // createrepo --version output is like:
            // createrepo x.x.x
            String[] createrepoTemp = repoOutput.split(" "); //$NON-NLS-1$
            if (createrepoTemp.length > 1) {
                createrepoVersion = createrepoTemp[1];
            }
            boolean createrepoValid = isGreaterOrEqual(createrepoVersion.split("\\."), CREATEREPO_VALID_VERSION.split("\\.")); //$NON-NLS-1$ //$NON-NLS-2$
            // exit return an error early if the version does not meet the requirements
            if (!createrepoValid) {
                return new Status(
                        IStatus.ERROR,
                        FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                        NLS.bind(Messages.Createrepo_errorWrongVersionCreaterepo, new String[] {
                                CREATEREPO_VALID_VERSION, createrepoVersion}),
                                null);
            }
            String yumOutput = Utils.runCommandToString("yum", "--version").trim(); //$NON-NLS-1$ //$NON-NLS-2$
            // yum --version output is like:
            // x.x.x
            //        blah...
            //        blah...
            String[] yumTemp = yumOutput.split("\n"); //$NON-NLS-1$
            if (yumTemp.length > 0) {
                yumVersion = yumTemp[0];
            }
            boolean yumValid = isGreaterOrEqual(yumVersion.split("\\."), YUM_VALID_VERSION.split("\\.")); //$NON-NLS-1$ //$NON-NLS-2$
            if (yumValid) {
                // return an OK status only if yum is valid version
                return Status.OK_STATUS;
            } else {
                // return an error status otherwise stating the versions in the message
                return new Status(
                        IStatus.ERROR,
                        FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                        NLS.bind(Messages.Createrepo_errorWrongVersionYum, new String[] {
                                YUM_VALID_VERSION, yumVersion}), null);
            }
        } catch (IOException e) {
            IStatus status = new Status(
                    IStatus.CANCEL,
                    FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                    Messages.Createrepo_errorCancelled, e);
            throw new CoreException(status);
        } catch (ArrayIndexOutOfBoundsException e) {
            IStatus status = new Status(
                    IStatus.ERROR,
                    FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
                    NLS.bind(Messages.Createrepo_errorPasingVersion, new String[] {createrepoVersion, yumVersion}), e);
            throw new CoreException(status);
        }
    }

    /**
     * Go through the version and compare if the current version passed is
     * greater or equal to the minimum version required. Assumes left-most
     * value is higher priority than right-most.
     *
     * The minimum should always follow a x.x.x version format, the current can
     * be x or x.x or x.x.x
     *
     * @param current Current version to check.
     * @param minimum The minimum version to pass.
     * @return True if the current version is higher or equal to the minimum version.
     */
    private static boolean isGreaterOrEqual(String[] current, String[] minimum) {
        if (current.length > 0) {
            try {
                int iCurrent = Integer.parseInt(current[0]);
                int iMinimum = minimum.length == 0 ? 0 : Integer.parseInt(minimum[0]);
                if (iCurrent > iMinimum || (current.length == 1 && iCurrent == iMinimum)) {
                    return true;
                } else if (iCurrent == iMinimum && current.length > 1){
                    return isGreaterOrEqual(Arrays.copyOfRange(current, 1, current.length),
                            Arrays.copyOfRange(minimum, 1, minimum.length));
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

}
