/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * Abstract resolution for Valgrind markers
 * @author rafaelmt
 *
 */
public abstract class AbstractValgrindMarkerResolution extends AbstractCodanCMarkerResolution {

    /**
     * Returns the enclosed AST node in the given marker.
     * @param marker The {@link IMarker} containing the {@link IASTNode}
     * @return the enclosed {@link IASTNode}
     */
    protected IASTNode getIASTNode(IMarker marker, IDocument document){
        int offset = this.getOffset(marker, document);
        int length = this.getLength(marker, document);

        IASTNode node = null;
        IASTTranslationUnit ast = getASTTranslationUnit(marker);

        IASTNodeSelector nodeSelector = ast.getNodeSelector(marker.getResource().getLocationURI().getPath());
        node = nodeSelector.findFirstContainedNode(offset, length);

        return node;
    }

    /**
     * Returns the translation unit that contains the given marker.
     * @param marker The {@link IMarker} from which the {@link IASTTranslationUnit} will be obtained
     * @return {@link IASTTranslationUnit} containing the marker
     */
    private IASTTranslationUnit getASTTranslationUnit(IMarker marker){
        ITranslationUnit tu = getTranslationUnitViaEditor(marker);
        try {
            return tu.getAST();
        } catch (CoreException e) {
            return null;
        }
    }

    /**
     * Returns the length of the code contained in the given marker or
     * -1 if the location does not exist in the document.
     * @param marker {@link IMarker} from which the length will be obtained
     * @return length of the code enclosed in the {@link IMarker}
     */
    private int getLength(IMarker marker, IDocument document) {
        int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
        int charEnd = marker.getAttribute(IMarker.CHAR_END, -1);
        if (charEnd != -1 && charStart != -1) {
            return charEnd - charStart;
        }
        int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) -1;
        try {
            return document.getLineLength(line);
        } catch (BadLocationException e) {
            return -1;
        }
    }
}
