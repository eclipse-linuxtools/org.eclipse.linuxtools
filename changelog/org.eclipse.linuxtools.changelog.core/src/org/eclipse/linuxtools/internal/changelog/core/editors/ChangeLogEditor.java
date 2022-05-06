/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *    Kiu Kwan Leung <kleung@redhat.com> - added a boolean field and its getter
 *                                         and setter to help formater properly
 *                                         decide when to add/delete a new line
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.internal.changelog.core.Messages;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * ChangeLog editor that supports GNU format.
 */
public class ChangeLogEditor extends TextEditor {

	private boolean forceNewLogEntry;

	public ChangeLogEditor() {
		super();

		SourceViewerConfiguration config = getConfig();

		if (config != null) {
			setSourceViewerConfiguration(config);
		} else {
			ChangelogPlugin.getDefault().getLog()
					.log(Status.error(Messages.getString("ChangeLogEditor.ErrConfiguration"), // $NON-NLS-1$
							new Exception(Messages.getString("ChangeLogEditor.ErrConfiguration")))); // $NON-NLS-1$
		}

		setDocumentProvider(new TextFileDocumentProvider());

	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.linuxtools.changelog.core.changelogEditorScope" }); //$NON-NLS-1$
	}

	/**
	 * Gets appropriate style editor from user pref.
	 *
	 * @return configuration for the Changelog editor
	 */

	private SourceViewerConfiguration getConfig() {

		IExtensionPoint editorExtensions = null;
		IEditorChangeLogContrib editorContrib = null;

		// get editor which is stored in preference.
		IPreferenceStore store = ChangelogPlugin.getDefault().getPreferenceStore();
		String pref_Editor = store.getString("IChangeLogConstants.DEFAULT_EDITOR"); // $NON-NLS-1$

		editorExtensions = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.linuxtools.changelog.core", //$NON-NLS-1$
				"editorContribution"); //$NON-NLS-1$

		if (editorExtensions != null) {
			IConfigurationElement[] elements = editorExtensions.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("editor") // $NON-NLS-1$
						&& (elements[i].getAttribute("name").equals(pref_Editor))) { //$NON-NLS-1$

					try {
						IConfigurationElement bob = elements[i];
						editorContrib = (IEditorChangeLogContrib) bob.createExecutableExtension("class"); // $NON-NLS-1$

						return (SourceViewerConfiguration) editorContrib;
					} catch (CoreException e) {
						ChangelogPlugin.getDefault().getLog().log(Status.error(e.getMessage(), e));
					}

				}
			}
		}

		return null;
	}

	public ISourceViewer getMySourceViewer() {
		return this.getSourceViewer();
	}

	public boolean isForceNewLogEntry() {
		return forceNewLogEntry;
	}

	public void setForceNewLogEntry(boolean forceNewLogEntry) {
		this.forceNewLogEntry = forceNewLogEntry;
	}

}
