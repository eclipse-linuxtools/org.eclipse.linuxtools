/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.changelog.core.IFormatterChangeLogContrib;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;


/**
 * This class will manage extension related operations.
 *
 * @author klee
 *
 */
public final class ChangeLogExtensionManager {


    private static final ChangeLogExtensionManager EXM = new ChangeLogExtensionManager();


	// These are used as a simple cache so we don't have to iterate over
	// all extensions to formatContribution every time the action is invoked.
	private IConfigurationElement cachedPrefFormatter = null;

	private IConfigurationElement[] cachedInFileFormateters = null;

	private IExtensionPoint parserExtensions = null;

	private IExtensionPoint formatterExtensions = null;

	private IParserChangeLogContrib parserContributor = null;

	private IConfigurationElement formatterConfigElementToUse = null;

	private ChangeLogExtensionManager() {
		getParserContributions();
		getFormatterContributions();
	}

	public static ChangeLogExtensionManager getExtensionManager() {
		return EXM;
	}

	private void getFormatterContributions() {
		formatterExtensions = Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						"org.eclipse.linuxtools.changelog.core", "formatterContribution"); //$NON-NLS-1$
	}

	private void getParserContributions() {

		parserExtensions = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.linuxtools.changelog.core", "parserContribution"); //$NON-NLS-1$

	}

	public IParserChangeLogContrib getParserContributor(String editorName) {

		 if (parserExtensions != null) {
			IConfigurationElement[] elements = parserExtensions
					.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("parser") // $NON-NLS-1$
						&& (elements[i].getAttribute("editor") // $NON-NLS-1$
								.equals(editorName))) {
					//$NON-NLS-1$
					try {
						IConfigurationElement bob = elements[i];
						parserContributor = (IParserChangeLogContrib) bob
								.createExecutableExtension("class"); // $NON-NLS-1$
						return parserContributor;
					} catch (CoreException e) {
						ChangelogPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID,
										IStatus.ERROR, e.getMessage(), e));
					}

				}
			}
		}



		return null;
	}




	public IConfigurationElement getFormatterConfigElement() {
		return formatterConfigElementToUse;
	}

	/**
	 * Fetches formatterName formatter from extension, but if there exists a inline
	 * formatter for entryFileName, then it uses that inline formatter.
	 */
	public IFormatterChangeLogContrib getFormatterContributor(String entryFilePath, String formatterName) {


		// extract just file name;
		String fileName;

		int lastDir = entryFilePath.lastIndexOf('/');
		if ((lastDir >= 0) && (lastDir +1 <= entryFilePath.length()))
			fileName = entryFilePath.substring(lastDir + 1, entryFilePath.length());
		else
			fileName = entryFilePath;

		// We don't yet know which formatter to use
		formatterConfigElementToUse = null;

		// IFile file = null;

		if (formatterExtensions != null) {
			IConfigurationElement[] elements = formatterExtensions
					.getConfigurationElements();

			// cache the in-file formatters on the first run
			if (cachedInFileFormateters == null) {
				List<IConfigurationElement> inFileFormatters = new LinkedList<>();
				for (int i = 0; i < elements.length; i++) {
					IConfigurationElement formatterConfigElement = elements[i];
					if (formatterConfigElement.getName().equals("formatter") // $NON-NLS-1$
							&& formatterConfigElement.getAttribute("inFile") // $NON-NLS-1$
									.equalsIgnoreCase("true")) { // $NON-NLS-1$
						inFileFormatters.add(elements[i]);
					}
				}
				cachedInFileFormateters = inFileFormatters
						.toArray(new IConfigurationElement[] {});
			}

			// check if there is an in-file changelog formatter for the
			// currently
			// edited file
			for (int i = 0; i < cachedInFileFormateters.length; i++) {
				IConfigurationElement formatterConfigElement = cachedInFileFormateters[i];

				IConfigurationElement[] patternElementTmp = formatterConfigElement
						.getChildren();

				// error check
				if (patternElementTmp == null)
					continue;
				IConfigurationElement patternElement = patternElementTmp[0];

				if (patternElement.getAttribute("pattern") == null) { // $NON-NLS-1$
					ChangelogPlugin
							.getDefault()
							.getLog()
							.log(
									new Status(
											IStatus.ERROR,
											ChangelogPlugin.PLUGIN_ID,
											IStatus.ERROR,
											Messages.getString("ChangeLog.ErrNonPattern"), // $NON-NLS-1$
											new Exception(Messages.getString("ChangeLog.ErrNonPattern")))); // $NON-NLS-1$
				} else {
					String filePattern = patternElement.getAttribute("pattern"); // $NON-NLS-1$

					try {
						Pattern pattern = Pattern.compile(filePattern);
						Matcher fileMatcher = pattern.matcher(fileName);

						// if the filename of the current editor matches the
						// file
						// pattern then we're done
						if (fileMatcher.matches()) {
							formatterConfigElementToUse = formatterConfigElement;
							break;
						}
					} catch (PatternSyntaxException e) {
						ChangelogPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID,
										IStatus.ERROR, e.getMessage(), e));
					}
				}

			}

			// if we haven't found an in-file formatter we try to get the user's
			// prefered formatter
			if (formatterConfigElementToUse == null) {

				// we cache the user's preferred formatter on the first run, and
				// whenever it changes
				if (cachedPrefFormatter == null
						|| !cachedPrefFormatter.getAttribute("name").equals( // $NON-NLS-1$
								formatterName)) {

					for (int i = 0; i < elements.length; i++) {
						IConfigurationElement formatterConfigElement = elements[i];
						if (formatterConfigElement.getName()
								.equals("formatter") && formatterConfigElement.getAttribute("inFile").equalsIgnoreCase("false")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							if (formatterConfigElement.getAttribute("name") // $NON-NLS-1$
									.equals(formatterName))
								cachedPrefFormatter = formatterConfigElement;
							break;

						}
					}
				}

				formatterConfigElementToUse = cachedPrefFormatter;

				if (formatterConfigElementToUse == null) {
					ChangelogPlugin
							.getDefault()
							.getLog()
							.log(
									new Status(
											IStatus.ERROR,
											ChangelogPlugin.PLUGIN_ID,
											IStatus.ERROR,
											Messages.getString("ChangeLog.ErrRetrieveFormatter"), // $NON-NLS-1$
											new Exception(Messages.getString("ChangeLog.ErrRetrieveFormatter")))); // $NON-NLS-1$

					return null;
				}

			}
		}



		try {
			return (IFormatterChangeLogContrib) formatterConfigElementToUse
					.createExecutableExtension("class"); // $NON-NLS-1$

		} catch (CoreException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, e
							.getMessage(), e));
			e.printStackTrace();
		}
		return null;
	}
}
