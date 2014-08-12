/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Roland Grunberg, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.SystemTapScriptLaunch;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.EnvironmentVariablesPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.StapErrorParser;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpClient;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.RemoteScriptOptions;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import com.jcraft.jsch.JSchException;

/**
 * This <code>Action</code> is used to run a SystemTap script that is currently open in the editor.
 * Contributors:
 *    Ryan Morse - Original author.
 *    Red Hat Inc. - Copied most code from RunScriptAction here and made it into
 *                   base class for run actions.
 * @since 2.0
 */

public class RunScriptHandler extends AbstractHandler {

    /**
     * @since 2.0
     */
    private RemoteScriptOptions remoteOptions = null;
    private IEditorPart targetEditor = null;
    private String fileName = null;
    private String tmpfileName = null;
    private IPath path = null;
    private IProject project = null;
    private SystemTapScriptLaunch launch = null;
    private final List<String> cmdList = new ArrayList<>();

    /**
     * @since 2.0
     */
    public void setPath(IPath path) {
        this.path = path;
        URI uri = URIUtil.toURI(path);
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
        if (files.length > 0) {
            this.project = files[0].getProject();
        }
    }

    /**
     * @since 2.3
     */
    public void setLaunch(SystemTapScriptLaunch launch) {
        this.launch = launch;
    }

    /**
     * Adds the given String to the list of commands to be
     * passed to systemtap when running the command
     * @param option
     */
    public void addComandLineOptions(String option) {
        cmdList.add(option);
    }

    /**
     * Set the options for running the script remotely. If the script is to be run locally,
     * pass <code>null</code> as the only parameter.
     * @param remoteOptions The remote options of the script run, or <code>null</code> if the script
     * is to be run locally.
     * @since 3.0
     */
    public void setRemoteScriptOptions(RemoteScriptOptions remoteOptions) {
        this.remoteOptions = remoteOptions;
    }

    public boolean getRunLocal() {
        return remoteOptions == null;
    }

    /**
     * @since 2.1
     */
    public IProject getProject() {
        return project;
    }

    /**
     * The main body of this event. Starts by making sure the current editor is valid to run,
     * then builds the command line arguments for stap and retrieves the environment variables.
     * Finally, it gets an instance of <code>ScriptConsole</code> to run the script.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            executeAction(event);
        } catch (ExecutionException e) {
            // If the event isn't null, an error dialog must be displayed now.
            if (event != null) {
                ExceptionErrorDialog.openError(
                        Localization.getString("RunScriptHandler.Error"), //$NON-NLS-1$
                        Localization.getString("RunScriptHandler.ErrorMessage"), e); //$NON-NLS-1$
            }
            throw e;
        }
        return null;
    }

    private void executeAction(ExecutionEvent event) throws ExecutionException {
        cmdList.clear();
        final boolean local = getRunLocal();
        findTargetEditor(event);
        findFilePath();
        tryEditorSave(event);
        if (!local) {
            prepareNonLocalScript();
        }
        final String[] script = buildStandardScript();
        final String[] envVars = EnvironmentVariablesPreferencePage.getEnvironmentVariables();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                String name = getConsoleName();
                if (ScriptConsole.instanceIsRunning(name)) {
                    MessageDialog dialog = new MessageDialog(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.RunScriptHandler_AlreadyRunningDialogTitle, null,
                            MessageFormat.format(Messages.RunScriptHandler_AlreadyRunningDialogMessage, fileName),
                            MessageDialog.QUESTION, new String[]{"Yes", "No"}, 0); //$NON-NLS-1$ //$NON-NLS-2$
                    if (dialog.open() != Window.OK) {
                        if (launch != null) {
                            launch.forceRemove();
                        }
                        return;
                    }
                }
                final ScriptConsole console = ScriptConsole.getInstance(name);
                synchronized (console) {
                    if (!local) {
                        console.run(script, envVars, remoteOptions, new StapErrorParser());
                    } else {
                        console.runLocally(script, envVars, new StapErrorParser(), getProject());
                    }
                    scriptConsoleInitialized(console);
                }
            }
        });
    }

    private String getConsoleName() {
        return getRunLocal() ? fileName :
            MessageFormat.format(Messages.RunScriptHandler_NonLocalTitle,
                    fileName, remoteOptions.userName, remoteOptions.hostName);
    }

    /**
     * Once a console for running the script has been created this
     * function is called so that observers can be added for example
     * @param console
     * @since 2.0
     */
    protected void scriptConsoleInitialized(ScriptConsole console) {
        if (launch != null && path != null) {
            launch.setConsole(console);
        }
    }

    private void findTargetEditor(ExecutionEvent event) {
        if (event != null) {
            targetEditor = HandlerUtil.getActiveEditor(event);
        } else {
            for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                IWorkbenchPage activePage = window.getActivePage();
                IEditorPart edTest = activePage.getActiveEditor();
                if (edTest != null && editorMatchesPath(edTest.getEditorInput())) {
                    targetEditor = edTest;
                } else {
                    for (IEditorReference ref : activePage.getEditorReferences()) {
                        try {
                            if (editorMatchesPath(ref.getEditorInput())) {
                                targetEditor = ref.getEditor(false);
                                break;
                            }
                        } catch (PartInitException e) {
                            continue;
                        }
                    }
                }
            }
        }
    }

    private boolean editorMatchesPath(IEditorInput input) {
        return input instanceof IPathEditorInput && ((IPathEditorInput) (input)).getPath().equals(path);
    }

    /**
     * Checks whether the directory to which the given file
     * belongs is a valid directory. Currently this function just
     * checks if the given file does not belong to the tapset
     * directory.
     * @param fileName
     * @return true if the given path is valid false otherwise.
     * @since 1.2
     */
    private void findFilePath() throws ExecutionException {
        if (path != null) {
            fileName = path.toOSString();
        } else if (targetEditor == null) {
            // Cannot have neither a path nor an editor.
            throw new ExecutionException(Localization.getString("RunScriptHandler.noScriptFile"));  //$NON-NLS-1$
        } else if (targetEditor.getEditorInput() instanceof PathEditorInput) {
            fileName = ((PathEditorInput) targetEditor.getEditorInput()).getPath().toString();
        } else {
            fileName = ResourceUtil.getFile(targetEditor.getEditorInput()).getLocation().toString();
        }
    }

    /**
     * If an editor containing the file to be run is open & dirty, save it, if appropriate.
     * @param event
     */
    private void tryEditorSave(final ExecutionEvent event) {
        // No need to save if the script will already be saved with its project when launched.
        if (project != null) {
            return;
        }

        if (targetEditor != null && targetEditor.isDirty()) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    Shell shell = event != null ? HandlerUtil.getActiveShell(event)
                            : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    targetEditor.doSave(new ProgressMonitorPart(shell, new FillLayout()));
                }
            });
        }
    }

    /**
     * Attempts to set up a channel for a script that runs on a non-local host or user.
     * @return <code>true</code> on success, <code>false</code> on failure.
     * @throws ExecutionException If failure occurs during a (non-simple) launch,
     * this will throw an exception instead of returning <code>false</code>.
     */
    private void prepareNonLocalScript() throws ExecutionException {
        try {
            ScpClient scpclient = new ScpClient(remoteOptions);
            tmpfileName = new Path("/tmp").append(getFileName(fileName)).toOSString(); //$NON-NLS-1$
            scpclient.transfer(fileName, tmpfileName);
        } catch (final JSchException | IOException e) {
            String message = e instanceof JSchException
                    ? Localization.getString("RunScriptHandler.checkCredentials") //$NON-NLS-1$
                    : Localization.getString("RunScriptHandler.ioError"); //$NON-NLS-1$
            throw new ExecutionException(message, e);
        }
    }

    private String getFileName(String fileName) {
        return new File(fileName).getName();
    }

    /**
     * The command line argument generation method used by <code>RunScriptAction</code>. This generates
     * a stap command line that includes the tapsets specified in user preferences, a guru mode flag
     * if necessary, and the path to the script on disk.
     * @return The command to invoke to start the script running in stap.
     * @since 2.0
     */
    private String[] buildStandardScript() throws ExecutionException {
        getImportedTapsets();

        if (isGuru()) {
            cmdList.add("-g"); //$NON-NLS-1$
        }

        return finalizeScript();
    }

    /**
     * Adds the tapsets that the user has added in preferences to the input <code>ArrayList</code>
     * @param cmdList The list to add the user-specified tapset locations to.
     * @since 2.0
     */

    private void getImportedTapsets() {
        IPreferenceStore preferenceStore = IDEPlugin.getDefault().getPreferenceStore();
        String[] tapsets = preferenceStore.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);

        //Get all imported tapsets
        if (tapsets.length > 0 && tapsets[0].trim().length() > 0) {
            for (int i = 0; i < tapsets.length; i++) {
                cmdList.add("-I"); //$NON-NLS-1$
                cmdList.add(tapsets[i]);
            }
        }
    }

    /**
     * Checks the current script to determine if guru mode is required in order to run. This is determined
     * by the presence of embedded C.
     * @return True if the script contains embedded C code.
     */
    private boolean isGuru() throws ExecutionException {
        File f = new File(fileName);
        try (FileReader fr = new FileReader(f)) {
            int curr = 0;
            int prev = 0;
            boolean front = false;
            boolean embedded = false;
            boolean inLineComment = false;
            boolean inBlockComment = false;
            while (-1 != (curr = fr.read())) {
                if (!inLineComment && !inBlockComment && prev == '%' && curr == '{') {
                    front = true;
                } else if (!inLineComment && !inBlockComment && prev == '%' && curr == '}' && front) {
                    embedded = true;
                    break;
                } else if (!inBlockComment && ((prev == '/' && curr == '/') || curr == '#')) {
                    inLineComment = true;
                } else if (!inLineComment && prev == '/' && curr == '*') {
                    inBlockComment = true;
                } else if (curr == '\n') {
                    inLineComment = false;
                } else if (prev == '*' && curr == '/') {
                    inBlockComment = false;
                }
                prev = curr;
            }
            if (embedded) {
                return true;
            }
        } catch (FileNotFoundException fnfe) {
            throw new ExecutionException(Localization.getString("RunScriptHandler.couldNotOpenScriptFile"), fnfe); //$NON-NLS-1$
        } catch (IOException ie) {
            throw new ExecutionException(Localization.getString("RunScriptHandler.fileIOError"), ie); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Produces a <code>String[]</code> from the <code>ArrayList</code> passed in with stap inserted
     * as the first entry, and the filename as the last entry. Used to convert the arguments generated
     * earlier in <code>buildStandardScript</code> such as tapset locations and guru mode into an actual
     * command line argument array that can be passed to <code>Runtime.exec</code>.
     * @return An array suitable to pass to <code>Runtime.exec</code> to start stap on this file.
     * @since 2.0
     */
    private String[] finalizeScript() throws ExecutionException {
        // Make sure script name only contains underscores and/or alphanumeric characters.
        if (!Pattern.matches("^[a-z0-9_A-Z]+$", //$NON-NLS-1$
                getFileNameWithoutExtension(getFileName(fileName)))) {
            throw new ExecutionException(Messages.RunScriptHandler_InvalidScriptMessage);
        }

        String[] script = new String[cmdList.size() + 2];
        script[0] = "stap"; //$NON-NLS-1$
        script[script.length - 1] = !getRunLocal() ? tmpfileName : fileName;

        for (int i = 0; i < cmdList.size(); i++) {
            script[i + 1] = cmdList.get(i);
        }
        return script;
    }

    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex != -1 ? fileName.substring(0, dotIndex) : fileName;
    }

}
