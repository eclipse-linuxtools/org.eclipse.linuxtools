/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation 
 *******************************************************************************/

package org.eclipse.linuxtools.changelog.core.parsers;

//TODO: Disabled this python for now to remove runtime pydev dependency
//      until implementing a way to optionally require pydev. 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author pmuldoon (Phil Muldoon)
 */

public class PythonParser implements IParserChangeLogContrib {

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorPart)
	 */
	public String parseCurrentFunction(IEditorPart editor) throws CoreException {

/*		if (editor instanceof PyEdit) {
			PyEdit python_editor = (PyEdit) editor;
			IDocument doc = python_editor.getDocumentProvider().getDocument(
					python_editor.getEditorInput());
			ITextSelection selection = (ITextSelection) python_editor
					.getSelectionProvider().getSelection();

			Location loc = Location
					.offsetToLocation(doc, selection.getOffset());
			AbstractNode node = ModelUtils.getElement(python_editor
					.getPythonModel(), loc, AbstractNode.PROP_ANY);

			if (node != null) {
				AbstractNode node_parent = node.getScope().getStartNode();
				if (node_parent != null) {
					if (!python_editor.getTitle().equals(node_parent.getName()))
						return node_parent.getName();

				}
			}

			return "";

		}*/
		return "";
	}

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorInput, int)
	 */
	public String parseCurrentFunction(IEditorInput editor, int offset)
			throws CoreException {
		// TODO Auto-generated method stub
		return "";
	}
}
