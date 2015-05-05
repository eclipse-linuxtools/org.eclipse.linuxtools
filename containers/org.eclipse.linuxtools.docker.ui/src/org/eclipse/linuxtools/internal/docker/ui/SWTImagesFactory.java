/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.graphics.Image;

public class SWTImagesFactory {
	// The plug-in registry
	private static ImageRegistry imageRegistry = Activator.getDefault()
			.getImageRegistry();

	// Sub-directory (under the package containing this class) where 16 color
	// images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL = new URL(Activator.getDefault().getBundle()
					.getEntry("/"), "icons/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			Activator.log(e);
		}
	}
	private static final String NAME_PREFIX = Activator.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	public static final String IMG_CONNECTION = NAME_PREFIX + "connection.gif"; //$NON-NLS-1$
	public static final String IMG_CREATE_CONTAINER = NAME_PREFIX
			+ "createcontainer.gif"; //$NON-NLS-1$
	public static final String IMG_CREATE_CONTAINERD = NAME_PREFIX
			+ "createcontainer_d.gif"; //$NON-NLS-1$
	public static final String IMG_FOLDER = NAME_PREFIX + "folder.gif"; //$NON-NLS-1$
	public static final String IMG_INSTANCE = NAME_PREFIX + "instance.gif"; //$NON-NLS-1$
	public static final String IMG_IMAGE = NAME_PREFIX + "image.gif"; //$NON-NLS-1$
	public static final String IMG_COLLAPSE_ALL = NAME_PREFIX
			+ "collapseall.gif"; //$NON-NLS-1$
	public static final String IMG_DOCKER_LARGE = NAME_PREFIX
			+ "docker_large.png"; //$NON-NLS-1$
	public static final String IMG_DOCKER_SMALL = NAME_PREFIX
			+ "docker_small.gif"; //$NON-NLS-1$
	public static final String IMG_FILTER = NAME_PREFIX + "filter_ps.gif"; //$NON-NLS-1$
	public static final String IMG_KILL = NAME_PREFIX + "kill.gif"; //$NON-NLS-1$
	public static final String IMG_KILLD = NAME_PREFIX + "killd.gif"; //$NON-NLS-1$
	public static final String IMG_PULL = NAME_PREFIX + "pull.gif"; //$NON-NLS-1$
	public static final String IMG_PUSH = NAME_PREFIX + "push.gif"; //$NON-NLS-1$
	public static final String IMG_BUILD = NAME_PREFIX + "build_exec.png"; //$NON-NLS-1$
	public static final String IMG_PAUSE = NAME_PREFIX + "suspend.gif"; //$NON-NLS-1$
	public static final String IMG_PAUSE_D = NAME_PREFIX + "suspendd.gif"; //$NON-NLS-1$
	public static final String IMG_REMOVE = NAME_PREFIX + "delete.gif"; //$NON-NLS-1$
	public static final String IMG_REMOVE_D = NAME_PREFIX + "delete_d.gif"; //$NON-NLS-1$
	public static final String IMG_RESUME = NAME_PREFIX + "resume.gif"; //$NON-NLS-1$
	public static final String IMG_RESUME_D = NAME_PREFIX + "resumed.gif"; //$NON-NLS-1$
	public static final String IMG_START = NAME_PREFIX + "running.gif"; //$NON-NLS-1$
	public static final String IMG_STARTD = NAME_PREFIX + "runningd.gif"; //$NON-NLS-1$
	public static final String IMG_STOP = NAME_PREFIX + "stopped.gif"; //$NON-NLS-1$
	public static final String IMG_STOPD = NAME_PREFIX + "stoppedd.gif"; //$NON-NLS-1$
	public static final String IMG_REFRESH = NAME_PREFIX + "refresh_tab.gif"; //$NON-NLS-1$
	public static final String IMG_REBOOT = NAME_PREFIX + "reboot.gif"; //$NON-NLS-1$
	public static final String IMG_REBOOTD = NAME_PREFIX + "rebootd.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CONNECTION = createManaged("",
			IMG_CONNECTION);
	public static final ImageDescriptor DESC_CREATE_CONTAINER = createManaged(
			"", IMG_CREATE_CONTAINER);
	public static final ImageDescriptor DESC_FOLDER = createManaged("",
			IMG_FOLDER);
	public static final ImageDescriptor DESC_INSTANCE = createManaged("",
			IMG_INSTANCE);
	public static final ImageDescriptor DESC_IMAGE = createManaged("",
			IMG_IMAGE);
	public static final ImageDescriptor DESC_COLLAPSE_ALL = createManaged("",
			IMG_COLLAPSE_ALL);
	public static final ImageDescriptor DESC_KILL = createManaged("", IMG_KILL);
	public static final ImageDescriptor DESC_KILLD = createManaged("",
			IMG_KILLD);
	public static final ImageDescriptor DESC_DOCKER_LARGE = createManaged("",
			IMG_DOCKER_LARGE);
	public static final ImageDescriptor DESC_DOCKER_SMALL = createManaged("",
			IMG_DOCKER_SMALL);
	public static final ImageDescriptor DESC_FILTER = createManaged("",
			IMG_FILTER);
	public static final ImageDescriptor DESC_PULL = createManaged("", IMG_PULL);
	public static final ImageDescriptor DESC_PUSH = createManaged("", IMG_PUSH);
	public static final ImageDescriptor DESC_BUILD = createManaged("",
			IMG_BUILD);
	public static final ImageDescriptor DESC_PAUSE = createManaged("",
			IMG_PAUSE);
	public static final ImageDescriptor DESC_PAUSE_D = createManaged("",
			IMG_PAUSE_D);
	public static final ImageDescriptor DESC_REMOVE = createManaged("",
			IMG_REMOVE);
	public static final ImageDescriptor DESC_REMOVE_D = createManaged("",
			IMG_REMOVE_D);
	public static final ImageDescriptor DESC_RESUME = createManaged("",
			IMG_RESUME);
	public static final ImageDescriptor DESC_RESUME_D = createManaged("",
			IMG_RESUME_D);
	public static final ImageDescriptor DESC_START = createManaged("",
			IMG_START);
	public static final ImageDescriptor DESC_STARTD = createManaged("",
			IMG_STARTD);
	public static final ImageDescriptor DESC_STOP = createManaged("", IMG_STOP);
	public static final ImageDescriptor DESC_STOPD = createManaged("",
			IMG_STOPD);
	public static final ImageDescriptor DESC_REBOOT = createManaged("",
			IMG_REBOOT);
	public static final ImageDescriptor DESC_REFRESH = createManaged("",
			IMG_REFRESH);
	public static final ImageDescriptor DESC_REBOOTD = createManaged("",
			IMG_REBOOTD);

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry,
			String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(
				prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			Activator.log(e);
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 * 
	 * @param action
	 *            - action
	 * @param type
	 *            - type of image descriptor
	 * @param relPath
	 *            - relative path
	 */
	public static void setImageDescriptors(IAction action, String type,
			String relPath) {
		if (relPath.startsWith(NAME_PREFIX))
			relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type, relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$

	}

	/**
	 * Helper method to access the image registry from the CUIPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

}
