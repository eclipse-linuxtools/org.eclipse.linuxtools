/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.gprof;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.gprof";

	// The shared instance
	private static Activator plugin;

	// The temporary cache directory
	private File cacheDir;

	private final static Object lock = new Object();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		File workspaceData = this.getStateLocation().toFile();
		cacheDir = new File(workspaceData, "cache");
		cacheDir.mkdirs();
		cacheDir.deleteOnExit();

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			plugin = null;
		}finally {
			delete(cacheDir);
			super.stop(context);			
		}
	}

	/**
	 * Delete recursively the file.
	 */
	private boolean delete(File toDel)
	{

		if (toDel.isDirectory()) {
			File[] files = toDel.listFiles();
			if (files != null) {
				for (int i=0; i<files.length; i++) {
					delete(files[i]);
				}
			}
		}

		return	toDel.delete();
	}

	/**
	 * @return the temporary cache directory
	 */
	public File getCacheDir() {
		return cacheDir;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the plugin unique identifier
	 */
	public static String getUniqueIdentifier()
	{
		return PLUGIN_ID;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Gets the absolute program path
	 * @param config
	 * @return the path to the binary, or null.
	 * @throws CoreException 
	 */
	public static String getAbsoluteProgramPath(ILaunchConfiguration config) throws CoreException
	{
		ICProject cproject =  CDebugUtils.getCProject(config);
		IPath programPath  = CDebugUtils.getProgramPath(config);
		if (cproject == null || programPath == null || programPath.isEmpty()) {
			return null;
		}
		if (!programPath.isAbsolute()) {
			IFile wsProgramPath = cproject.getProject().getFile(programPath);
			programPath = wsProgramPath.getLocation();
		}
		if (!programPath.toFile().exists()) {
			return null;
		}
		return programPath.toOSString();
	}

	/**
	 * Creates analysis dir.
	 * @param project
	 * @return the folder containing analysis files
	 * @throws CoreException
	 */
	public static IFolder createAnalysisDir(IProject project) throws CoreException
	{
		IFolder folder = project.getFolder("analysis");
		synchronized(lock)
		{
			if (!folder.exists()) folder.create(true,true,null);
		}
		return folder;
	}

	/**
	 * Creates the directory where to save analysis files.
	 * The name of the directory is a "human readable" timestamp
	 * @param timestamp
	 * @param folder
	 * @return a folder
	 * @throws CoreException if the folder can not be created
	 */
	public static IFolder createTimeStampDir(String timestamp, IFolder folder) throws CoreException
	{
		long l = Long.parseLong(timestamp);
		Date d = new Date(l);
		SimpleDateFormat f = new SimpleDateFormat();
		f.applyPattern("yy_MM_dd__HH'h'mm'm'ss's'SSS");
		timestamp = f.format(d);
		folder = folder.getFolder(timestamp);
		synchronized(lock)
		{
			if (!folder.exists()) folder.create(true,true,null);
		}
		return folder;
	}

}
