/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.StringOutputStream;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.EnvironmentVariablesPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.structures.runnable.StringStreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.tools.launch.core.factory.LinuxtoolsProcessFactory;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

/**
 * Runs stap -vp1 & stap -up2 in order to get all of the probes/functions
 * that are defined in the tapsets.  Builds probeAlias and function trees
 * with the values obtained from the tapsets.
 *
 * Ugly code is a result of two issues with getting stap output.  First,
 * many tapsets do not work under stap -up2.  Second since the output
 * is not a regular language, we can't create a nice lexor/parser combination
 * to do everything nicely.
 * @author Ryan Morse
 */
public abstract class TapsetParser extends Job {

    private static AtomicBoolean displayingError = new AtomicBoolean(false);
    private static AtomicBoolean displayingCredentialDialog = new AtomicBoolean(false);

    protected TapsetParser(String jobTitle) {
        super(jobTitle);
    }

    @Override
    protected void canceling() {
        Thread thread = getThread();
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Generates a {@link Status} with the provided severity.
     */
    protected IStatus createStatus(int severity) {
        return new Status(severity, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    }

    /**
     * Runs stap with the given options and returns the output generated,
     * or <code>null</code> if the case of an error.
     * @param options String[] of any optional parameters to pass to stap
     * @param probe String containing the script to run stap on,
     * or <code>null</code> for scriptless commands
     * @param getErrors Set this to <code>true</code> if the script's error
     * stream contents should be returned instead of its standard output
     * @return The output generated from the stap run, or <code>null</code>
     * in the case of an error, or an empty string if the run was canceled.
     */
    protected String runStap(String[] options, String probe, boolean getErrors) {
        String[] args = null;
        String[] tapsets = IDEPlugin.getDefault().getPreferenceStore()
                .getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);
        boolean noTapsets = tapsets[0].trim().isEmpty();
        boolean noOptions = options[0].trim().isEmpty();
        final boolean remote = IDEPlugin.getDefault().getPreferenceStore().getBoolean(IDEPreferenceConstants.P_REMOTE_PROBES);

        int size = probe != null ? 2 : 1;
        if (!noTapsets) {
            size += tapsets.length<<1;
        }
        if (!noOptions) {
            size += options.length;
        }

        args = new String[size];
        args[0] = "stap"; //$NON-NLS-1$
        if (probe != null) {
            // Workaround for the fact that remote & local execution methods use string args differently
            args[size - 1] = !remote ? probe : '\'' + probe + '\'';
        }

        //Add extra tapset directories
        if (!noTapsets) {
            for (int i = 0; i < tapsets.length; i++) {
                args[1 + 2*i] = "-I"; //$NON-NLS-1$
                args[2 + 2*i] = tapsets[i];
            }
        }
        if (!noOptions) {
            for (int i = 0, s = noTapsets ? 1 : 1 + tapsets.length*2; i < options.length; i++) {
                args[s + i] = options[i];
            }
        }

        try {
            if (!remote) {
                return runLocalStap(args, getErrors);
            } else {
                return runRemoteStapAttempt(args, getErrors);
            }
        } catch (IOException e) {
            displayError(Messages.TapsetParser_ErrorRunningSystemtap, e.getMessage());
        } catch (InterruptedException e) {
            return ""; //$NON-NLS-1$
        }

        return null;
    }

    /**
     * Return an {@link IStatus} severity constant for the result of a call to
     * {@link TapsetParser#runStap(String[], String, boolean)}.
     * @param result The output generated by a call to {@link #runStap(String[], String, boolean)}.
     */
    protected int verifyRunResult(String result) {
        if (result == null) {
            return IStatus.ERROR;
        } else if (result.isEmpty()) {
            return IStatus.CANCEL;
        }
        return IStatus.OK;
    }

    private String runLocalStap(String[] args, boolean getErrors) throws IOException, InterruptedException {
        Process process = RuntimeProcessFactory.getFactory().exec(
                args, EnvironmentVariablesPreferencePage.getEnvironmentVariables(), null);
        if (process == null) {
            displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
            return null;
        }

        StringStreamGobbler gobbler = new StringStreamGobbler(process.getInputStream());
        StringStreamGobbler egobbler = null;
        gobbler.start();
        if (getErrors) {
            egobbler = new StringStreamGobbler(process.getErrorStream());
            egobbler.start();
        }
        process.waitFor();
        gobbler.stop();
        if (egobbler == null) {
            return gobbler.getOutput().toString();
        } else {
            egobbler.stop();
            return egobbler.getOutput().toString();
        }
    }

    private String runRemoteStapAttempt(String[] args, boolean getErrors) {
        int attemptsLeft = 3;
        while (true) {
            try {
                return runRemoteStap(args, getErrors);
            } catch (JSchException e) {
                if (!(e.getCause() instanceof ConnectException) || --attemptsLeft == 0) {
                    askIfEditCredentials();
                    return null;
                }
            }
        }
    }

    private String runRemoteStap(String[] args, boolean getErrors) throws JSchException {
        StringOutputStream str = new StringOutputStream();
        StringOutputStream strErr = new StringOutputStream();

        IPreferenceStore p = ConsoleLogPlugin.getDefault().getPreferenceStore();
        String user = p.getString(ConsoleLogPreferenceConstants.SCP_USER);
        String host = p.getString(ConsoleLogPreferenceConstants.HOST_NAME);
        String password = p.getString(ConsoleLogPreferenceConstants.SCP_PASSWORD);
        int port = p.getInt(ConsoleLogPreferenceConstants.PORT_NUMBER);

        Channel channel = LinuxtoolsProcessFactory.execRemoteAndWait(args, str, strErr, user, host, password,
                port, EnvironmentVariablesPreferencePage.getEnvironmentVariables());
        if (channel == null) {
            displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
        } else {
            channel.getSession().disconnect();
        }

        return (!getErrors ? str : strErr).toString();
    }

    private void askIfEditCredentials() {
        if (displayingCredentialDialog.compareAndSet(false, true)) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    dialog.setText(Messages.TapsetParser_RemoteCredentialErrorTitle);
                    dialog.setMessage(Messages.TapsetParser_RemoteCredentialErrorMessage);
                    if (dialog.open() == SWT.YES) {
                        String pageID = "org.eclipse.linuxtools.systemtap.prefs.consoleLog"; //$NON-NLS-1$
                        PreferencesUtil.createPreferenceDialogOn(shell, pageID, new String[]{pageID}, null).open();
                    }
                    displayingCredentialDialog.set(false);
                }
            });
        }
    }

    private void displayError(final String title, final String error) {
        if (displayingError.compareAndSet(false, true)) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    MessageDialog.openWarning(window.getShell(), title, error);
                    displayingError.set(false);
                }
            });
        }
    }

}
