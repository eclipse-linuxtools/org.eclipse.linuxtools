/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib;
import org.eclipse.linuxtools.changelog.core.actions.FormatChangeLogAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;


/**
 * ChangeLog editor that supports GNU format.
 * 
 * @author klee (Kyu Lee)
 */
public class ChangeLogEditor extends TextEditor {

	FormatChangeLogAction fcla;

	public ChangeLogEditor() {
		super();

		fcla = new FormatChangeLogAction(this);

		SourceViewerConfiguration config = getConfig();

		if (config != null) {

			setSourceViewerConfiguration(config);
		} else
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR,
							"Couldn't get editor configuration", new Exception(
									"Couldn't get editor configuration")));

		setDocumentProvider(new ChangeLogDocumentProvider());

	}

	/**
	 * Gets appropriate style editor from user pref.
	 * 
	 * @return configuration for the Chagelog editor
	 */
	
	public SourceViewerConfiguration getConfig() {

		IExtensionPoint editorExtensions = null;
		IEditorChangeLogContrib editorContrib = null;

		// get editor which is stored in preference.
		IPreferenceStore store = ChangelogPlugin.getDefault()
				.getPreferenceStore();
		String pref_Editor = store
				.getString("IChangeLogConstants.DEFAULT_EDITOR");

		editorExtensions = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.linuxtools.changelog.core", "editorContribution"); //$NON-NLS-1$

		if (editorExtensions != null) {
			IConfigurationElement[] elements = editorExtensions
					.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("editor") && (elements[i].getAttribute("name").equals(pref_Editor))) { //$NON-NLS-1$

					try {
						IConfigurationElement bob = elements[i];
						editorContrib = (IEditorChangeLogContrib) bob
								.createExecutableExtension("class");

						editorContrib.setTextEditor(this);
						return (SourceViewerConfiguration) editorContrib;
					} catch (CoreException e) {
						ChangelogPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, "Changelog",
										IStatus.ERROR, e.getMessage(), e));
					}

				}
			}
		}

		return null;
	}

	public void dispose() {
		super.dispose();

	}

	
	public ISourceViewer getMySourceViewer() {
		return this.getSourceViewer();
	}
	
	/**
	 * Specifies context menu.
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {

		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_LEFT);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, fcla);

	}

}
