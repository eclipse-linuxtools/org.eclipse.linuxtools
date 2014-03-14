/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *     IBM Corporation - wrote CompletionProposal, upon which this class is based
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.proposals;

import java.security.InvalidParameterException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ManpageCacher;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetItemType;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public abstract class STPCompletionProposal implements ICompletionProposal {

    private final int fPrefixLength;
    private final int fReplacementOffset;
    private final String fDisplayString;

    protected final TreeNode fCompletionNode;
    protected String fDocumentation = null;

    public STPCompletionProposal(TreeNode completionNode, int prefixLength, int replacementOffset) {
        if (completionNode == null) {
            throw new InvalidParameterException();
        }

        fCompletionNode = completionNode;
        fPrefixLength = prefixLength;
        fReplacementOffset = replacementOffset;
        fDisplayString = completionNode.toString() + " - " + getType().toString(); //$NON-NLS-1$
    }

    protected String getReplacementString() {
        return fCompletionNode.toString();
    }

    protected int getCursorPosition() {
        return getReplacementString().length() - fPrefixLength;
    }

    @Override
    public String getAdditionalProposalInfo() {
        if (fDocumentation == null) {
            fDocumentation = ManpageCacher.getDocumentation(
                    getType(), getDocumentationSearchArgs());
        }
        return fDocumentation;
    }

    abstract protected TapsetItemType getType();

    protected String[] getDocumentationSearchArgs() {
        return new String[]{fCompletionNode.toString()};
    }

    @Override
    public void apply(IDocument document) {
        try {
            document.replace(fReplacementOffset, 0, getReplacementString().substring(fPrefixLength));
        } catch (BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(fReplacementOffset + getCursorPosition(), 0);
    }

    @Override
    public String getDisplayString() {
        return fDisplayString;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

}
