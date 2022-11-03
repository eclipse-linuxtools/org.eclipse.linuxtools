/*******************************************************************************
 * Copyright (c) 2015, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverLibrary;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;

/**
 * Job used to load and parse Devhelp documentation and generate cached hover
 * help data for Libhover.
 */
public class DevHelpGenerateJob extends Job {

	private static final String LIBHOVER_FILE = "devhelp.libhover"; //$NON-NLS-1$

	private boolean force;

	/**
	 * Creates a job to regenerate the Libhover data for Devhelp documentation. The
	 * Libhover data is regenerated only if it is out of date compared to the
	 * installed Devhelp documentation.
	 * 
	 * @param force pass true to force regeneration the Devhelp Libhover data, even
	 *              if not out of date
	 */
	public DevHelpGenerateJob(boolean force) {
		super(Messages.DevHelpGenerateJob_GenerateJobName);
		this.force = force;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		// Find all Devhelp books in the set of paths from preferences
		IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
		String devhelpDirs = ps.getString(PreferenceConstants.DEVHELP_DIRECTORY);
		List<Path> books = ParseDevHelp.findAllDevhelpBooks(devhelpDirs);

		if (books.isEmpty()) {
			// No devhelp books found to process so quit now
			monitor.done();
			return Status.OK_STATUS;
		}

		// Try to find if there are any devhelp books that are newly installed/modified
		// compared to the modification date of existing libhover data, if it exists
		Path libhoverPath = Path.of(LibhoverPlugin.getDefault().getStateLocation().toOSString(), "C", //$NON-NLS-1$
				LIBHOVER_FILE);
		boolean outOfDate = true;
		try {
			if (Files.exists(libhoverPath)) {
				outOfDate = false;
				FileTime ltime = Files.getLastModifiedTime(libhoverPath);
				for (Path book : books) {
					// We check the time of the parent directory because the devhelp index itself
					// can have an ancient modification time if installed by RPM, for example
					FileTime ltimeBook = Files.getLastModifiedTime(book.getParent());
					if (ltime.compareTo(ltimeBook) < 0) {
						outOfDate = true;
						break;
					}
				}
			}
		} catch (IOException e) {
			// Unable to determine if any book is newer since we last generated libhover
			// data, but it's no big deal, we can just regenerate the libhover data anyway,
			// just to be safe
			outOfDate = true;
		}

		if (!outOfDate && !force) {
			// Our libhover data is up to date, and we are not asked to force regeneration
			// there is no need to do anything
			monitor.done();
			return Status.OK_STATUS;
		}

		ParseDevHelp.DevHelpParser p = new ParseDevHelp.DevHelpParser(books);
		LibHoverInfo hover = p.parse(monitor);
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;

		// Update the devhelp library info if it is on library list
		Collection<LibHoverLibrary> libs = LibHover.getLibraries();
		for (LibHoverLibrary l : libs) {
			if ("devhelp".equals(l.getName())) { //$NON-NLS-1$
				l.setHoverinfo(hover);
				break;
			}
		}
		try {
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			// Now, output the LibHoverInfo for caching later
			Path location = Path.of(LibhoverPlugin.getDefault().getStateLocation().toOSString(), "C"); //$NON-NLS-1$
			Files.createDirectories(location);
			try (OutputStream f = Files.newOutputStream(location.resolve(LIBHOVER_FILE));
					ObjectOutputStream out = new ObjectOutputStream(f)) {
				out.writeObject(hover);
			}
			monitor.done();
		} catch (NullPointerException e) {
			monitor.done();
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			monitor.done();
			return Status.error(e.getLocalizedMessage(), e);
		}

		return Status.OK_STATUS;
	}

}