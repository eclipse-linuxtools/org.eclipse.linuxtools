/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Roland Grunberg, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.SystemTapScriptLaunch;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.EnvironmentVariablesPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.StapErrorParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
	protected boolean continueRun = true;
	private RemoteScriptOptions remoteOptions = null;
	private IEditorPart ed = null;
	private String fileName = null;
	private String tmpfileName = null;
	private String serverfileName = null;
	private IPath path;
	private IProject project;
	private SystemTapScriptLaunch launch;
	private final List<String> cmdList;


	public RunScriptHandler(){
		this.cmdList = new ArrayList<>();
	}

	/**
	 * @since 2.0
	 */
	public void setPath(IPath path){
		this.path = path;
		URI uri = URIUtil.toURI(path);
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
		if (files.length > 0) {
			this.project = files[0].getProject();
		}
	}

	/**
	 * @since 2.1
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @since 2.3
	 */
	public void setLaunch(SystemTapScriptLaunch launch){
		this.launch = launch;
	}

	/**
	 * Finds the editor containing the target script to run, so the script can be saved
	 * when it is run, if appropriate.
	 * The script is saved when it is run with the "simple" run button on the toolbar (path == null),
	 * or if the script is outside of a project (working with a PathEditorInput).
	 */
	private void findTargetEditor() {
		ed = null;

		if (path == null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ed = window.getActivePage().getActiveEditor();
			return;
		}

		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			IEditorPart edTest = window.getActivePage().getActiveEditor();
			if (edTest != null && matchesEditor(edTest.getEditorInput(), edTest)) {
				return;
			}
			for (IEditorReference ref : window.getActivePage().getEditorReferences()) {
				try {
					if (matchesEditor(ref.getEditorInput(), ref.getEditor(false))) {
						return;
					}
				} catch (PartInitException e) {
					continue;
				}
			}
		}
	}

	private boolean matchesEditor(IEditorInput input, IEditorPart editor) {
		if (input instanceof IPathEditorInput && ((IPathEditorInput) (input)).getPath().equals(this.path)) {
			// Only save the editor when working with a file without a project (PathEditorInput),
			// otherwise the editor isn't needed at all (saving is handled elsewhere in that case).
			if (input instanceof PathEditorInput) {
				this.ed = editor;
			}
			return true;
		}
		return false;
	}

	/**
	 * The main body of this event. Starts by making sure the current editor is valid to run,
	 * then builds the command line arguments for stap and retrieves the environment variables.
	 * Finally, it gets an instance of <code>ScriptConsole</code> to run the script.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		findTargetEditor();
		final boolean local = getRunLocal();
		if(isValid()) {
			if(!local && !prepareNonLocalScript()) {
				return null;
			}
			final String[] script = buildStandardScript();
			final String[] envVars = getEnvironmentVariables();
			if (continueRun) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						String name = !local ? serverfileName : fileName;
						if (ScriptConsole.instanceIsRunning(name)) {
							MessageDialog dialog = new MessageDialog(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									Messages.RunScriptAction_alreadyRunningDialogTitle, null,
									MessageFormat.format(Messages.RunScriptAction_alreadyRunningDialogMessage, fileName),
									MessageDialog.QUESTION, new String[]{"Yes", "No"}, 0); //$NON-NLS-1$ //$NON-NLS-2$
							if (dialog.open() != Window.OK) {
								if (launch != null) {
									launch.forceRemove();
								}
								return;
							}
						}
						final ScriptConsole console;
						if (!local) {
							console = ScriptConsole.getInstance(serverfileName);
						} else {
							console = ScriptConsole.getInstance(fileName);
						}
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
		}

		return null;
	}

	/**
	 * Attempts to set up a channel for a script that runs on a non-local host or user.
	 * @return <code>true</code> on success, <code>false</code> on failure.
	 * @throws ExecutionException If failure occurs during a (non-simple) launch,
	 * this will throw an exception instead of returning <code>false</code>.
	 */
	private boolean prepareNonLocalScript() throws ExecutionException {
		try {
			ScpClient scpclient = new ScpClient(remoteOptions);
			serverfileName = fileName.substring(fileName.lastIndexOf('/')+1);
			tmpfileName="/tmp/"+ serverfileName; //$NON-NLS-1$
			scpclient.transfer(fileName,tmpfileName);
		} catch (final JSchException | IOException e) {
			final String message = e instanceof JSchException ? Localization.getString("RunScriptHandler.checkCredentials") //$NON-NLS-1$
					: Localization.getString("RunScriptHandler.ioError"); //$NON-NLS-1$
			if (launch == null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (e instanceof JSchException) {
							ErrorDialog.openError(PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
									Localization.getString("RunScriptHandler.serverError"), Localization.getString("RunScriptHandler.serverError"), //$NON-NLS-1$ //$NON-NLS-2$
									new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID, message));
						} else {
							ExceptionErrorDialog.openError(Localization.getString("RunScriptHandler.ioError"), e); //$NON-NLS-1$
						}
					}
				});
				return false;
			} else {
				throw new ExecutionException(message, e);
			}
		}
		return true;
	}

	/**
	 * Once a console for running the script has been created this
	 * function is called so that observers can be added for example
	 * @param console
	 * @since 2.0
	 */
	protected void scriptConsoleInitialized(ScriptConsole console){
		if (launch != null && path != null) {
			launch.setConsole(console);
		}
	}

	/**
	 * Returns the path that was set for this action. If one was not set it
	 * returns the path of the current editor in the window this action is
	 * associated with.
	 *
	 * @return The string representation of the path of the script to run.
	 */
	protected String getFilePath() {
		if (path != null){
			return path.toOSString();
		}
		if (ed == null) {
			return ""; //$NON-NLS-1$
		}
		if(ed.getEditorInput() instanceof PathEditorInput){
			return ((PathEditorInput)ed.getEditorInput()).getPath().toString();
		} else {
			return ResourceUtil.getFile(ed.getEditorInput()).getLocation().toString();
		}
	}

	/**
	 * Checks if the current editor is operating on a file that actually exists and can be
	 * used as an argument to stap (as opposed to an unsaved buffer).
	 * @return True if the file is valid.
	 */
	private boolean isValid() {
		// If the path is not set this action will run the script from
		// the active editor
		if(!tryEditorSave()){
			if (this.path == null){
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						String msg = MessageFormat.format(Localization.getString("RunScriptAction.NoScriptFile"),(Object[]) null); //$NON-NLS-1$
						MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("RunScriptAction.Problem"), msg); //$NON-NLS-1$
					}
				});
				return false;
			}
		}
		String filePath = this.getFilePath();
		return filePath.endsWith(".stp") //$NON-NLS-1$
				&& isValidDirectory(filePath);
	}

	private boolean tryEditorSave() {
		if(null == ed) {
			return false;
		}

		if(ed.isDirty()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					ed.doSave(new ProgressMonitorPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new FillLayout()));
				}
			});
		}

		return true;
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
	private boolean isValidDirectory(String fileName) {
		this.fileName = fileName;
		if(0 == IDESessionSettings.tapsetLocation.trim().length()){
			TapsetLibrary.getTapsetLocation(IDEPlugin.getDefault().getPreferenceStore());
		}

		if(fileName.contains(IDESessionSettings.tapsetLocation)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					String msg = MessageFormat.format(Localization.getString("RunScriptAction.TapsetDirectoryRun"),(Object []) null); //$NON-NLS-1$
					MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("RunScriptAction.Error"), msg); //$NON-NLS-1$
				}
			});
			return false;
		}
		return true;
	}

	/**
	 * Adds the given String to the list of commands to be
	 * passed to systemtap when running the command
	 * @param option
	 */
	public void addComandLineOptions(String option){
		this.cmdList.add(option);
	}

	/**
	 * The command line argument generation method used by <code>RunScriptAction</code>. This generates
	 * a stap command line that includes the tapsets specified in user preferences, a guru mode flag
	 * if necessary, and the path to the script on disk.
	 * @return The command to invoke to start the script running in stap.
	 * @since 2.0
	 */
	protected String[] buildStandardScript() {
		getImportedTapsets(cmdList);

		if(isGuru()) {
			cmdList.add("-g"); //$NON-NLS-1$
		}

		return finalizeScript(cmdList);
	}

	/**
	 * Adds the tapsets that the user has added in preferences to the input <code>ArrayList</code>
	 * @param cmdList The list to add the user-specified tapset locations to.
	 * @since 2.0
	 */

	protected void getImportedTapsets(List<String> cmdList) {
		IPreferenceStore preferenceStore = IDEPlugin.getDefault().getPreferenceStore();
		String[] tapsets = preferenceStore.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);

		//Get all imported tapsets
		if(null != tapsets && tapsets.length > 0 && tapsets[0].trim().length() > 0) {
			for(int i=0; i<tapsets.length; i++) {
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
	private boolean isGuru() {
		File f = new File(fileName);
		try (FileReader fr = new FileReader(f)){
			int curr = 0;
			int prev = 0;
			boolean front = false;
			boolean imbedded = false;
			boolean inLineComment = false;
			boolean inBlockComment = false;
			while(-1 != (curr = fr.read())) {
				if(!inLineComment && !inBlockComment && '%' == prev && '{' == curr) {
					front = true;
				} else if(!inLineComment && !inBlockComment && '%' == prev && '}' == curr && front) {
					imbedded = true;
					break;
				} else if(!inBlockComment && (('/' == prev && '/' == curr) || '#' == curr)) {
					inLineComment = true;
				} else if(!inLineComment && '/' == prev && '*' == curr) {
					inBlockComment = true;
				} else if('\n' == curr) {
					inLineComment = false;
				} else if('*' == prev && '/' == curr) {
					inBlockComment = false;
				}
				prev = curr;
			}
			fr.close();
			if(imbedded) {
				return true;
			}
		} catch (FileNotFoundException fnfe) {
			ExceptionErrorDialog.openError(Localization.getString("RunScriptHandler.couldNotOpenScriptFile"), fnfe); //$NON-NLS-1$
		} catch (IOException ie) {
			ExceptionErrorDialog.openError(Localization.getString("RunScriptHandler.fileIOError"), ie); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Produces a <code>String[]</code> from the <code>ArrayList</code> passed in with stap inserted
	 * as the first entry, and the filename as the last entry. Used to convert the arguments generated
	 * earlier in <code>buildStandardScript</code> such as tapset locations and guru mode into an actual
	 * command line argument array that can be passed to <code>Runtime.exec</code>.
	 * @param cmdList The list of arguments for stap for this script
	 * @return An array suitable to pass to <code>Runtime.exec</code> to start stap on this file.
	 * @since 2.0
	 */
	protected String[] finalizeScript(List<String> cmdList) {

		String[] script;

		script = new String[cmdList.size() + 4];
		script[0] = "stap"; //$NON-NLS-1$

		if(getRunLocal() == false) {
			script[script.length-1] = tmpfileName;
		} else {
			script[script.length-1] = fileName;
		}

		for(int i=0; i< cmdList.size(); i++) {
			script[i+1] = cmdList.get(i);
		}
		script[script.length-3]="-m"; //$NON-NLS-1$

		String modname;
		if(getRunLocal() == false) {
			modname = serverfileName.substring(0, serverfileName.lastIndexOf(".stp")); //$NON-NLS-1$
		}
		/* We need to remove the directory prefix here because in the case of
		 * running the script remotely, this is already done.  Not doing so
		 * causes a modname error.
		 */
		else {
			modname = fileName.substring(fileName.lastIndexOf('/')+1);
			modname = modname.substring(0, modname.lastIndexOf(".stp")); //$NON-NLS-1$
		}

		// Make sure script name only contains underscores and/or alphanumeric characters.
		Pattern validModName = Pattern.compile("^[a-z0-9_A-Z]+$"); //$NON-NLS-1$
		Matcher modNameMatch = validModName.matcher(modname);
		if (!modNameMatch.matches()) {
			continueRun = false;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {

					Shell parent = PlatformUI.getWorkbench().getDisplay()
							.getActiveShell();
					MessageDialog.openError(parent,
							Messages.ScriptRunAction_InvalidScriptTitle,
							Messages.ScriptRunAction_InvalidScriptTMessage);
				}
			});
			return new String[0];
		}

		script[script.length-2]=modname;
		return script;
	}

	private String[] getEnvironmentVariables() {
		return EnvironmentVariablesPreferencePage.getEnvironmentVariables();
	}

	@Override
	public boolean isEnabled() {
		return (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof STPEditor);
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

	private boolean getRunLocal() {
		return remoteOptions == null;
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
