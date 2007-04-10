/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com>       - initial API and implementation
 *    Kyu Lee <klee@redhat.com>                - bug fixes and improvement 
 *    Remy Chi Jian Suen <remy.suen@gmail.com> - support static blocks (#179549)
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.parsers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * @author pmuldoon (Phil Muldoon)
 */
public class JavaParser implements IParserChangeLogContrib {

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorPart)
	 */
	public String parseCurrentFunction(IEditorInput input, int offset)
			throws CoreException {
		IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
		manager.connect(input);

		ICompilationUnit workingCopy = manager.getWorkingCopy(input);
		IJavaElement method = workingCopy.getElementAt(offset);
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
		case IJavaElement.METHOD:
		case IJavaElement.FIELD:
			break;
		case IJavaElement.COMPILATION_UNIT:
			return "";
		case IJavaElement.INITIALIZER:
			return "static initializer";
		default:
			IJavaElement tmpMethodType;
			if (((tmpMethodType = method.getAncestor(IJavaElement.METHOD)) == null)
					&& ((tmpMethodType = method.getAncestor(IJavaElement.TYPE)) == null)) {
				return "";
			} else {
				// cursor is inside a class, but not method
				method = tmpMethodType;
				currentElementName = method.getElementName();
			}
		}

		// now append all ancestor class names to string

		IJavaElement tmpParent = method.getParent();
		boolean firstLoop = true;

		while (tmpParent != null) {
			IJavaElement tmpParentClass = tmpParent
					.getAncestor(IJavaElement.TYPE);
			if (tmpParentClass != null) {
				String tmpParentClassName = tmpParentClass.getElementName();
				if (tmpParentClassName == null)
					return "";
				currentElementName = tmpParentClassName + "."
						+ currentElementName;
			} else {
				// cut root class name
				int rootClassPos = currentElementName.indexOf(".");
				if (rootClassPos >= 0)
					currentElementName = currentElementName
							.substring(rootClassPos + 1);
				if (firstLoop)
					return "";
				else
					return currentElementName;
			}
			tmpParent = tmpParentClass.getParent();
			firstLoop = false;

		}

		return "";
	}

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorInput, int)
	 */
	public String parseCurrentFunction(IEditorPart editor) throws CoreException {

		// Check for type casting
		if (!(editor instanceof AbstractDecoratedTextEditor))
			return "";

		AbstractDecoratedTextEditor java_editor = (AbstractDecoratedTextEditor) editor;

		ITextSelection selection = (ITextSelection) java_editor
				.getSelectionProvider().getSelection();

		IEditorInput input = java_editor.getEditorInput();

		return parseCurrentFunction(input, selection.getOffset());
	}

}
