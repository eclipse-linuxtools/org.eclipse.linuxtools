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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;


// This class is used to get and store information from the Autotools
// makefiles for a specific file in a specific Autotools project.
// It gets information by invoking make with special options
// to find out what occurs if the file in question has changed.
public class AutotoolsScannerInfo implements IScannerInfo {
	
	private IResource res;
	private IProject project;
	private IPath filePath;
	private String[] includePaths;
	@SuppressWarnings("unchecked")
	private HashMap definedSymbols;
	private boolean isDirty;
	private String compilationString;
	private String dirName;
	private HashSet<IScannerInfoChangeListener> listeners = new HashSet<IScannerInfoChangeListener>();
	
	public AutotoolsScannerInfo (IResource res) {
		this.res = res;
		this.project = res.getProject();
		this.filePath = res.getFullPath();
	}
	
	public void addListener(IScannerInfoChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(IScannerInfoChangeListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	// TODO: Should make a listener to watch all Makefiles up the
	// tree from the resource.  If any change, the include
	// paths and options passed to make the file in question
	// could change.
	public void setDirty (boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	private String buildFile (IPath filePath, IFile makefile, IManagedBuildInfo info) {
		String outString = "";
		IPath dir = makefile.getFullPath().removeLastSegments(1);
		IPath relFilePath = filePath.removeFirstSegments(dir.segmentCount());
		CommandLauncher launcher = new CommandLauncher();
		String[] env = null;
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = stdout;
		IPath makeCommandPath = new Path(info.getBuildCommand());
		ITool tool = info.getToolFromOutputExtension("status"); // $NON-NLS-1$
		IOption[] options = tool.getOptions();
		IPath runPath = null;
		boolean done = false;

		for (int i = 0; i < options.length && !done; ++i) {
			try {
				if (options[i].getValueType() == IOption.STRING) {
					String value = (String) options[i].getValue();
					String id = options[i].getId();
					if (id.indexOf("builddir") > 0) { // $NON-NLS-1$
						runPath = makefile.getProject().getLocation().append(value.trim());
						done = true;
					}
				}
			} catch (BuildException e) {
				// do nothing
			}
		}

		IProgressMonitor monitor = new NullProgressMonitor();
		String errMsg = null;
		String[] makeArgs = new String[3];
		makeArgs[0] = "-n"; // $NON-NLS-1$
		makeArgs[1] = "all"; // $NON-NLS-1$
		makeArgs[2] = "MAKE=make -W " + relFilePath.toOSString(); //$NON-NLS-1$

		try {
			Process proc = launcher.execute(makeCommandPath, makeArgs, env,
					runPath, new NullProgressMonitor());
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
				outString = stdout.toString();
			} else {
				errMsg = launcher.getErrorMessage();
			}
		} catch (CoreException e) {
			errMsg = e.getLocalizedMessage();
			AutotoolsPlugin.logErrorMessage(errMsg);
		}
		return outString;
	}
	
	private IFile getMakefile (IManagedBuildInfo info, IPath filePath) {
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IPath dir = filePath.removeLastSegments(1);
		IFile makefile = null;
		boolean done = false;
		IPath makefilePath = null;
		// Look starting at source file directory for the
		// Makefile.in or Makefile for the given file.
		while (!done) {
			makefilePath = dir.append("Makefile.in"); // $NON-NLS-1$
			makefile = root.getFile(makefilePath);
			if (makefile != null && makefile.exists()) {
				done = true;
				continue;
			}
			makefilePath = dir.append("Makefile"); // $NON-NLS-1$
			makefile = root.getFile(makefilePath);
			if (makefile != null && makefile.exists()) {
				done = true;
			}
			else {
				dir = dir.removeLastSegments(1);
				if (dir.lastSegment() == null)
					done = true;
			}
		}
		return makefile;
	}
	
	private String getCompilationString() {
		if (compilationString != null && !isDirty)
			return compilationString;
		String makeWEnabled = null;
		try {
			makeWEnabled = project.getPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W);
		} catch (CoreException e) {
			// do nothing
		}
		if (!(res instanceof IFile) ||
				makeWEnabled == null ||
				makeWEnabled.equals("false")) // $NON-NLS-1$
			return null;
		isDirty = false;
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile file = root.getFile(filePath);
		IFile makefile = null;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		// If the given file exists and we have a ManagedBuild, we may have
		// an Autotools project source file.
		if (file != null && file.exists() && info != null) {
			makefile = getMakefile(info, filePath);
		}
		// We now have the closest directory to the given path
		// that contains a Makefile.in or Makefile.  This Makefile
		// should contain a relative reference to the file in
		// question.  We now want to try and extract the include
		// path from the top-level make run with the assumption
		// that the file in question has been altered.
		if (makefile != null) {
			IPath dir = makefile.getFullPath().removeLastSegments(1);
			// Get relative name of file as we assume the Makefile will
			// refer to it this way.
			String out = buildFile(filePath, makefile, info);
			try {
				String regex1 = "^Making.*in.*" + dir.lastSegment(); // $NON-NLS-1$
				Pattern p = Pattern.compile(regex1, Pattern.MULTILINE);
				Matcher m = p.matcher(out);
				if (m.find()) {
					String substr2 = out.substring(m.end());
					String regex2 = "^make.*Entering directory.*`(.*)'"; // $NON-NLS-1$
					Pattern p2 = Pattern.compile(regex2, Pattern.MULTILINE);
					Matcher m2 = p2.matcher(substr2);
					if (m2.find()) {
						dirName = m2.group(1);
						String substr3 = substr2.substring(m2.start());
						String regex3 = "^.*gcc.*-I.*" + filePath.lastSegment(); // $NON-NLS-1$
						Pattern p3 = Pattern.compile(regex3, Pattern.MULTILINE);
						Matcher m3 = p3.matcher(substr3);
						if (m3.find())
							compilationString = substr3.substring(m3.start(),m3.end());
					}
				} else if (!out.equals("")) {
					compilationString = "";
				}
			} catch (IllegalStateException t) {
				// Mark as dirty for next time.
				isDirty = true;
			}
		}
		return compilationString;
	}
	
	public String[] getIncludePaths() {
		String[] pathArray = new String[0];
		if (project == null || filePath == null)
			return pathArray;
		if (includePaths != null && !isDirty && compilationString != null) {
			return includePaths;
		}
		ArrayList<String> pathList = new ArrayList<String>();
		String cs = getCompilationString();
		if (cs != null) {
			// Grab include paths specified via -I
			Pattern p4 = Pattern.compile(" -I");
			String[] tokens = p4.split(cs);
			for (int j = 1; j < tokens.length; ++j) {
				String x = tokens[j].trim();
				int firstSpace = x.indexOf(' ');
				if (firstSpace != -1) {
					x = x.substring(0, firstSpace);
				}
				if (x.charAt(0) == '/') {
					pathList.add(x);
				} else {
					IPath relPath = new Path(dirName);
					relPath = relPath.append(x);
					pathList.add(relPath.toOSString());
				}
			}
			// Grab include paths that are specified via -isystem
			Pattern p5 = Pattern.compile(" -isystem");
			tokens = p5.split(cs);
			for (int j = 1; j < tokens.length; ++j) {
				String x = tokens[j].trim();
				int firstSpace = x.indexOf(' ');
				if (firstSpace != -1) {
					x = x.substring(0, firstSpace);
				}
				if (x.charAt(0) == '/') {
					pathList.add(x);
				} else {
					IPath relPath = new Path(dirName);
					relPath = relPath.append(x);
					pathList.add(relPath.toOSString());
				}
			}
		}
		
		// The ManagedBuildManager is the normal default IScannerInfoProvider
		// for Managed Projects.  It has the ability to check gcc for the
		// default include paths and we don't want to lose that.  Append
		// the include paths from it to the end of our list which is dynamically
		// picking up the -I options.
		
		// TODO: Should we even allow the user-defined includePaths in settings?
		//       These will be picked up by the ManagedBuildManager.
		IScannerInfo info = (IScannerInfo)ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			String[] extraIncludePaths = info.getIncludePaths();
			for (int i = 0; i < extraIncludePaths.length; ++i) {
				pathList.add(extraIncludePaths[i]);
			}
		}

		includePaths = (String[])pathList.toArray(pathArray);
		
		// FIXME: Info has been updated.  Is this the best place to notify listeners?
		Iterator<IScannerInfoChangeListener> i = listeners.iterator();
		while (i.hasNext()) {
			IScannerInfoChangeListener listener = (IScannerInfoChangeListener)i.next();
			listener.changeNotification(project, this);
		}
		return includePaths;
	}
	
	@SuppressWarnings("unchecked")
	public Map getDefinedSymbols () {
		HashMap symbolMap = new HashMap();
		if (project == null || filePath == null)
			return symbolMap;
		if (definedSymbols != null && !isDirty && compilationString != null) {
			return definedSymbols;
		}
		// Extract -D directives from the compilation string.
		// TODO: Handle -U directives as well.
		String cs = getCompilationString();
		if (cs != null) {
			Pattern p4 = Pattern.compile("\\s-D([^\\s=]+)(?:=(\\\\\".*?\\\\\"|\\S*))?"); // $NON-NLS-1$
			Matcher m = p4.matcher(cs);
			while(m.find()) {
				String name = m.group(1);
				String value = m.group(2);
				if(value != null)
					symbolMap.put(name, value.replace("\\", "")); // $NON-NLS-1$ $NON-NLS-2$
				else
					symbolMap.put(name, "");
			}
		}
		// Add the defined symbols from ManagedBuildManager.  This will include
		// the builtin implicit defines from the compiler and the user-set
		// defines from settings.  Since these are added last, they will overwrite
		// existing defines fished out of the Makefiles above.  For the user-defined
		// symbols, this is the correct behavior.  For the implicit defines,
		// this is not; however, this is not valid programming behavior and
		// the compiler definitely warns against it.
		IScannerInfo info = (IScannerInfo)ManagedBuildManager.getBuildInfo(project);
		if (info != null) {
			Map builtinDefinedSymbols = info.getDefinedSymbols();
			symbolMap.putAll(builtinDefinedSymbols);
		}

		definedSymbols = symbolMap;
		return definedSymbols;
	}
	
	public void createIncludeChain(IFile include, IResource res) {
		try {
			// We store two different values.  The first only lasts for the 
			// the session and is the actual resource.  We prefer to work with
			// this form as it is quicker.  To handle persistence across
			// sessions, we also store a persistent copy of the resource's
			// path.  When the session is brought back again, we will recreate
			// the session chain again using the persistent data.
			include.setSessionProperty(AutotoolsPropertyConstants.OPEN_INCLUDE, res);
			include.setPersistentProperty(AutotoolsPropertyConstants.OPEN_INCLUDE_P, res.getLocation().toPortableString());
		} catch (CoreException e) {
			// Do nothing
		}
	}
	
	/**
	 * 
	 * @param resource the resource we are scanning
	 * @return the final resource in the include chain if one exists
	 * otherwise the resource itself
	 */
	public static IResource followIncludeChain(IResource resource) {
		IResource res = resource;
		try {
			boolean done = false;
			// In the case of include files, we chain to the file that
			// included the header file.  This may end up ultimately as
			// a source file which has build parameters such as flag
			// defines and a separate include path.
			while (!done) {
				while (res.getSessionProperty(AutotoolsPropertyConstants.OPEN_INCLUDE) != null)
					res = (IResource)res.getSessionProperty(AutotoolsPropertyConstants.OPEN_INCLUDE);
				String chainPath = res.getPersistentProperty(AutotoolsPropertyConstants.OPEN_INCLUDE_P);
				if (chainPath != null) {
					IPath location = Path.fromPortableString(chainPath);
					IResource next = res.getWorkspace().getRoot().getFileForLocation(location);
					res.setSessionProperty(AutotoolsPropertyConstants.OPEN_INCLUDE, next);
					res = next;
				} else
					done = true;
			}
		} catch (CoreException e) {
			// Do nothing
		} finally {
			resource = res;
		}
		return resource;
	}
}
