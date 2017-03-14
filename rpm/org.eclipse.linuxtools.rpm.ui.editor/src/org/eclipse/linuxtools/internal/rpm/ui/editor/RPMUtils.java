/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for RPM UI Editor related things.
 *
 */
public class RPMUtils {

	/**
	 * Utility classes should not have a public or default constructor.
	 */
	private RPMUtils() {
	}

	/**
	 * Show an error dialog.
	 *
	 * @param shell
	 *            A valid shell
	 * @param title
	 *            The error dialog title
	 * @param message
	 *            The message to be displayed.
	 */
	public static void showErrorDialog(final Shell shell, final String title, final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> MessageDialog.openError(shell, title, message));
	}

	/**
	 * Check if the line passed in is a valid URL.
	 *
	 * @param line
	 *            The line to check if is a valid URL.
	 * @return True if valid URL, false otherwise.
	 */
	public static boolean isValidUrl(String line) {
		try {
			new URL(line);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * Get the file from the URL if any.
	 *
	 * @param url
	 *            The URL to get the file from.
	 * @return Return the filename.
	 */
	public static String getURLFilename(String url) {
		String rc = ""; //$NON-NLS-1$

		try {
			// URL#getPath will ignore any queries after the filename
			String fileName = new URL(url).getPath();
			int lastSegment = fileName.lastIndexOf('/') + 1;
			rc = fileName.substring(lastSegment).trim();
		} catch (IndexOutOfBoundsException | MalformedURLException e) {
			SpecfileLog.logError(e);
		}

		return rc;
	}

	/**
	 * Check if the file exists within the current project. It will first check
	 * the root of the project and then the sources. If the file cannot be found
	 * in either, return false. An empty file name would immediately return
	 * false.
	 *
	 * @param original
	 *            A file in the project.
	 * @param fileName
	 *            The file name being searched.
	 *
	 * @return True if the file exists.
	 */
	public static boolean fileExistsInSources(IFile original, String fileName) {
		if (fileName.trim().isEmpty()) {
			return false;
		}
		IContainer container = original.getParent();
		IResource resourceToOpen = container.findMember(fileName);
		IFile file = null;

		if (resourceToOpen == null) {
			IResource sourcesFolder = container.getProject().findMember("SOURCES"); //$NON-NLS-1$
			file = container.getFile(new Path(fileName));
			if (sourcesFolder != null) {
				file = ((IFolder) sourcesFolder).getFile(new Path(fileName));
			}
			if (!file.exists()) {
				return false;
			}
		}

		return true;
	}

	public static String getSourceOrPatchValue(Specfile spec, String patchOrSourceName) {
		String value = null;
		Pattern p = Pattern.compile("(source|patch)(\\d*)"); //$NON-NLS-1$
		Matcher m = p.matcher(patchOrSourceName);

		if (m.matches()) {
			String digits = m.group(2);

			SpecfileSource source = null;
			int number = -1;

			if (digits != null && digits.isEmpty()) {
				number = 0;
			} else if (digits != null && !digits.isEmpty()) {
				number = Integer.parseInt(digits);
			}

			if (number != -1) {
				if (m.group(1).equals("source")) {//$NON-NLS-1$
					source = spec.getSource(number);
				} else if (m.group(1).equals("patch")) {//$NON-NLS-1$
					source = spec.getPatch(number);
				}

				if (source != null) {
					value = source.getFileName();
				}
			}
		}
		return value;
	}

	public static String getMacroValueFromMacroList(String macroName) {
		String value = null;
		if (Activator.getDefault().getRpmMacroList().findKey("%" + macroName)) { //$NON-NLS-1$
			String currentConfig = Activator.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_MACRO_HOVER_CONTENT);
			// Show content of the macro according with the configuration set
			// in the macro preference page.
			if (currentConfig.equals(PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION)) {
				value = Activator.getDefault().getRpmMacroList().getValue(macroName);
			} else {
				value = RpmMacroProposalsList.getMacroEval("%" + macroName); //$NON-NLS-1$
			}
		}
		return value;
	}
}
