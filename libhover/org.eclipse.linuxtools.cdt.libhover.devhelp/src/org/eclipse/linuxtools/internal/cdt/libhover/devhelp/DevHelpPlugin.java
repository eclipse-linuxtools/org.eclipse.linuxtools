/*******************************************************************************
 * Copyright (c) 2012, 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLibrary;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.LibHoverMessages;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class DevHelpPlugin extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.cdt.libhover.devhelp"; //$NON-NLS-1$
	private static final String REGENERATE_MSG = "Libhover.Devhelp.Regenerate.msg"; //$NON-NLS-1$

	// The shared instance
	private static DevHelpPlugin plugin;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Job k = new DevhelpStartupJob(LibHoverMessages.getString(REGENERATE_MSG)) ;
		k.schedule();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DevHelpPlugin getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
		// do nothing...we just want start to get called and reparse the devhelp
		// directory
	}

	/**
	 * Job used to load devhelp data on startup.
	 *
	 */
	private static class DevhelpStartupJob extends Job {

		public DevhelpStartupJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IPreferenceStore ps = DevHelpPlugin.getDefault()
					.getPreferenceStore();
			ParseDevHelp.DevHelpParser p = new ParseDevHelp.DevHelpParser(
					ps.getString(PreferenceConstants.DEVHELP_DIRECTORY));
			LibHoverInfo hover = p.parse(monitor);
			// Update the devhelp library info if it is on library list
			Collection<LibHoverLibrary> libs = LibHover.getLibraries();
			for (LibHoverLibrary l : libs) {
				if (l.getName().equals("devhelp")) { //$NON-NLS-1$
					l.setHoverinfo(hover);
					break;
				}
			}
			try {
				// Now, output the LibHoverInfo for caching later
				IPath location = LibhoverPlugin.getDefault()
						.getStateLocation().append("C"); //$NON-NLS-1$
				File ldir = new File(location.toOSString());
				ldir.mkdir();
				location = location.append("devhelp.libhover"); //$NON-NLS-1$
				try (FileOutputStream f = new FileOutputStream(
						location.toOSString());
						ObjectOutputStream out = new ObjectOutputStream(f)) {
					out.writeObject(hover);
				}
				monitor.done();
			} catch (IOException e) {
				monitor.done();
				return new Status(IStatus.ERROR, DevHelpPlugin.PLUGIN_ID,
						e.getLocalizedMessage(), e);
			}

			return Status.OK_STATUS;
		}

	};

}