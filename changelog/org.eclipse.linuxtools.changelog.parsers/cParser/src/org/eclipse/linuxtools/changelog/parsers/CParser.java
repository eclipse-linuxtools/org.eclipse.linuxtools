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
package org.eclipse.linuxtools.changelog.parsers;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author pmuldoon (Phil Muldoon)
 */

public class CParser implements IParserChangeLogContrib {

	public CParser() {
		super();
	}

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorPart)
	 */
	public String parseCurrentFunction(IEditorPart editor) throws CoreException {

		if (!(editor instanceof CEditor)) {
			return "";
		}

		CEditor c_editor = (CEditor) editor;

		ITextSelection selection = (ITextSelection) ((AbstractTextEditor)c_editor)
				.getSelectionProvider().getSelection();

		IEditorInput input = c_editor.getEditorInput();

		return parseCurrentFunction(input, selection.getOffset());
	}

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorInput, int)
	 */
	public String parseCurrentFunction(IEditorInput input, int offset)
			throws CoreException {
		IWorkingCopyManager manager = CUIPlugin.getDefault()
				.getWorkingCopyManager();
		manager.connect(input);

		IWorkingCopy workingCopy = manager.getWorkingCopy(input);
		ICElement method = workingCopy.getElementAtOffset(offset);

		manager.disconnect(input);

		// no element selected
		if (method == null)
			return "";

		String currentElementName = "";

		if ((currentElementName = method.getElementName()) == null) {
			// element doesn't have a name
			return "";
		}

		int elementType = method.getElementType();

		switch (elementType) {
		case ICElement.C_FIELD:
		case ICElement.C_METHOD:
		case ICElement.C_FUNCTION:
			break;
		case ICElement.C_MODEL:
			return "";

		default:
			ICElement tmpMethodType;
			if (((tmpMethodType = method.getAncestor(ICElement.C_FUNCTION)) == null)
					&& ((tmpMethodType = method.getAncestor(ICElement.C_METHOD)) == null)
					&& ((tmpMethodType = method.getAncestor(ICElement.C_CLASS)) == null)) {
				return "";
			} else {
				// cursor is inside a class, but not method
				method = tmpMethodType;
				currentElementName = method.getElementName();
			}

		}

		// now append all ancestor class names to string

		ICElement tmpParent = method.getParent();

		while (tmpParent != null) {
			ICElement tmpParentClass = tmpParent.getAncestor(ICElement.C_CLASS);
			if (tmpParentClass != null) {
				String tmpParentClassName = tmpParentClass.getElementName();
				if (tmpParentClassName == null)
					return currentElementName;
				currentElementName = tmpParentClassName + "."
						+ currentElementName;
			} else
				return currentElementName;
			tmpParent = tmpParentClass.getParent();

		}
		return currentElementName;
	}

}
