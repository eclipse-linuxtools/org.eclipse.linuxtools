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
package org.eclipse.linuxtools.internal.changelog.parsers.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
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
@SuppressWarnings("restriction")
public class JavaParser implements IParserChangeLogContrib {

    public static final String STATIC_INITIALIZER_NAME = "static initializer";

    @Override
    public String parseCurrentFunction(IEditorInput input, int offset)
            throws CoreException {

        String currentElementName;
        int elementType;

        // Get the working copy and connect to input.
        IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
        manager.connect(input);

        // Retrieve the Java Element in question.
        // The following internal access is done because the getWorkingCopy()
        // method
        // for the WorkingCopyManager returns null for StorageEditorInput,
        // however,
        // there is a working copy available through the
        // ICompilationUnitDocumentProvider.
        ICompilationUnitDocumentProvider x = (ICompilationUnitDocumentProvider) JavaUI
                .getDocumentProvider();
        // Retrieve the Java Element in question.
        ICompilationUnit workingCopy = x.getWorkingCopy(input);

        if (workingCopy == null) {
            return "";
        }

        IJavaElement method = workingCopy.getElementAt(offset);

        manager.disconnect(input);

        // no element selected
        if (method == null) {
            return "";
        }

        // Get the current element name, to test it.
        currentElementName = method.getElementName();

        // Element doesn't have a name. Can go no further.
        if (currentElementName == null) {
            return "";
        }

        // Get the Element Type to test.
        elementType = method.getElementType();

        switch (elementType) {
        case IJavaElement.METHOD:
        case IJavaElement.FIELD:
            break;
        case IJavaElement.COMPILATION_UNIT:
            return "";
        case IJavaElement.INITIALIZER:
            return STATIC_INITIALIZER_NAME;

            // So it's not a method, field, type, or static initializer. Where
            // are we?
        default:
            IJavaElement tmpMethodType;
            if (((tmpMethodType = method.getAncestor(IJavaElement.METHOD)) == null)
                    && ((tmpMethodType = method.getAncestor(IJavaElement.TYPE)) == null)) {
                return "";
            } else {
                // In a class, but not in a method. Return class name instead.
                method = tmpMethodType;
                currentElementName = method.getElementName();
            }
        }

        // Build all ancestor classes.
        // Append all ancestor class names to string

        IJavaElement tmpParent = method.getParent();
        boolean firstLoop = true;

        while (tmpParent != null) {
            IJavaElement tmpParentClass = tmpParent
                    .getAncestor(IJavaElement.TYPE);
            if (tmpParentClass != null) {
                String tmpParentClassName = tmpParentClass.getElementName();
                if (tmpParentClassName == null) {
                    return "";
                }
                currentElementName = tmpParentClassName + "."
                        + currentElementName;
            } else {
                // cut root class name
                int rootClassPos = currentElementName.indexOf('.');
                if (rootClassPos >= 0) {
                    currentElementName = currentElementName
                            .substring(rootClassPos + 1);
                }
                if (firstLoop) {
                    return "";
                } else {
                    return currentElementName;
                }
            }
            tmpParent = tmpParentClass.getParent();
            firstLoop = false;

        }

        return "";
    }

    @Override
    public String parseCurrentFunction(IEditorPart editor) throws CoreException {

        // Check for correct editor type
        if (!(editor instanceof AbstractDecoratedTextEditor)) {
            return "";
        }

        // Get the editor, test selection and input.
        AbstractDecoratedTextEditor java_editor = (AbstractDecoratedTextEditor) editor;
        ITextSelection selection = (ITextSelection) (java_editor
                .getSelectionProvider().getSelection());
        IEditorInput input = java_editor.getEditorInput();

        // Parse it and return the function.
        return parseCurrentFunction(input, selection.getOffset());
    }

}
