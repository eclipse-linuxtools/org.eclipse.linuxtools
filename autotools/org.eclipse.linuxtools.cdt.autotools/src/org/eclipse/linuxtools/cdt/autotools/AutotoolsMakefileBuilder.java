/*******************************************************************************
 * Copyright (c) 2002, 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;


/**
 * This is the incremental builder associated with a managed build project. It dynamically 
 * decides the makefile generator it wants to use for a specific target.
 * 
 * @since 1.2 
 */
public class AutotoolsMakefileBuilder extends CommonBuilder {
	public static final String BUILDER_NAME = "genmakebuilder"; //$NON-NLS-1$
	public static final String BUILDER_ID = AutotoolsPlugin.getUniqueIdentifier() + "." + BUILDER_NAME; //$NON-NLS-1$
	public static final String MANAGED_BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + "." + BUILDER_NAME; //$NON-NLS-1$
	public static final String AUTOTOOLS_CONFIG_ID = AutotoolsPlugin.getUniqueIdentifier() + ".configuration.build"; //$NON-NLS-1$
	public static final String AUTOTOOLS_PROJECT_TYPE_ID = AutotoolsPlugin.getUniqueIdentifier() + ".projectType"; //$NON-NLS-1$
	
	private static final String BUILD_FINISHED = "AutotoolsMakefileBuilder.message.finished";	//$NON-NLS-1$
	private static final String TYPE_CLEAN = "AutotoolsMakefileBuilder.type.clean";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = "AutotoolsMakefileBuilder.message.console.header";	//$NON-NLS-1$

	private static final String EMPTY_STRING = new String();
	public final String WHITESPACE = " ";	//$NON-NLS-1$

	protected boolean buildCalled;
	
	private String makeTargetName;
	
	public static String getBuilderId() {
		return BUILDER_ID;
	}
	
	public static boolean hasTargetBuilder(IProject project) {
		try {
			// When a project is converted to an Autotools project, we
			// replace the ManagedMake builder with a special one that
			// handles MakeTargets.  If a project is brought into Eclipse and
			// uses the New Project Wizard to create a ManagedMake project that
			// is of type: Autotools, this added step is not done.  If we know
			// we have an Autotools project from the configuration id, then
			// we should add the builder now.  We also should replace the
			// default ManagedMake scanner provider with the Autotools one,
			// then return true.
			if (project.getNature(ManagedCProjectNature.MNG_NATURE_ID) != null) {
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
				IManagedProject m = info.getManagedProject();
				if (m != null && m.getProjectType().getId().equals(AUTOTOOLS_PROJECT_TYPE_ID)) {
					AutotoolsProjectNature.addAutotoolsBuilder(project, new NullProgressMonitor());
					AutotoolsPlugin.verifyScannerInfoProvider(project);
					return true;
				}
			}
		} catch (CoreException e) {
			// Don't care...fall through to not found.
		} catch (Exception f) {
			// Don't care...fall through to not found.
		}
		// Otherwise not found.
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject[] results = null;
		IProject project = getProject();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedBuilderMakefileGenerator generator = null;
		try {
			// Figure out the working directory for the build and make sure there is a makefile there
			// If not, mark a rebuild is required so that configuration will get
			// invoked.
			IWorkspace workspace = project.getWorkspace();
			if (workspace != null) {
				IWorkspaceRoot root = workspace.getRoot();
				if (root != null) {
					if (info.getDefaultConfiguration() == null)
						return null;
					generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
					generator.initialize(getProject(), info, monitor);
					IPath buildDir = project.getLocation().append(generator.getBuildWorkingDir());
					IPath makefilePath = buildDir.append(generator.getMakefileName());
					IFile makefile = root.getFileForLocation(makefilePath);
					if (makefile == null || !makefile.exists()) {
						info.setRebuildState(true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			makeTargetName = (String)args.get("org.eclipse.cdt.make.core.build.target.inc"); //$NON-NLS-1$
			
			buildCalled = true;
			/**
			 * @see IncrementalProjectBuilder#build
			 */
//			fBuildSet.start(this);

			// Hijack the build.  This is because the CommonBuilder code will
			// try and create builders for a MakeTarget build.  We don't want
			// that because this will default to using the GnuMakefileGenerator
			// which fails.  We want to use our Autotools MakeGenerator and
			// perform a make from the top-level.
			if(VERBOSE)
				outputTrace(project.getName(), ">>build requested, type = " + kind); //$NON-NLS-1$
			IConfiguration cfg = info.getDefaultConfiguration();

			// Assemble the information needed to generate the targets
			String errMsg;
			String prebuildStep = cfg.getPrebuildStep();
			try{
				//try to resolve the build macros in the prebuild step
				prebuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						prebuildStep,
						EMPTY_STRING,
						WHITESPACE,
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
			} catch (BuildMacroException e){
			}
			prebuildStep = prebuildStep.trim(); // Remove leading and trailing whitespace (and control characters)

			String postbuildStep = cfg.getPostbuildStep();
			try{
				//try to resolve the build macros in the postbuild step
				postbuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						postbuildStep,
						EMPTY_STRING,
						WHITESPACE,
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
					
			} catch (BuildMacroException e){
			}
			postbuildStep = postbuildStep.trim(); // Remove leading and trailing whitespace (and control characters)
			String preannouncebuildStep = cfg.getPreannouncebuildStep();
			String postannouncebuildStep = cfg.getPostannouncebuildStep();

			IConsole console = null;
			ConsoleOutputStream consoleOutStream = null;
			CommandLauncher launcher = null;
			String[] env = null;
			Process proc = null;
			OutputStream stdout = null;
			OutputStream stderr = null;
			
			// If we have a prebuild or postbuild step, set up a command launcher to use.
			if (!prebuildStep.equals("") || !postbuildStep.equals("")) {
				console = CCorePlugin.getDefault().getConsole("org.eclipse.linuxtools.cdt.autotools.buildStepsConsole"); // $NON-NLS-1$
				console.start(project);
				consoleOutStream = console.getOutputStream();
				stdout = consoleOutStream;
				stderr = consoleOutStream;
				launcher = new CommandLauncher();
				// Set the environment
				IEnvironmentVariable variables[] = ManagedBuildManager
						.getEnvironmentVariableProvider().getVariables(cfg, true);
				ArrayList envList = new ArrayList();
				if (variables != null) {
					for (int i = 0; i < variables.length; i++) {
						envList.add(variables[i].getName()
								+ "=" + variables[i].getValue()); //$NON-NLS-1$
					}
					env = (String[]) envList.toArray(new String[envList.size()]);
				}
			}
			
			// Check for a prebuild step and execute it if it exists.
			if (!prebuildStep.equals("")) {
				monitor.subTask(preannouncebuildStep);

				StringBuffer buffer = new StringBuffer();
				buffer.append(preannouncebuildStep);
				buffer.append(System.getProperty("lineSeparator", "\n")); // $NON-NLS-1$ // $NON-NLS-2$

				try {
					consoleOutStream.write(buffer.toString().getBytes());
					consoleOutStream.flush();
				} catch (IOException e) {
					// do nothing
				}

				launcher.showCommand(true);
				String[] tmp = prebuildStep.split("\\s");
				String[] cmdargs = (String[]) Arrays.copyOfRange(tmp, 1, tmp.length);
				proc = launcher.execute(new Path(tmp[0]), cmdargs, env,
						project.getLocation().append(generator.getBuildWorkingDir()));
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
				}
			}

			// Perform build
			
			IBuilder builders[] = new IBuilder[1];
			// Hijack the builder itself so that instead of ManagedMake
			// policy of defaulting the build path to the configuration name,
			// we get the build occurring in the builddir configure tool setting.
			builders[0] = new AutotoolsBuilder(cfg.getEditableBuilder(), project);
			builders[0].setBuildPath(project.getLocation().append(generator.getBuildWorkingDir()).toOSString());
			builders[0].setAutoBuildEnable(true);
			builders[0].setCleanBuildEnable(true);
			IProject[] projects = build(kind, project, builders, true, monitor);

			if(VERBOSE)
				outputTrace(project.getName(), "<<done build requested, type = " + kind); //$NON-NLS-1$

			results = projects;
			buildCalled = false;
			
			// Check for a postbuild step and execute it if it exists.
			if (!postbuildStep.equals("")) {
				monitor.subTask(postannouncebuildStep);
				StringBuffer buffer = new StringBuffer();
				buffer.append(postannouncebuildStep);
				buffer.append(System.getProperty("lineSeparator", "\n")); // $NON-NLS-1$ // $NON-NLS-2$

				try {
					consoleOutStream.write(buffer.toString().getBytes());
					consoleOutStream.flush();
				} catch (IOException e) {
					// do nothing
				}
				String[] tmp = postbuildStep.split("\\s");
				String[] cmdargs = (String[]) Arrays.copyOfRange(tmp, 1, tmp.length);
				proc = launcher.execute(new Path(tmp[0]), cmdargs, env,
						project.getLocation().append(generator.getBuildWorkingDir()));
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
				}
			}

		}
		return results;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// See what type of cleaning the user has set up in the
		// build properties dialog.
		String cleanDelete = null;
		try {
			cleanDelete = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
		} catch (CoreException ce) {
			// do nothing
		}
		
		if (cleanDelete != null && cleanDelete.equals(AutotoolsPropertyConstants.TRUE))
			removeBuildDir(monitor);
		else {
			IBuilder builders[] = new IBuilder[1];
			IProject project = getProject();
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration cfg = info.getDefaultConfiguration();
			IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(cfg);
			generator.initialize(getProject(), info, monitor);

			// Hijack the builder itself so that instead of ManagedMake
			// policy of defaulting the build path to the configuration name,
			// we get the build occurring in the builddir configure tool setting.
			builders[0] = new AutotoolsBuilder(cfg.getEditableBuilder(), project);
			builders[0].setBuildPath(project.getLocation().append(generator.getBuildWorkingDir()).toOSString());
			builders[0].setAutoBuildEnable(true);
			builders[0].setCleanBuildEnable(true);
			builders[0].setManagedBuildOn(false);
			try {
				build(CLEAN_BUILD, project, builders, true, monitor);
			} finally {
				builders[0].setManagedBuildOn(true);
				builders[0].setCleanBuildEnable(false);
			}
		}
	}

	protected void removeBuildDir(IProgressMonitor monitor) {
		try {
			// use brute force approach
			IProject project = getProject();
			IWorkspace workspace = AutotoolsPlugin.getWorkspace();
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration cfg = info.getDefaultConfiguration();
			IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(cfg);
			generator.initialize(getProject(), info, monitor);
			String buildPath = project.getFullPath().append(generator.getBuildWorkingDir()).toOSString();
			IResource rc = workspace.getRoot().findMember(buildPath);
			if (rc == null || rc.getType() != IResource.FOLDER)
				return;
			IFolder buildDir = (IFolder)rc;
			String status = AutotoolsPlugin.getFormattedString("AutotoolsMakefileBuilder.message.clean.deleting.output", new String[]{buildDir.getName()});	//$NON-NLS-1$
			monitor.subTask(status);
			workspace.delete(new IResource[]{buildDir}, true, monitor);
			StringBuffer buf = new StringBuffer();
			// write to the console

//			IConsole console = CCorePlugin.getDefault().getConsole();
//			console.start(getProject());
			// Get a build console for the project
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(project);
//			IConsole console = bInfo.getConsole();
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			consoleHeader[0] = AutotoolsPlugin.getResourceString(TYPE_CLEAN);
			consoleHeader[1] = cfg.getName();
			consoleHeader[2] = project.getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(AutotoolsPlugin.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			buf = new StringBuffer();
			// Report a successful clean
			String successMsg = AutotoolsPlugin.getFormattedString(BUILD_FINISHED, new String[]{project.getName()});
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		}  catch (IOException io) {
			//  Ignore console failures...
		}  catch (CoreException e) {
			//  Ignore console failures...		
		}
	}

	/* (non-javadoc)
	 * Answers an array of strings with the proper make targets
        * for a build with no custom prebuild/postbuild steps 
	 * 
	 * @param fullBuild
	 * @return
	 */
	protected String[] getTargets(int kind, IBuilder builder) {
		List args = new ArrayList();
		String buildTarget = "all";
		switch (kind) {
			case CLEAN_BUILD:
				// For a clean build, we use the clean make target set up by the user
				// in the build properties dialog.  Otherwise, we use the default for
				// an autotools project.
				String target = null;
				try {
					target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
				} catch (CoreException ce) {
					// do nothing
				}
				if (target == null)
					target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
				args.add(target);
				break;
			case FULL_BUILD:
				if (makeTargetName != null)
					buildTarget = makeTargetName;
			case AUTO_BUILD:
			case INCREMENTAL_BUILD:
				args.addAll(makeArrayList(buildTarget));
				break;
		}
		return (String[])args.toArray(new String[args.size()]);
	}

	// Turn the string into an array.
	ArrayList makeArrayList(String string) {
		string.trim();
		char[] array = string.toCharArray();
		ArrayList aList = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
				}
			}
			if (c == ' ' && !inComment) {
				aList.add(buffer.toString());
				buffer = new StringBuffer();
			} else {
				buffer.append(c);
			}
		}
		if (buffer.length() > 0)
			aList.add(buffer.toString());
		return aList;
	}
}
