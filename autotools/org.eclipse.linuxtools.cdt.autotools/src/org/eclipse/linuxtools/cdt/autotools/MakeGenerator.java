/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.ITarget;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.newmake.core.IMakeCommonBuildInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.linuxtools.cdt.autotools.internal.MarkerGenerator;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MakeGenerator extends MarkerGenerator implements IManagedBuilderMakefileGenerator, IManagedBuilderMakefileGenerator2 {

	public final String CONFIG_STATUS = "config.status"; //$NON-NLS-1$
	public final String MAKEFILE = "Makefile"; //$NON-NLS-1$
	public final String MAKEFILE_CVS = "Makefile.cvs"; //$NON-NLS-1$
	public final String SETTINGS_FILE_NAME = ".cdtconfigure"; //$NON-NLS-1$
	public final String SHELL_COMMAND = "sh"; //$NON-NLS-1$
	
	public final String AUTOGEN_TOOL_ID = "org.eclipse.linuxtools.cdt.autotools.tool.autogen"; //$NON-NLS-1$
	public final String CONFIGURE_TOOL_ID = "org.eclipse.linuxtools.cdt.autotools.tool.configure"; //$NON-NLS-1$
	
	public final String GENERATED_TARGET = AutotoolsPlugin.PLUGIN_ID + ".generated.MakeTarget"; //$NON-NLS-1$

	private static final String MAKE_TARGET_KEY = MakeCorePlugin.getUniqueIdentifier() + ".buildtargets"; //$NON-NLS-1$
	private static final String BUILD_TARGET_ELEMENT = "buildTargets"; //$NON-NLS-1$
	private static final String TARGET_ELEMENT = "target"; //$NON-NLS-1$
	private static final String TARGET_ATTR_ID = "targetID"; //$NON-NLS-1$
	private static final String TARGET_ATTR_PATH = "path"; //$NON-NLS-1$
	private static final String TARGET_ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String TARGET_STOP_ON_ERROR = "stopOnError"; //$NON-NLS-1$
	private static final String TARGET_USE_DEFAULT_CMD = "useDefaultCommand"; //$NON-NLS-1$
	private static final String TARGET_ARGUMENTS = "buildArguments"; //$NON-NLS-1$
	private static final String TARGET_COMMAND = "buildCommand"; //$NON-NLS-1$
	private static final String TARGET_RUN_ALL_BUILDERS = "runAllBuilders";
	private static final String TARGET = "buildTarget"; //$NON-NLS-1$

	private IProject project;

	private IProgressMonitor monitor;

	private String buildDir;
	private String srcDir;

	private IConfiguration cfg;
	private IBuilder builder;

	public void generateDependencies() throws CoreException {
		// TODO Auto-generated method stub

	}

	public MultiStatus generateMakefiles(IResourceDelta delta)
			throws CoreException {
		return regenerateMakefiles();
	}

	private void initializeBuildConfigDirs() {
		ITool tool = cfg.getToolFromOutputExtension("status"); //$NON-NLS-1$
		IOption[] options = tool.getOptions();
		for (int i = 0; i < options.length; ++i) {
			String id = options[i].getId();
			if (id.indexOf("builddir") > 0) { //$NON-NLS-1$
				buildDir = (String) options[i].getValue();
				try {
					String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValue(buildDir, "", null, 
							IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
					if (resolved != null && (resolved = resolved.trim()).length() > 0)
						buildDir = resolved;
				} catch (BuildMacroException e) {
					// do nothing
				}
				// Fix for 266451.  Check if value has changed and use setBuildPath() instead of setBuildAttribute() 
				// since we want the builder marked as dirty and the project info saved.  We must use getBuildAttribute()
				// instead of getBuildPath() to avoid an infinite loop where getBuildPath() may call this function to
				// find the default path.
				String oldPath = builder.getBuildAttribute(IMakeCommonBuildInfo.BUILD_LOCATION,"");
				String newPath = "${workspace_loc:/" + project.getName() + "/" + buildDir + "}";
				if (oldPath == null || !oldPath.equals(newPath))
					builder.setBuildPath(newPath);
			} else if (id.indexOf("configdir") > 0) {  //$NON-NLS-1$
				srcDir = (String) options[i].getValue();
				try {
					String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValue(srcDir, "", null, 
							IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
					if (resolved != null && (resolved = resolved.trim()).length() > 0)
						srcDir = resolved;
				} catch (BuildMacroException e) {
					// do nothing
				}
			}
		}
	}
	
	public void initialize(IProject project, IManagedBuildInfo info,
			IProgressMonitor monitor) {
		this.project = project;
		this.cfg = info.getDefaultConfiguration();
		this.builder = cfg.getBuilder();
		this.monitor = monitor;
		initializeBuildConfigDirs();
	}

	public void initialize(int buildKind, IConfiguration cfg, IBuilder builder, 
			IProgressMonitor monitor) {
		this.cfg = cfg;
		this.builder = builder;
		this.monitor = monitor;
		this.project = (IProject)cfg.getManagedProject().getOwner();
		initializeBuildConfigDirs();
	}

	public IProject getProject() {
		return project;
	}
	
	public boolean isGeneratedResource(IResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	public void regenerateDependencies(boolean force) throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc) Check whether the build has been cancelled. Cancellation
	 * requests propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	protected void checkCancel() {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/*
	 * (non-Javadoc) Return or create the makefile needed for the build. If we
	 * are creating the resource, set the derived bit to true so the CM system
	 * ignores the contents. If the resource exists, respect the existing
	 * derived setting.
	 * 
	 * @param makefilePath @return IFile
	 */
	protected IFile createFile(IPath makefilePath) throws CoreException {
		// Create or get the handle for the makefile
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(makefilePath);
		if (newFile == null) {
			newFile = root.getFile(makefilePath);
		}
		// Create the file if it does not exist
		ByteArrayInputStream contents = new ByteArrayInputStream(new byte[0]);
		try {
			newFile.create(contents, false, new SubProgressMonitor(monitor, 1));
			// Make sure the new file is marked as derived
			if (!newFile.isDerived()) {
				newFile.setDerived(true);
			}

		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				newFile.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		return newFile;
	}

	/*
	 * (non-Javadoc) Return or create the folder needed for the build output. If
	 * we are creating the folder, set the derived bit to true so the CM system
	 * ignores the contents. If the resource exists, respect the existing
	 * derived setting.
	 * 
	 * @param string @return IPath
	 */
	private IPath createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the build directory
		if (dirName.length() == 0 || dirName.equals("."))
			return project.getFullPath();
		IFolder folder = project.getFolder(dirName);
		if (!folder.exists()) {
			// Make sure that parent folders exist
			IPath parentPath = (new Path(dirName)).removeLastSegments(1);
			// Assume that the parent exists if the path is empty
			if (!parentPath.isEmpty()) {
				IFolder parent = project.getFolder(parentPath);
				if (!parent.exists()) {
					createDirectory(parentPath.toString());
				}
			}

			// Now make the requested folder
			try {
				folder.create(true, true, null);
			} catch (CoreException e) {
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
					folder.refreshLocal(IResource.DEPTH_ZERO, null);
				else
					throw e;
			}

			// Make sure the folder is marked as derived so it is not added to
			// CM
			if (!folder.isDerived()) {
				folder.setDerived(true);
			}
		}

		return folder.getFullPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getBuildWorkingDir()
	 */
	public IPath getBuildWorkingDir() {
		return new Path(buildDir);
	}

	/*
	 * (non-Javadoc) Get the project's absolute path. @return IPath
	 */
	private IPath getProjectLocation() {
		return project.getLocation();
	}

	private String getAbsoluteDirectory(String dir) {
		IPath path = new Path(dir);
		if (!path.isAbsolute()) {
			IPath absPath = getProjectLocation().addTrailingSeparator().append(
					path);
			return absPath.toOSString();
		}
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getMakefileName()
	 */
	public String getMakefileName() {
		return new String(MAKEFILE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return the path to the configuration settings file
	 */
	protected IPath getConfigSettingsPath() {
		IPath path = project.getLocation().append(SETTINGS_FILE_NAME + "." + cfg.getName()); //$NON-NLS-1$
		return path;
	}
	
	private class PathVar {
		IPath path;
	};

	public MultiStatus regenerateMakefiles() throws CoreException {
		MultiStatus status;
		int rc = IStatus.OK;
		String errMsg = new String();
		boolean needFullConfigure = true;

		// See if the user has cancelled the build
		checkCancel();

		// Create the top-level directory for the build output
		createDirectory(buildDir);
		checkCancel();

		// // How did we do
		// if (!getInvalidDirList().isEmpty()) {
		// status = new MultiStatus (
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// IStatus.WARNING,
		// new String(),
		// null);
		// // Add a new status for each of the bad folders
		// iter = getInvalidDirList().iterator();
		// while (iter.hasNext()) {
		// status.add(new Status (
		// IStatus.WARNING,
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// SPACES_IN_PATH,
		// ((IContainer)iter.next()).getFullPath().toString(),
		// null));
		// }
		// } else {
		// status = new MultiStatus(
		// ManagedBuilderCorePlugin.getUniqueIdentifier(),
		// IStatus.OK,
		// new String(),
		// null);
		// }

		// Get a build console for the project
		IConsole console = CCorePlugin.getDefault().getConsole("org.eclipse.linuxtools.cdt.autotools.configureConsole"); //$NON-NLS-1$

		// Get the project and make sure there's a monitor to cancel the build
		IProject currentProject = project;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			IWorkspace workspace = currentProject.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			// If a config.status file exists in the build directory, we call it
			// to
			// regenerate the makefile
			IPath configfile = getProjectLocation().append(buildDir).append(
					CONFIG_STATUS);
			IPath makefilePath = getProjectLocation().append(buildDir).append(MAKEFILE);
			IFile configStatus = root.getFileForLocation(configfile);
			IFile makefile = root.getFileForLocation(makefilePath);
			IPath configSettingsPath = getConfigSettingsPath();
			IFile configSettings = root.getFileForLocation(configSettingsPath);
			String[] configArgs = getConfigArgs();
			PathVar autogenPath = new PathVar();

			// We need to figure out if the end-user has changed the configuration
			// settings.  In such a case, we need to reconfigure from scratch
			// regardless of whether config.status exists or not.
			// We figure this out by saving the configuration settings to
			// a special file and reading/comparing whenever we are asked to build.
			if (configSettings.exists()) {
				int i = 0;
				boolean needSaveConfigArgs = false;
				needFullConfigure = false;
				IPath settingsPath = getConfigSettingsPath();
				try {
					File f = new File(settingsPath.toOSString());
					DataInputStream settings = new DataInputStream(
							new BufferedInputStream(new FileInputStream(f)));
					// Get the first String in the configure settings file.
					// Newer configure settings file start with the project name.  
					// If the project name is present and doesn't match the
					// current project name, the project has been refactored and
					// we need to do a full reconfigure.
					settings.mark(100);
					String s = settings.readUTF();
					if (s.startsWith("project=")) { //$NON-NLS-1$
						if (!s.substring(8).equals(project.getName())) {
							needFullConfigure = true;
						}
					} else {
						// An older configure arguments file.  Reset
						// to beginning and process as normal.
						needSaveConfigArgs = true;
						settings.reset();
					}
					while (i < configArgs.length) {
						s = settings.readUTF();
						if (!s.equals(configArgs[i])) {
							i = configArgs.length;
							needFullConfigure = true;
						}
						++i;
					}
					if (settings.available() > 0)
						needFullConfigure = true;
				} catch (EOFException e) {
					needFullConfigure = true;
				} catch (IOException e) {
					needFullConfigure = true;
				}
				if (needFullConfigure) {
					// If we are going to do a full reconfigure, then if the current
					// build directory exists, we should clean it out first.  This is
					// because the reconfiguration could change compile flags, etc..
					// and the Makefile might not detect a rebuild is required.
					IResource r = root.findMember(project.getFullPath().append(buildDir));
					if (r != null && r.exists()) {
						// See what type of cleaning the user has set up in the
						// build properties dialog.
						String cleanDelete = null;
						try {
							cleanDelete = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
						} catch (CoreException ce) {
							// do nothing
						}
						
						if (cleanDelete != null && cleanDelete.equals(AutotoolsPropertyConstants.TRUE))
							r.delete(true, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
						else {
							// There is a make target for cleaning.
							if (makefile != null && makefile.exists()) {
								String[] makeargs = new String[1];
								IPath makeCmd = new Path("make"); //$NON-NLS-1$
								String target = null;
								try {
									target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
								} catch (CoreException ce) {
									// do nothing
								}
								if (target == null)
									target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
								String args = builder.getBuildArguments();
								if (args != null && !(args = args.trim()).equals("")) { //$NON-NLS-1$
									String[] newArgs = makeArray(args);
									makeargs = new String[newArgs.length + 1];
									System.arraycopy(newArgs, 0, makeargs, 0, newArgs.length);
								}
								makeargs[makeargs.length - 1] = target;
								rc = runCommand(makeCmd,
										project.getLocation().append(buildDir),
										makeargs,
										AutotoolsPlugin.getResourceString("MakeGenerator.clean.builddir"), //$NON-NLS-1$
										errMsg, console, true);
							}
						}
					}
					initializeBuildConfigDirs();
					createDirectory(buildDir);
					// Mark the scanner info as dirty.
					try {
						project.setSessionProperty(AutotoolsPropertyConstants.SCANNER_INFO_DIRTY, Boolean.TRUE);
					} catch (CoreException ce) {
						// do nothing
					}
				} else if (needSaveConfigArgs) {
					// No change in configuration args, but we have old
					// style settings format which can't determine if project has
					// been renamed.  Refresh the settings file.
					saveConfigArgs(configArgs);
				}
			}
			// Check if we have a config.status (meaning configure has already run).
    		if (!needFullConfigure && configStatus != null && configStatus.exists()) {
			    // If no corresponding Makefile in the same build location, then we
	            // can simply run config.status again to ensure the top level Makefile has been
				// created.
				if (makefile == null || !makefile.exists()) {
					rc = runScript(configfile, project.getLocation().append(
							buildDir), null, 
							AutotoolsPlugin.getResourceString("MakeGenerator.run.config.status"), //$NON-NLS-1$
							errMsg, console, true);
				}
			}
			// Look for configure and configure from scratch
			else if (configureExists()) {
				rc = runScript(getConfigurePath(), 
						project.getLocation().append(buildDir),
						configArgs, 
						AutotoolsPlugin.getResourceString("MakeGenerator.gen.makefile"), //$NON-NLS-1$
						errMsg, console, true);
				if (rc != IStatus.ERROR) {
					File makefileFile = project.getLocation().append(buildDir)
					.append(MAKEFILE).toFile();
					addMakeTargetsToManager(makefileFile);
					// TODO: should we do something special if configure doesn't
					// return ok?
					saveConfigArgs(configArgs);
				}
			}
			// If no configure, look for autogen.sh which may create configure and
    		// possibly even run it.
			else if (autogenExists(autogenPath)) {
				// Remove the existing config.status file since we use it
				// to figure out if configure was run.
				if (configStatus.exists())
					configStatus.delete(true, null);
				// Get any user-specified arguments for autogen.
				String[] autogenArgs = getAutogenArgs();
				rc = runScript(autogenPath.path,
						autogenPath.path.removeLastSegments(1), autogenArgs,
						AutotoolsPlugin.getResourceString("MakeGenerator.autogen.sh"), //$NON-NLS-1$
						errMsg, console, true);
				if (rc != IStatus.ERROR) {
					configStatus = root.getFileForLocation(configfile);
					// Check for config.status.  If it is created, then
					// autogen.sh ran configure and we should not run it
					// ourselves.
					if (configStatus == null || !configStatus.exists()) {
						rc = runScript(getConfigurePath(), 
								project.getLocation().append(buildDir),
								configArgs, 
								AutotoolsPlugin.getResourceString("MakeGenerator.gen.makefile"), //$NON-NLS-1$
								errMsg, console, false);
						if (rc != IStatus.ERROR) {
							File makefileFile = project.getLocation().append(buildDir)
							.append(MAKEFILE).toFile();
							addMakeTargetsToManager(makefileFile);
						}
					}
				}
			}
			// If nothing this far, look for a Makefile.cvs file which needs to be run. 
			else if (makefileCvsExists()) {
				String[] makeargs = new String[1];
				IPath makeCmd = new Path("make"); //$NON-NLS-1$
				makeargs[0] = "-f" + getMakefileCVSPath().toOSString(); //$NON-NLS-1$
				rc = runCommand(makeCmd,
						project.getLocation().append(buildDir),
						makeargs,
						AutotoolsPlugin.getResourceString("MakeGenerator.makefile.cvs"), //$NON-NLS-1$
						errMsg, console, true);
				if (rc != IStatus.ERROR) {
					File makefileFile = project.getLocation().append(buildDir)
					.append(MAKEFILE).toFile();
					addMakeTargetsToManager(makefileFile);
					saveConfigArgs(configArgs);
				}
			}
    		// If we didn't create a Makefile, consider that an error.
			if (makefile == null || !makefile.exists()) {
				rc = IStatus.ERROR;
				errMsg = AutotoolsPlugin.getResourceString("MakeGenerator.didnt.generate"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
			// forgetLastBuiltState();
			rc = IStatus.ERROR;
		} finally {
			// getGenerationProblems().clear();
			status = new MultiStatus(ManagedBuilderCorePlugin
						.getUniqueIdentifier(), rc, errMsg, null);
		}
		return status;
	}

	protected IPath getConfigurePath() {
		IPath configPath;
		ITool[] tool = cfg.getToolsBySuperClassId(CONFIGURE_TOOL_ID);
		String command = tool[0].getToolCommand();
		if (srcDir.equals(""))
			configPath = project.getLocation().append(command);
		else
			configPath = project.getLocation().append(srcDir).append(command);
		return configPath;
	}
	
	protected boolean configureExists() {
		IPath configCommand = getConfigurePath();
		return configCommand.toFile().exists();
	}

	protected IPath getMakefileCVSPath() {
		IPath makefileCVSPath;
		if (srcDir.equals(""))
			makefileCVSPath = project.getLocation().append(MAKEFILE_CVS);
		else
			makefileCVSPath= project.getLocation().append(srcDir).append(
					MAKEFILE_CVS);
		return makefileCVSPath;
	}
	
	protected boolean makefileCvsExists() {
		IPath makefileCVSPath = getMakefileCVSPath();
		return makefileCVSPath.toFile().exists();
	}

	/*
	 * Calculate whether a *autogen.sh file exists in the src directory.
	 * 
	 * @param autogenPath is an empty path passed in which will be filled in with location of file if found
	 * 
	 * @return true if *autogen.sh is found, false otherwise
	 */
	protected boolean autogenExists(PathVar autogenPath) {
		IPath path = project.getLocation();
		if (!srcDir.equals("") && !srcDir.equals("."))
			path = path.append(srcDir);
		ITool[] tool = cfg.getToolsBySuperClassId(AUTOGEN_TOOL_ID);
		String command = tool[0].getToolCommand();
		IPath autogenCommand = path.append(command);
		if (autogenCommand.toFile().exists()) {
			autogenPath.path = autogenCommand;
			return true;
		}
		// No match, return false
		return false;
	}

	private void saveConfigArgs(String[] args) {
		IPath settingsPath = getConfigSettingsPath();
		try {
			File f = new File(settingsPath.toOSString());
			DataOutputStream settings = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(f)));
			// Write the project name in the configure arguments
			// so we know if the project gets refactored.
			settings.writeUTF("project=" + project.getName()); //$NON-NLS-1$
			for (int i = 0; i < args.length; ++i) {
				settings.writeUTF(args[i]);
			}
			settings.close();
		} catch (IOException e) {
			/* What should we do?  */
		}
	}
	
	private String[] getAutogenArgs() throws BuildException {
		// Get the arguments to be passed to config from build model
		ITool[] tool = cfg.getToolsBySuperClassId(AUTOGEN_TOOL_ID);
		IOption[] options = tool[0].getOptions();
		ArrayList<String> autogenArgs = new ArrayList<String>();
		
		for (int i = 0; i < options.length; ++i) {
			if (options[i].getValueType() == IOption.STRING) {
				String value = (String) options[i].getValue();
				try {
					String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValue(value, "", null, 
							IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
					if(resolved != null && (resolved = resolved.trim()).length() > 0)
						value = resolved;
				} catch (BuildMacroException e) {
					// do nothing
				}
				String id = options[i].getId();
				if (id.indexOf("user") > 0) { //$NON-NLS-1$
					// May be multiple user-specified options in which case we
					// need to split them up into individual options
					value = value.trim();
					boolean finished = false;
					int lastIndex = value.indexOf("--"); //$NON-NLS-1$
					if (lastIndex != -1) {
						while (!finished) {
							int index = value.indexOf("--",lastIndex+2); //$NON-NLS-1$
							if (index != -1) {
								String previous = value.substring(lastIndex, index).trim();
								autogenArgs.add(previous);
								value = value.substring(index);
							} else {
								autogenArgs.add(value);
								finished = true;
							}
						}
					}
				}
			}
		}
		return (String[]) autogenArgs.toArray(new String[autogenArgs.size()]);
	}

	private String[] getConfigArgs() throws BuildException {
		// Get the arguments to be passed to config from build model
		ITool tool = cfg.getToolFromOutputExtension("status"); //$NON-NLS-1$
		IOption[] options = tool.getOptions();
		ArrayList<String> configArgs = new ArrayList<String>();
		for (int i = 0; i < options.length; ++i) {
			if (options[i].getValueType() == IOption.STRING) {
				String value = (String) options[i].getValue();
				try {
					String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValue(value, "", null, 
							IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
					if(resolved != null && (resolved = resolved.trim()).length() > 0)
						value = resolved;
				} catch (BuildMacroException e) {
					// do nothing
				}
				String id = options[i].getId();
				if (id.indexOf("configdir") > 0 || id.indexOf("builddir") > 0) //$NON-NLS-1$ $NON-NLS-2$
					continue;
				else if (id.indexOf("user") > 0) { //$NON-NLS-1$
					// May be multiple user-specified options in which case we
					// need to split them up into individual options
					value = value.trim();
					boolean finished = false;
					int lastIndex = value.indexOf("--"); //$NON-NLS-1$
					if (lastIndex != -1) {
						while (!finished) {
							int index = value.indexOf("--",lastIndex+2); //$NON-NLS-1$
							if (index != -1) {
								String previous = value.substring(lastIndex, index).trim();
								configArgs.add(previous);
								value = value.substring(index);
							} else {
								configArgs.add(value);
								finished = true;
							}
						}
					} else {
						// No --xxx arguments, but there might still be some NAME=VALUE args
						// and we should pass them on regardless
						configArgs.add(value);
					}
				}
				else if (value.trim().length() > 0) {
					String categoryId = options[i].getCategory().getId();
					if (categoryId.indexOf("directories") >= 0) //$NON-NLS-1$
						value = getAbsoluteDirectory(value);
					String cmd = options[i].getCommand().concat(value);
					configArgs.add(cmd);
				}
			} else if (options[i].getValueType() == IOption.BOOLEAN) {
				Boolean value = (Boolean) options[i].getValue();
				if (value.booleanValue())
					configArgs.add(options[i].getCommand());
				else if (!options[i].getCommandFalse().equals(""))
					configArgs.add(options[i].getCommandFalse());
			}
		}
		return (String[]) configArgs.toArray(new String[configArgs.size()]);
	}

	// Run a command or executable (e.g. make).
	private int runCommand(IPath commandPath, IPath runPath, String[] args,
			String jobDescription, String errMsg, IConsole console, 
			boolean consoleStart) throws BuildException, CoreException,
			NullPointerException, IOException {
		// TODO: Figure out what this next stuff is used for
		// //try to resolve the build macros in the builder command
		// try{
		// String resolved =
		// ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
		// configCmd,
		// "", //$NON-NLS-1$
		// " ", //$NON-NLS-1$
		// IBuildMacroProvider.CONTEXT_CONFIGURATION,
		// info.getDefaultConfiguration());
		// if((resolved = resolved.trim()).length() > 0)
		// configCmd = resolved;
		// } catch (BuildMacroException e){
		// }

		int rc = IStatus.OK;
		
		removeAllMarkers(project);
		
		String[] configTargets = args;
		if (args == null)
			configTargets = new String[0];
	
		String[] msgs = new String[2];
		msgs[0] = commandPath.toString();
		msgs[1] = project.getName();
		monitor.subTask(AutotoolsPlugin.getFormattedString(
				"MakeGenerator.make.message", msgs)); //$NON-NLS-1$


		ConsoleOutputStream consoleOutStream = null;
		StringBuffer buf = new StringBuffer();

		// Launch command - main invocation
		if (consoleStart)
			console.start(project);
		consoleOutStream = console.getOutputStream();
		String[] consoleHeader = new String[3];

		consoleHeader[0] = jobDescription;
		consoleHeader[1] = cfg.getName();
		consoleHeader[2] = project.getName();
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		buf.append(jobDescription);
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

		if (!cfg.isSupported()) {
			String msgArgs[] = new String[1];
			msgArgs[0] = cfg.getName();
			buf.append(AutotoolsPlugin.getFormattedString("MakeGenerator.unsupportedConfig", msgArgs));
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		}
		consoleOutStream.write(buf.toString().getBytes());
		consoleOutStream.flush();

		// Get a launcher for the config command
		CommandLauncher launcher = new CommandLauncher();
		// Set the environment
		IEnvironmentVariable variables[] = ManagedBuildManager
				.getEnvironmentVariableProvider().getVariables(cfg, true);
		String[] env = null;
		ArrayList<String> envList = new ArrayList<String>();
		if (variables != null) {
			for (int i = 0; i < variables.length; i++) {
				envList.add(variables[i].getName()
						+ "=" + variables[i].getValue()); //$NON-NLS-1$
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}

		// // Hook up an error parser manager
		// String[] errorParsers =
		// info.getDefaultConfiguration().getErrorParserList();
		// ErrorParserManager epm = new ErrorParserManager(project, topBuildDir,
		// this, errorParsers);
		// epm.setOutputStream(consoleOutStream);
		OutputStream stdout = consoleOutStream;
		OutputStream stderr = consoleOutStream;

		launcher.showCommand(true);
		Process proc = launcher.execute(commandPath, configTargets, env,
				runPath);
		if (proc != null) {
			try {
				// Close the input of the process since we will never write to
				// it
				proc.getOutputStream().close();
			} catch (IOException e) {
			}

			if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
					monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
				errMsg = launcher.getErrorMessage();
			}

			// Force a resync of the projects without allowing the user to
			// cancel.
			// This is probably unkind, but short of this there is no way to
			// ensure
			// the UI is up-to-date with the build results
			// monitor.subTask(ManagedMakeMessages
			// .getResourceString(REFRESH));
			monitor.subTask(AutotoolsPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				monitor.subTask(AutotoolsPlugin
						.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}

		// Report either the success or failure of our mission
		buf = new StringBuffer();
		if (errMsg != null && errMsg.length() > 0) {
			String errorDesc = AutotoolsPlugin
					.getResourceString("MakeGenerator.generation.error"); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append(errorDesc);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			rc = IStatus.ERROR;
		} else if (proc.exitValue() >= 1 || proc.exitValue() < 0) {
			// We have an invalid return code from configuration.
			String[] errArg = new String[2];
			errArg[0] = Integer.toString(proc.exitValue());
			errArg[1] = commandPath.toString();
			errMsg = AutotoolsPlugin.getFormattedString(
					"MakeGenerator.config.error", errArg); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append(AutotoolsPlugin.getResourceString("MakeGenerator.generation.error")); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			if (proc.exitValue() == 1)
				rc = IStatus.WARNING;
			else
			    rc = IStatus.ERROR;
		} else {
			// Report a successful build
			String successMsg = 
				AutotoolsPlugin.getResourceString("MakeGenerator.success"); //$NON-NLS-1$
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			rc = IStatus.OK;
		}

		// Write message on the console
		consoleOutStream.write(buf.toString().getBytes());
		consoleOutStream.flush();

		// // Generate any error markers that the build has discovered
		// monitor.subTask(ManagedMakeMessages
		// .getResourceString(MARKERS));
		// epm.reportProblems();
		consoleOutStream.close();
		
		// TODO: For now, add a marker with our generated error message.
		// In the future, we might add an error parser to do this properly
		// and give the actual error line, etc..
		if (rc == IStatus.ERROR) {
			addMarker(project, -1, errMsg, SEVERITY_ERROR_BUILD, null);
			// Mark the configuration as needing a full rebuild.
			cfg.setRebuildState(true);
		}
		
		return rc;
	}
	
	// Run an autotools script (e.g. configure, autogen.sh, config.status).
	private int runScript(IPath commandPath, IPath runPath, String[] args,
			String jobDescription, String errMsg, IConsole console, 
			boolean consoleStart) throws BuildException, CoreException,
			NullPointerException, IOException {
		// TODO: Figure out what this next stuff is used for
		// //try to resolve the build macros in the builder command
		// try{
		// String resolved =
		// ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
		// configCmd,
		// "", //$NON-NLS-1$
		// " ", //$NON-NLS-1$
		// IBuildMacroProvider.CONTEXT_CONFIGURATION,
		// info.getDefaultConfiguration());
		// if((resolved = resolved.trim()).length() > 0)
		// configCmd = resolved;
		// } catch (BuildMacroException e){
		// }

		int rc = IStatus.OK;
		
		removeAllMarkers(project);
		
		// We want to run the script via the shell command.  So, we add the command
		// script as the first argument and expect "sh" to be on the runtime path.
		// Any other arguments are placed after the script name.
		String[] configTargets = null;
		if (args == null)
			configTargets = new String[1];
		else {
			configTargets = new String[args.length+1];
			System.arraycopy(args, 0, configTargets, 1, args.length);
		}
		configTargets[0] = commandPath.toOSString();
		
		String[] msgs = new String[2];
		msgs[0] = commandPath.toString();
		msgs[1] = project.getName();
		monitor.subTask(AutotoolsPlugin.getFormattedString(
				"MakeGenerator.make.message", msgs)); //$NON-NLS-1$


		ConsoleOutputStream consoleOutStream = null;
		StringBuffer buf = new StringBuffer();

		// Launch command - main invocation
		if (consoleStart)
			console.start(project);
		consoleOutStream = console.getOutputStream();
		String[] consoleHeader = new String[3];

		consoleHeader[0] = jobDescription;
		consoleHeader[1] = cfg.getName();
		consoleHeader[2] = project.getName();
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		buf.append(jobDescription);
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

		if (!cfg.isSupported()) {
			String msgArgs[] = new String[1];
			msgArgs[0] = cfg.getName();
			buf.append(AutotoolsPlugin.getFormattedString("MakeGenerator.unsupportedConfig", msgArgs));
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
		}
		consoleOutStream.write(buf.toString().getBytes());
		consoleOutStream.flush();

		// Get a launcher for the config command
		CommandLauncher launcher = new CommandLauncher();
		// Set the environment
		IEnvironmentVariable variables[] = ManagedBuildManager
				.getEnvironmentVariableProvider().getVariables(cfg, true);
		String[] env = null;
		ArrayList<String> envList = new ArrayList<String>();
		if (variables != null) {
			for (int i = 0; i < variables.length; i++) {
				envList.add(variables[i].getName()
						+ "=" + variables[i].getValue()); //$NON-NLS-1$
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}

		// // Hook up an error parser manager
		// String[] errorParsers =
		// info.getDefaultConfiguration().getErrorParserList();
		// ErrorParserManager epm = new ErrorParserManager(project, topBuildDir,
		// this, errorParsers);
		// epm.setOutputStream(consoleOutStream);
		OutputStream stdout = consoleOutStream;
		OutputStream stderr = consoleOutStream;

		launcher.showCommand(true);
		// Run the shell script via shell command.
		Process proc = launcher.execute(new Path(SHELL_COMMAND), configTargets, env,
				runPath);
		if (proc != null) {
			try {
				// Close the input of the process since we will never write to
				// it
				proc.getOutputStream().close();
			} catch (IOException e) {
			}

			if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
					monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
				errMsg = launcher.getErrorMessage();
			}

			// Force a resync of the projects without allowing the user to
			// cancel.
			// This is probably unkind, but short of this there is no way to
			// ensure
			// the UI is up-to-date with the build results
			// monitor.subTask(ManagedMakeMessages
			// .getResourceString(REFRESH));
			monitor.subTask(AutotoolsPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				monitor.subTask(AutotoolsPlugin
						.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}

		// Report either the success or failure of our mission
		buf = new StringBuffer();
		if (errMsg != null && errMsg.length() > 0) {
			String errorDesc = AutotoolsPlugin
					.getResourceString("MakeGenerator.generation.error"); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append(errorDesc);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			rc = IStatus.ERROR;
		} else if (proc.exitValue() >= 1 || proc.exitValue() < 0) {
			// We have an invalid return code from configuration.
			String[] errArg = new String[2];
			errArg[0] = Integer.toString(proc.exitValue());
			errArg[1] = commandPath.toString();
			errMsg = AutotoolsPlugin.getFormattedString(
					"MakeGenerator.config.error", errArg); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append(AutotoolsPlugin.getResourceString("MakeGenerator.generation.error")); //$NON-NLS-1$
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			if (proc.exitValue() == 1)
				rc = IStatus.WARNING;
			else
			    rc = IStatus.ERROR;
		} else {
			// Report a successful build
			String successMsg = 
				AutotoolsPlugin.getResourceString("MakeGenerator.success"); //$NON-NLS-1$
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			rc = IStatus.OK;
		}

		// Write message on the console
		consoleOutStream.write(buf.toString().getBytes());
		consoleOutStream.flush();

		// // Generate any error markers that the build has discovered
		// monitor.subTask(ManagedMakeMessages
		// .getResourceString(MARKERS));
		// epm.reportProblems();
		consoleOutStream.close();
		
		// TODO: For now, add a marker with our generated error message.
		// In the future, we might add an error parser to do this properly
		// and give the actual error line, etc..
		if (rc == IStatus.ERROR) {
			addMarker(project, -1, errMsg, SEVERITY_ERROR_BUILD, null);
			// Mark the configuration as needing a full rebuild.
			cfg.setRebuildState(true);
		}
		
		return rc;
	}
	
	private Node createTargetElement(Document doc, IMakeTarget target) {
		Element targetElem = doc.createElement(TARGET_ELEMENT);
		targetElem.setAttribute(TARGET_ATTR_NAME, target.getName());
		targetElem.setAttribute(TARGET_ATTR_ID, target.getTargetBuilderID());
		targetElem.setAttribute(TARGET_ATTR_PATH, target.getContainer().getProjectRelativePath().toString());
		Element elem = doc.createElement(TARGET_COMMAND);
		targetElem.appendChild(elem);
		elem.appendChild(doc.createTextNode(target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make"))); //$NON-NLS-1$

		String targetAttr = target.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, null);
		if ( targetAttr != null) {
			elem = doc.createElement(TARGET_ARGUMENTS);
			elem.appendChild(doc.createTextNode(targetAttr));
			targetElem.appendChild(elem);
		}

		targetAttr = target.getBuildAttribute(IMakeTarget.BUILD_TARGET, null);
		if (targetAttr != null) {
			elem = doc.createElement(TARGET);
			elem.appendChild(doc.createTextNode(targetAttr));
			targetElem.appendChild(elem);
		}

		elem = doc.createElement(TARGET_STOP_ON_ERROR);
		elem.appendChild(doc.createTextNode(new Boolean(target.isStopOnError()).toString()));
		targetElem.appendChild(elem);

		elem = doc.createElement(TARGET_USE_DEFAULT_CMD);
		elem.appendChild(doc.createTextNode(new Boolean(target.isDefaultBuildCmd()).toString()));
		targetElem.appendChild(elem);
		
		elem = doc.createElement(TARGET_RUN_ALL_BUILDERS);
		elem.appendChild(doc.createTextNode(new Boolean(target.runAllBuilders()).toString()));
		targetElem.appendChild(elem);
		return targetElem;
	}

	/**
	 * This output method saves the information into the .cdtproject metadata file.
	 * 
	 * @param doc
	 * @throws CoreException
	 */
	private void translateDocumentToCDTProject(Document doc) throws CoreException {
		ICDescriptor descriptor;
		descriptor = CCorePlugin.getDefault().getCProjectDescription(getProject(), true);

		Element rootElement = descriptor.getProjectData(MAKE_TARGET_KEY);

		//Nuke the children since we are going to write out new ones
		NodeList kids = rootElement.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			rootElement.removeChild(kids.item(i));
			i--;
		}

		//Extract the root of our temporary document
		Node node = doc.getFirstChild();
		if (node.hasChildNodes()) {
			//Create a copy which is a part of the new document
			Node appendNode = rootElement.getOwnerDocument().importNode(node, true);
			//Put the copy into the document in the appropriate location
			rootElement.appendChild(appendNode);
		}
		//Save the results
		descriptor.saveProjectData();
	}
	

	protected class MakeTargetComparator implements Comparator<Object> {
		public int compare(Object a, Object b) {
			IMakeTarget make1 = (IMakeTarget)a;
			IMakeTarget make2 = (IMakeTarget)b;
			return make1.getName().compareToIgnoreCase(make2.getName());
		}
		
	}

	/**
	 * This method parses the given Makefile and produces MakeTargets for all targets so the
	 * end-user can access them from the MakeTargets popup-menu.
	 * 
	 * @param makefileFile the Makefile to parse
	 * @throws CoreException
	 */
	private void addMakeTargetsToManager(File makefileFile) throws CoreException {
		// We don't bother if the Makefile wasn't created successfully.
		if (!makefileFile.exists())
			return;
		
		checkCancel();
		if (monitor == null)
			monitor = new NullProgressMonitor();
		String statusMsg = AutotoolsPlugin.getResourceString("MakeGenerator.refresh.MakeTargets");	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		
		IMakeTargetManager makeTargetManager = 
			MakeCorePlugin.getDefault().getTargetManager();
		
		IMakefile makefile = MakeCorePlugin.createMakefile(makefileFile.toURI(), false, null);
		ITargetRule[] targets = makefile.getTargetRules();
		ITarget target = null;
		Map<String, IMakeTarget> makeTargets = new HashMap<String, IMakeTarget>(); // use a HashMap so duplicate names are handled
		for (int i = 0; i < targets.length; i++) {
			target = targets[i].getTarget();
			String targetName = target.toString();
			if (!isValidTarget(targetName, makeTargetManager))
				continue;
			try {
				IMakeTarget makeTarget = makeTargetManager.createTarget(
						project, targetName, "org.eclipse.linuxtools.cdt.autotools.builder1"); //$NON-NLS-1$
				makeTarget.setContainer(project);
				makeTarget.setStopOnError(true);
				makeTarget.setRunAllBuilders(false);
				makeTarget.setUseDefaultBuildCmd(true);

				makeTarget.setBuildAttribute(GENERATED_TARGET, "true");
				makeTarget.setBuildAttribute(IMakeTarget.BUILD_TARGET,
						targetName);

				makeTarget.setBuildAttribute(IMakeTarget.BUILD_LOCATION,
						buildDir);
				makeTargets.put(makeTarget.getName(), makeTarget);
			} catch (CoreException e) {
				// Duplicate target.  Ignore.
			}
		}
		
		IMakeTarget[] makeTargetArray = new IMakeTarget[makeTargets.size()];
		Collection<IMakeTarget> values = makeTargets.values();
		ArrayList<IMakeTarget> valueList = new ArrayList<IMakeTarget>(values);
		valueList.toArray(makeTargetArray);
		MakeTargetComparator compareMakeTargets = new MakeTargetComparator();
		Arrays.sort(makeTargetArray, compareMakeTargets);
		
		// At this point, we could use IMakeTargetManager to add new targets and
		// remove old ones that no longer exist.  Unfortunately, the Makefile
		// MakeTarget class is rather inefficient at this and rewrites the .cproject
		// file for every change made to the set of MakeTargets.  In our case,
		// we are better off performing the rewrite of the .cproject file ourselves
		// and then restarting the MakeTargetManager manually.  This will cause it
		// to read the .cproject file to get the MakeTargets again but there is
		// only 1 read and 1 write vs thousands of potential operations.
		// each 
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			//This should never happen.
			throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), -1,
					"Error creating new XML storage document", ex)); //$NON-NLS-1$
		}
		Element targetsRootElement = doc.createElement(BUILD_TARGET_ELEMENT);
		doc.appendChild(targetsRootElement);
		for (int i = 0; i < makeTargetArray.length; i++) {
			IMakeTarget t = (IMakeTarget)makeTargetArray[i];
			targetsRootElement.appendChild(createTargetElement(doc, t));
		}
		
		try {
			translateDocumentToCDTProject(doc);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// To perform the shutdown and restart, we know that MakeCorePlugin's
		// MakeTargetManager has two special methods that are not exposed as
		// part of IMakeTargetManager.  We use reflection to access them and
		// avoid referencing an internal CDT class: MakeTargetManager.
		Method shutdown;
		Method startup;
		try {
			shutdown = makeTargetManager.getClass().getMethod("shutdown", (Class<?>[])null);
			startup = makeTargetManager.getClass().getMethod("startup", (Class<?>[])null);
			if (shutdown != null && startup != null) {
				shutdown.invoke(makeTargetManager, (Object[])null);
				startup.invoke(makeTargetManager, (Object[])null);
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isValidTarget(String targetName, IMakeTargetManager makeTargetManager) {
		return !(targetName.endsWith("-am") //$NON-NLS-1$
				|| targetName.endsWith("PROGRAMS") //$NON-NLS-1$
				|| targetName.endsWith("-generic") //$NON-NLS-1$
				|| (targetName.indexOf('$') >= 0)
				|| (targetName.charAt(0) == '.')
				|| targetName.equals(targetName.toUpperCase()));
	}
	
	// Turn the string into an array.
	private String[] makeArray(String string) {
		string = string.trim();
		char[] array = string.toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			boolean needsToAdd = true;
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
					needsToAdd = false; // skip it
				}
			}
			if (c == ' ' && !inComment) {
				if (buffer.length() > 0){
					String str = buffer.toString().trim();
					if(str.length() > 0){
						aList.add(str);
					}
				}
				buffer = new StringBuilder();
			} else {
				if (needsToAdd)
					buffer.append(c);
			}
		}
		if (buffer.length() > 0){
			String str = buffer.toString().trim();
			if(str.length() > 0){
				aList.add(str);
			}
		}
		return (String[])aList.toArray(new String[aList.size()]);
	}

}