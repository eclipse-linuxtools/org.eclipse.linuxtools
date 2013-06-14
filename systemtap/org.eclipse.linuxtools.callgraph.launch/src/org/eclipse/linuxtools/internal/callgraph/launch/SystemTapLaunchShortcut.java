/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Launch method for a generated script that executes on a binary <br>
 * MUST specify (String) scriptPath and call config = createConfiguration(bin)! <br>
 * Noteworthy defaults:
 * <ul>
 * <li>'name' defaults to ""</li>
 * <li>'overwrite' defaults to true</li>
 * </ul>
 * To create new launches: <br>
 * <ul>
 * <li>
 * Extend the shortcut extension in Eclipse (recommend copying code from the
 * existing launch in the plugin.xml)</li>
 * <li>
 * Create a class that extends SystemTapLaunchShortcut with a function public
 * void launch(IBinary bin, String mode) that calls super.Init().</li>
 * <li>
 * Set name</li>
 * <li>
 * If the script is to be launched with a binary, call binName = getName(bin)</li>
 * <li>
 * Call config=createConfiguration(bin, name) or
 * config=createConfiguration(name)</li>
 * <li>
 * Specify whichever of the optional parameters you need</li>
 * <li>
 * Extend the parser extension defined in org.eclipse.linuxtools.callgraph
 * .core. Build a basic parsing class.</li>
 * <li>
 * Set parserID to the id of your extension</li>
 * <li>
 * Set scriptPath</li>
 * <li>
 * If you wish to generate a script on-the-fly, override generateScript() and
 * set needToGenerate to true</li>
 * <li>
 * Call finishLaunch or finishLaunchWithoutBinary</li>
 * </ul>
 *<br>
 *<br>
	 * The following protected parameters are provided by
	 * SystemTapLaunchShortcut:
	 *<br> <br>
	 * Optional customization parameters: <code> protected String name; protected
	 * String binaryPath; protected String arguments; protected String
	 * outputPath; protected String dirPath; protected String generatedScript;
	 * protected boolean needToGenerate; protected boolean overwrite;</code>
	 * <br> <br>
	 * Mandatory: <code> protected String scriptPath; protected ILaunchConfiguration
	 * config;</code>

 */
public abstract class SystemTapLaunchShortcut extends ProfileLaunchShortcut {
	protected IEditorPart editor;
	protected ILaunchConfiguration config;

	private static final String USER_SELECTED_ALL = "ALL"; //$NON-NLS-1$
	private static final String MAIN_FUNC_NAME = "main"; //$NON-NLS-1$

	protected String name;
	protected String binaryPath;
	protected String scriptPath;
	protected String arguments;
	protected String outputPath;
	protected String binName;
	protected String dirPath;
	protected String generatedScript;
	protected String parserID;
	protected String viewID;
	protected boolean needToGenerate;
	protected boolean overwrite;
	protected boolean useColours;
	protected String resourceToSearchFor;
	protected boolean searchForResource;
	protected IBinary bin;

	private Button OKButton;
	private boolean testMode = false;
	protected String secondaryID = ""; //$NON-NLS-1$

	/**
	 * Initialize variables. Highly recommend calling this function within the
	 * launch methods. Will call setScriptPath, setParserID and setViewID.
	 */
	public void initialize() {
		name = ""; //$NON-NLS-1$
		dirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		binaryPath = LaunchConfigurationConstants.DEFAULT_BINARY_PATH;
		arguments = LaunchConfigurationConstants.DEFAULT_ARGUMENTS;
		outputPath = PluginConstants.getDefaultIOPath();
		overwrite = true;
		generatedScript = LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT;
		needToGenerate = false;
		useColours = false;
		secondaryID = ""; //$NON-NLS-1$
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				PluginConstants.CONFIGURATION_TYPE_ID);
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) {
		SystemTapOptionsTab tab = new SystemTapOptionsTab();
		tab.setDefaults(wc);
	}

	protected boolean existsConfiguration(ILaunchConfigurationWorkingCopy wc) {
		ILaunchConfigurationType configType = getLaunchConfigType();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);

			for (int i = 0; i < configs.length; i++) {
				if (configs[i] != null && configs[i].exists()
						&& checkIfAttributesAreEqual(wc, configs[i])) {
					config = configs[i];
					return true;
				}
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Returns true if two configurations are exactly identical (i.e. all
	 * attributes are equal)
	 *
	 * @param first
	 * @param second
	 * @return True if two configurations are exactly identical (i.e. all
	 *         attributes are equal)
	 */
	private boolean checkIfAttributesAreEqual(ILaunchConfiguration first,
			ILaunchConfiguration second) {

		try {
			if (first.getAttributes().equals(second.getAttributes())) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Helper function to complete launches. Uses protected parameters (Strings)
	 * scriptPath, binaryPath, arguments, outputPath and (boolean) overwrite.
	 * These must be set by the calling function (or else nonsensical results
	 * will occur). <br>
	 * If scriptPath has not been set, the setScriptPath() method will be
	 * called.
	 *
	 * @param name : Used to generate the name of the new configuration
	 * @param bin : Affiliated executable
	 * @param mode : Mode setting
	 * @param wc : A working copy of the launch configuration
	 * @throws IOException
	 */
	protected void finishLaunch(String name, String mode, ILaunchConfigurationWorkingCopy wc) throws IOException  {
		if (!finishLaunchHelper()) {
			return;
		}

		if (wc != null) {

			wc.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH,scriptPath);

			if (!invalid(binaryPath)) {
				wc.setAttribute(LaunchConfigurationConstants.BINARY_PATH,binaryPath);
			}

			wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH,outputPath);
			wc.setAttribute(LaunchConfigurationConstants.ARGUMENTS, arguments);
			wc.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT,generatedScript);
			wc.setAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE,needToGenerate);
			wc.setAttribute(LaunchConfigurationConstants.OVERWRITE, overwrite);
			wc.setAttribute(LaunchConfigurationConstants.USE_COLOUR,useColours);
			wc.setAttribute(LaunchConfigurationConstants.PARSER_CLASS,parserID);
			wc.setAttribute(LaunchConfigurationConstants.VIEW_CLASS, viewID);
			wc.setAttribute(LaunchConfigurationConstants.SECONDARY_VIEW_ID, setSecondaryViewID());


			/**
			 * Enable this to save the default launch configuration
			 */
			/*try {
				if (!existsConfiguration(wc)) {
					config = wc.doSave();
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}*/

			if (!testMode) {
				DebugUITools.launch(wc, mode);
			}
		}

	}

	/**
	 * returns true if str == null || str.length() < 1. Convenience method.
	 *
	 * @param str
	 * @return
	 */
	private static boolean invalid(String str) {
		return (str == null || str.length() < 1);
	}

	/**
	 * Helper function for methods common to both types of finishLaunch.
	 * @throws IOException
	 *
	 */
	private boolean finishLaunchHelper() throws IOException {
		if (invalid(scriptPath)) {
			scriptPath = setScriptPath();
		}
		if (invalid(scriptPath)) {
			// Setting the variable didn't work, do not launch.

			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					Messages
							.getString("SystemTapLaunchShortcut.ErrorMessageName"), //$NON-NLS-1$
					Messages
							.getString("SystemTapLaunchShortcut.ErrorMessageTitle"), Messages.getString("SystemTapLaunchShortcut.ErrorMessage") + name); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
			return false;
		}

		if (invalid(parserID)) {
			parserID = setParserID();
		}
		if (invalid(parserID)) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					Messages.getString("SystemTapLaunchShortcut.InvalidParser1"), //$NON-NLS-1$
					Messages.getString("SystemTapLaunchShortcut.InvalidParser2"), Messages.getString("SystemTapLaunchShortcut.InvalidParser3")); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
			return false;
		}

		if (invalid(viewID)) {
			viewID = setViewID();
		}
		if (invalid(viewID)) {
			// Setting the variable didn't work, do not launch.
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					Messages.getString("SystemTapLaunchShortcut.InvalidView1"), //$NON-NLS-1$
					Messages.getString("SystemTapLaunchShortcut.InvalidView2"), Messages.getString("SystemTapLaunchShortcut.InvalidView3")); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
			return false;
		}

		if (needToGenerate) {
			if (invalid(generatedScript)) {
				generatedScript = generateScript();
			}
			if (invalid(generatedScript)) {
				// Setting the variable didn't work, do not launch.
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
						Messages
								.getString("SystemTapLaunchShortcut.InvalidGeneration1"), //$NON-NLS-1$
						Messages
								.getString("SystemTapLaunchShortcut.InvalidGeneration2"), Messages.getString("SystemTapLaunchShortcut.InvalidGeneration3"));//$NON-NLS-1$ //$NON-NLS-2$
				mess.schedule();
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns bin.getPath().toString()
	 *
	 * @param bin
	 * @return
	 */
	public String getName(IBinary bin) {
		if (bin != null) {
			binName = bin.getPath().toString();
		} else {
			binName = ""; //$NON-NLS-1$
		}
		return binName;
	}

	/**
	 * Creates a configuration for the given IBinary
	 *
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(IBinary bin) {
		if (bin != null) {
			return super.createConfiguration(bin);
		} else {
			try {
				return getLaunchConfigType()
						.newInstance(
								null,
								getLaunchManager()
										.generateLaunchConfigurationName(
												Messages.getString("SystemTapLaunchShortcut.Invalid"))); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Creates a configuration with the given name - does not use a binary
	 *
	 * @param name
	 * @return
	 */
	protected ILaunchConfigurationWorkingCopy createConfiguration(String name) {
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			wc = configType.newInstance(null, getLaunchManager()
					.generateLaunchConfigurationName(name));

			setDefaultProfileAttributes(wc);

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return wc;
	}

	/**
	 * Allows null configurations to be launched. Any launch that uses a binary
	 * should never call this configuration with a null parameter, and any
	 * launch that does not use a binary should never call this function. The
	 * null handling is included for ease of testing.
	 *
	 * @param bin
	 * @param name
	 *            - Customize the name based on the shortcut being launched
	 * @return A launch configuration, or null
	 */
	protected ILaunchConfigurationWorkingCopy createConfiguration(IBinary bin,
			String name) {
		ILaunchConfigurationWorkingCopy wc = null;
		if (bin != null) {
			try {
				String projectName = bin.getResource().getProjectRelativePath().toString();
				ILaunchConfigurationType configType = getLaunchConfigType();
				wc = configType.newInstance(
						null,
						getLaunchManager().generateLaunchConfigurationName(
								name + " - " + bin.getElementName())); //$NON-NLS-1$

				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,projectName);
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
				wc.setMappedResources(new IResource[] { bin.getResource(),bin.getResource().getProject() });
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,(String) null);

				setDefaultProfileAttributes(wc);

			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {

			try {
				wc = getLaunchConfigType().newInstance(
						null,
						getLaunchManager()
								.generateLaunchConfigurationName(name));
				setDefaultProfileAttributes(wc);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
		return wc;
	}

	/**
	 * Creates an error message stating that the launch failed for the specified
	 * reason.
	 *
	 * @param reason
	 */
	protected void failedToLaunch(String reason) {
		SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
				Messages.getString("SystemTapLaunchShortcut.StapLaunchFailed"), //$NON-NLS-1$
				Messages
						.getString("SystemTapLaunchShortcut.StapLaunchFailedTitle"), Messages.getString("SystemTapLaunchShortcut.StapLaunchFailedMessage") + reason); //$NON-NLS-1$ //$NON-NLS-2$
		mess.schedule();
	}

	public void errorHandler() {
	}

	/*
	 * The following are convenience methods for test programs, etc. to check
	 * the value of certain protected parameters.
	 */

	public ILaunchConfigurationType outsideGetLaunchConfigType() {
		return getLaunchConfigType();
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public String getDirPath() {
		return dirPath;
	}

	public String getArguments() {
		return arguments;
	}

	public String getBinaryPath() {
		return binaryPath;
	}

	/**
	 * Retrieves the names of all functions referenced by the binary. If
	 * searchForResource is true, this function will return all function names
	 * belonging to an element with name matching the String held by
	 * resourceToSearchFor. Otherwise it will create a dialog prompting the user
	 * to select from a list of files to profile, or select the only available
	 * file if only one file is available.
	 *
	 *
	 * @param bin
	 * @return
	 */
	protected String getFunctionsFromBinary(IBinary bin, String targetResource) {
		String funcs = ""; //$NON-NLS-1$
		if (bin == null) {
			return funcs;
		}
		try {
			ArrayList<ICContainer> list = new ArrayList<ICContainer>();
			TranslationUnitVisitor v = new TranslationUnitVisitor();

			for (ICElement b : bin.getCProject().getChildrenOfType(
					ICElement.C_CCONTAINER)) {
				ICContainer c = (ICContainer) b;

				for (ITranslationUnit tu : c.getTranslationUnits()) {
					if (searchForResource
							&& tu.getElementName().contains(targetResource)) {
						tu.accept(v);
						funcs += v.getFunctions();
						return funcs;
					} else {
						if (!list.contains(c)) {
							list.add(c);
						}
					}
				}

				// Iterate down to all children, checking for more C_Containers
				while (c.getChildrenOfType(ICElement.C_CCONTAINER).size() > 0) {
					ICContainer e = null;
					for (ICElement d : c
							.getChildrenOfType(ICElement.C_CCONTAINER)) {
						e = (ICContainer) d;
						for (ITranslationUnit tu : e.getTranslationUnits()) {
							if (searchForResource
									&& tu.getElementName().contains(
											targetResource)) {
								tu.accept(v);
								funcs += (v.getFunctions());
								return funcs;
							} else {
								if (!list.contains(c)) {
									list.add(c);
								}
							}
						}
					}
					c = e;
				}
			}

			int numberOfFiles = numberOfValidFiles(list.toArray());
			if (numberOfFiles == 1) {
				for (ICContainer c : list) {
					for (ITranslationUnit e : c.getTranslationUnits()) {
						if (validElement(e)) {
							e.accept(v);
							funcs += v.getFunctions();
						}
					}
				}
			} else {

				Object[] unitList = chooseUnit(list, numberOfFiles);
				if (unitList == null || unitList.length == 0) {
					return null;
				} else if (unitList.length == 1
						&& unitList[0].toString().equals(USER_SELECTED_ALL)) {
					funcs = "*"; //$NON-NLS-1$
					return funcs;
				}

				StringBuilder tmpFunc = new StringBuilder();
				for (String item : getAllFunctions(bin.getCProject(), unitList)) {
					tmpFunc.append(item);
					tmpFunc.append(" "); //$NON-NLS-1$
				}
				funcs = tmpFunc.toString();

			}

			return funcs;

		} catch (CModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a dialog that prompts the user to select from the given list of
	 * ICElements
	 *
	 * @param list
	 *            : list of ICElements
	 * @return
	 */
	protected Object[] chooseUnit(List<ICContainer> list, int numberOfValidFiles) {
		ListTreeContentProvider prov = new ListTreeContentProvider();

		RuledTreeSelectionDialog dialog = new RuledTreeSelectionDialog(
				getActiveWorkbenchShell(), new WorkbenchLabelProvider(), prov);

		dialog.setTitle(Messages.getString("SystemTapLaunchShortcut.SelectFiles")); //$NON-NLS-1$
		dialog.setMessage(Messages.getString("SystemTapLaunchShortcut.SelectFilesMsg")); //$NON-NLS-1$
		dialog.setInput(list);
		dialog.setHelpAvailable(false);
		dialog.setStatusLineAboveButtons(false);
		dialog.setEmptyListMessage(Messages
				.getString("SystemTapLaunchShortcut.NoFiles")); //$NON-NLS-1$
		dialog.setContainerMode(true);

		Object[] topLevel = prov.findElements(list);
		dialog.setInitialSelections(topLevel);
		dialog.setSize(cap(topLevel.length * 10, 30, 55), cap(
				(int) (topLevel.length * 1.5), 3, 13));

		dialog.create();
		OKButton = dialog.getOkButton();

		Object[] result = null;

		if (testMode) {
			OKButton.setSelection(true);
			result = list.toArray();
			ArrayList<Object> output = new ArrayList<Object>();
			try {
				for (Object obj : result) {
					if (obj instanceof ICContainer) {
						ICElement[] array = ((ICContainer) obj).getChildren();
						for (ICElement c : array) {
							if (!(validElement(c))) {
								continue;
							}
							if (c.getElementName().contains(MAIN_FUNC_NAME) && !output.contains(c)) {
								output.add(c);
							}
						}
					}
				}

				if (output.size() >= numberOfValidFiles) {
					output.clear();
					output.add(USER_SELECTED_ALL);
				}
			} catch (CModelException e) {
				e.printStackTrace();
			}

			result = output.toArray();
		} else {
			if (dialog.open() == Window.CANCEL) {
				return null;
			}
			result = dialog.getResult();
		}

		if (result == null) {
			return null;
		}

		ArrayList<Object> output = new ArrayList<Object>();
		try {
			for (Object obj : result) {
				if (obj instanceof ICContainer) {
					ICElement[] array = ((ICContainer) obj).getChildren();
					for (ICElement c : array) {
						if (!(validElement(c))) {
							continue;
						}
						if (!output.contains(c)) {
							output.add(c);
						}
					}
				} else if ((obj instanceof ICElement)
						&& validElement((ICElement) obj)
						&& !output.contains(obj)) {
					output.add(obj);
				}
			}

			if (output.size() >= numberOfValidFiles) {
				output.clear();
				output.add(USER_SELECTED_ALL);
			}
		} catch (CModelException e) {
			e.printStackTrace();
		}

		return output.toArray();
	}

	private int numberOfValidFiles(Object[] list) throws CModelException {
		int output = 0;
		for (Object parent : list) {
			if (parent instanceof ICContainer) {
				ICContainer cont = (ICContainer) parent;
				for (ICElement ele : cont.getChildren()) {
					if (ele instanceof ICContainer) {
						output += numberOfValidFiles(((ICContainer) ele)
								.getChildren());
					}
					if (validElement(ele)) {
						output++;
					}
				}
			} else if ((parent instanceof ICElement)
					&& validElement((ICElement) parent)) {
				output++;
			}
		}
		return output;
	}

	/**
	 * Convenience method for creating a new configuration
	 *
	 * @return a new configuration
	 * @throws CoreException
	 */
	public ILaunchConfiguration getNewConfiguration() throws CoreException {
		ILaunchConfigurationType configType = getLaunchConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(
				null,
				getLaunchManager().generateLaunchConfigurationName(
						"TestingConfiguration")); //$NON-NLS-1$

		return wc.doSave();

	}

	/**
	 * @param project
	 *            : C Project Type
	 * @return A String list of all functions contained within the specified C
	 *         Project
	 */
	public static ArrayList<String> getAllFunctions(ICProject project,
			Object[] listOfFiles) {
		try {
			GetFunctionsJob j = new GetFunctionsJob(project
					.getHandleIdentifier(), project, listOfFiles);
			j.schedule();
			j.join();
			ArrayList<String> functionList = j.getFunctionList();

			return functionList;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns all ICElements in val that contains the given path. WARNING: Uses
	 * .contains, so be careful with the String path or you'll get too many
	 * hits.
	 *
	 * @param list
	 * @param path
	 * @return
	 */
	private static boolean specialContains(Object[] list, String path) {
		for (Object val : list) {
			if (val instanceof ICElement) {
				ICElement el = (ICElement) val;
				if (el.getPath().toString().contains(path)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a number clipped into the range [low,high].
	 *
	 * @param number
	 * @param low
	 * @param high
	 * @return number if number is in [low,high], low, or high.
	 */
	private int cap(int number, int low, int high) {
		if (number > high) {
			return high;
		}
		if (number < low) {
			return low;
		}
		return number;
	}

	/**
	 * Function for generating scripts. Should be overridden by interested
	 * classes
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public String generateScript() throws IOException {
		return null;
	}

	private static class GetFunctionsJob extends Job {
		private ArrayList<String> functionList;
		private ICProject project;
		private Object[] listOfFiles;

		public GetFunctionsJob(String name, ICProject p, Object[] o) {
			super(name);
			functionList = new ArrayList<String>();
			listOfFiles = Arrays.copyOf(o, o.length);
			project = p;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IIndexManager manager = CCorePlugin.getIndexManager();
			IIndex index = null;
			IProgressMonitor m = monitor;

			if (m == null) {
				m = new NullProgressMonitor();
			}
			m.worked(1);

			try {
				index = manager.getIndex(project);
				index.acquireReadLock();

				IIndexFile[] blah = index.getAllFiles();
				for (IIndexFile file : blah) {
					String fullFilePath = file.getLocation().getFullPath();
					if (fullFilePath == null
							|| !specialContains(listOfFiles, fullFilePath)) {
						continue;
					}

					IIndexName[] indexNamesArray = file.findNames(0,
							Integer.MAX_VALUE);
					for (IIndexName name : indexNamesArray) {
						if (name.isDefinition()
								&& specialContains(listOfFiles, name.getFile()
										.getLocation().getFullPath())) {
							IIndexBinding binder = index.findBinding(name);
							if (binder instanceof IFunction
									&& !functionList.contains(binder.getName())) {
								functionList.add(binder.getName());
							}
						}
					}

					m.worked(1);
				}

			} catch (CoreException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			index.releaseReadLock();
			return Status.OK_STATUS;
		}

		public ArrayList<String> getFunctionList() {
			return functionList;
		}
	}


	/**
	 * Set the parserID variable. ParserID should point to the ID of an
	 * extension extending the org.eclipse.linuxtools.callgraph.core.parser
	 * extension point. This function must return the parserID to be set.
	 *
	 * If not declared, the parserID will be set to the default SystemTap
	 * Text parser with colour support
	 *
	 * @return a valid parserID
	 */
	public String setParserID() {
		return PluginConstants.DEFAULT_PARSER_ID;
	}

	public String getScript() {
		return generatedScript;
	}

	public Button getButton() {
		return OKButton;
	}

	public void setTestMode(boolean val) {
		testMode = val;
	}

	/**
	 * Set the viewID variable. ViewID should point to the ID of an extension
	 * extending the org.eclipse.ui.views extension point. This function must
	 * return the viewID to be set. Defaults to the SystemTap Text View, if
	 * not overridden.
	 *
	 * @return a valid viewID
	 */
	public String setViewID() {
		return PluginConstants.DEFAULT_VIEW_ID;
	}

	public static boolean validElement(ICElement e) {
		return e.getElementName().endsWith(".c") || //$NON-NLS-1$
		e.getElementName().endsWith(".cpp") || //$NON-NLS-1$
		e.getElementName().endsWith(".h"); //$NON-NLS-1$
	}


	/**
	 * Default implementation of launch. It will run stap with the selected binary
	 * as an argument and set the output path to <code>PluginConstants.getDefaultIOPath()</code>.
	 * <br>
	 * The name of the created launch will be 'DefaultSystemTapLaunch'
	 */
	@Override
	public void launch(IBinary bin, String mode) {
		initialize();
		this.bin = bin;
		binName = getName(bin);
		name = "DefaultSystemTapLaunch";  //$NON-NLS-1$

		try {

			ILaunchConfigurationWorkingCopy wc = createConfiguration(bin, name);
			binaryPath = bin.getResource().getLocation().toString();
			arguments = binaryPath;
			outputPath = PluginConstants.getDefaultIOPath();
			finishLaunch(name, mode, wc);

		} catch (IOException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					"LaunchShortcutScriptGen",  //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.ScriptGenErr"),   //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.ScriptGenErrMsg"));  //$NON-NLS-1$
			mess.schedule();
			e.printStackTrace();
		} finally {
			resourceToSearchFor = ""; //$NON-NLS-1$
			searchForResource = false;
		}


	}


	/**
	 * Each launch class should define its own script path. Must return the
	 * correct script path or launch will fail.
	 */
	public abstract String setScriptPath();

	/**
	 * Overwrite to return a non-empty string if you want to be able to create
	 * multiple views.
	 * @return
	 */
	public String setSecondaryViewID() {
		return ""; //$NON-NLS-1$
	}

}
