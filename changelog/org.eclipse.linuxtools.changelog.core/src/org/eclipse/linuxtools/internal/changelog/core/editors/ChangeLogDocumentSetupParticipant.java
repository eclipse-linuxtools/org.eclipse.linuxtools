/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib;
import org.eclipse.linuxtools.changelog.core.IEditorChangeLogContrib2;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;

public class ChangeLogDocumentSetupParticipant implements
		IDocumentSetupParticipant, IDocumentListener {

	@Override
	public void setup(IDocument document) {
		IExtensionPoint editorExtensions = null;
		IEditorChangeLogContrib editorContrib = null;

		// get editor which is stored in preference.
		IPreferenceStore store = ChangelogPlugin.getDefault()
				.getPreferenceStore();
		String pref_Editor = store
				.getString("IChangeLogConstants.DEFAULT_EDITOR"); // $NON-NLS-1$

		editorExtensions = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.linuxtools.changelog.core", "editorContribution"); //$NON-NLS-1$ //$NON-NLS-2$

		if (editorExtensions != null) {
			IConfigurationElement[] elements = editorExtensions
					.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i].getName().equals("editor") // $NON-NLS-1$
						&& (elements[i].getAttribute("name").equals(pref_Editor))) { //$NON-NLS-1$

					try {
						// If editor has a special setup routine, use it.
						IConfigurationElement bob = elements[i];
						editorContrib = (IEditorChangeLogContrib) bob
								.createExecutableExtension("class"); // $NON-NLS-1$

						if (editorContrib instanceof IEditorChangeLogContrib2)
							((IEditorChangeLogContrib2)editorContrib).setup(document);
					} catch (CoreException e) {
						ChangelogPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID,
										IStatus.ERROR, e.getMessage(), e));
					}
				}
			}
		}
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */

	@Override
	public void documentAboutToBeChanged(DocumentEvent e) {
		// do nothing
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	@Override
	public void documentChanged(DocumentEvent e) {
		// do nothing
	}

}
