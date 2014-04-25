/*******************************************************************************
 * Copyright (c) 2006, 2010 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation, fixes.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.parsers;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author pmuldoon (Phil Muldoon)
 */
public class CParser implements IParserChangeLogContrib {

    public CParser() {
        super();
    }

    /**
     * @see IParserChangeLogContrib#parseCurrentFunction(IEditorInput, int)
     */
    @Override
    public String parseCurrentFunction(IEditorInput input, int offset)
            throws CoreException {

        String currentElementName;

        if (input instanceof IFileEditorInput) {
            // Get the working copy and connect to input.
            IWorkingCopyManager manager = CUIPlugin.getDefault()
            .getWorkingCopyManager();
            manager.connect(input);

            // Retrieve the C/C++ Element in question.
            IWorkingCopy workingCopy = manager.getWorkingCopy(input);
            ICElement method = workingCopy.getElementAtOffset(offset);

            manager.disconnect(input);

            // no element selected
            if (method == null)
                return "";

            // Get the current element name, to test it.
            currentElementName = method.getElementName();

            // Element doesn't have a name. Can go no further.
            if (currentElementName == null) {
                // element doesn't have a name
                return "";
            }

            // Get the Element Type to test.
            int elementType = method.getElementType();

            switch (elementType) {
            case ICElement.C_FIELD:
            case ICElement.C_METHOD:
            case ICElement.C_FUNCTION:
                break;
            case ICElement.C_MODEL:
                return "";

                // So it's not a method, field, function, or model. Where are we?
            default:
                ICElement tmpMethodType;
                if (((tmpMethodType = method.getAncestor(ICElement.C_FUNCTION)) == null)
                        && ((tmpMethodType = method.getAncestor(ICElement.C_METHOD)) == null)
                        && ((tmpMethodType = method.getAncestor(ICElement.C_CLASS)) == null)) {
                    return "";
                } else {
                    // In a class, but not in a method. Return class name instead.
                    method = tmpMethodType;
                    currentElementName = method.getElementName();
                }

            }

            // Build all ancestor classes.
            // Append all ancestor class names to string
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
        else if (input instanceof IStorageEditorInput) {
            // Get the working copy and connect to input.
            // don't follow inclusions
            currentElementName = "";
            IStorageEditorInput sei = (IStorageEditorInput)input;
            // don't follow inclusions
            IncludeFileContentProvider contentProvider = IncludeFileContentProvider.getEmptyFilesProvider();

            // empty scanner info
            IScannerInfo scanInfo= new ScannerInfo();
            IStorage ancestorStorage = sei.getStorage();
            if (ancestorStorage == null)
                return "";
            InputStream stream = ancestorStorage.getContents();
            byte buffer[] = new byte[100];
            String data = "";
            int read = 0;
            try {
                do {
                    read = stream.read(buffer);
                    if (read > 0) {
                        String tmp = new String(buffer, 0, read);
                        data = data.concat(tmp);
                    }
                } while (read == 100);
                stream.close();
            } catch (IOException e) {
                // do nothing
            }

            FileContent content = FileContent.create("<text>", data.toCharArray()); //$NON-NLS-1$

            // determine the language
            boolean isSource[]= {false};
            ILanguage language= GPPLanguage.getDefault();

            try {
                IASTTranslationUnit ast;
                int options= isSource[0] ? ILanguage.OPTION_IS_SOURCE_UNIT : 0;
                ast= language.getASTTranslationUnit(content, scanInfo, contentProvider, null, options, ParserUtil.getParserLogService());
                IASTNodeSelector n = ast.getNodeSelector(null);
                IASTNode node = n.findFirstContainedNode(offset, 100);
                while (node != null && !(node instanceof IASTTranslationUnit)) {
                    if (node instanceof IASTFunctionDefinition) {
                        IASTFunctionDefinition fd = (IASTFunctionDefinition)node;
                        IASTFunctionDeclarator d = fd.getDeclarator();
                        currentElementName = new String(d.getName().getSimpleID());
                        break;
                    }
                    node = node.getParent();
                }
//                System.out.println(currentElementName);
            } catch (CoreException exc) {
                currentElementName = "";
                CUIPlugin.log(exc);
            }

            return currentElementName;
        }

        return "";
    }

    /**
     * @see IParserChangeLogContrib#parseCurrentFunction(IEditorPart)
     */
    @Override
    public String parseCurrentFunction(IEditorPart editor) throws CoreException {

        // Check for correct editor type
        if (!(editor instanceof AbstractTextEditor))
            return "";

        // Get the editor, test selection and input.
        AbstractTextEditor a_editor = (AbstractTextEditor) editor;
        ITextSelection selection = (ITextSelection) (a_editor)
                .getSelectionProvider().getSelection();
        IEditorInput input = a_editor.getEditorInput();

        // Parse it and return the function.
        return parseCurrentFunction(input, selection.getOffset());
    }


}
