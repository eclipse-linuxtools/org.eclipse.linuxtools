/*******************************************************************************
 * Copyright (c) 2000, 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.text.CharacterIterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * An <code>IDocument</code> based implementation of
 * <code>CharacterIterator</code> and <code>CharSequence</code>. Note that
 * the supplied document is not copied; if the document is modified during the
 * lifetime of a <code>DocumentCharacterIterator</code>, the methods
 * returning document content may not always return the same values. Also, if
 * accessing the document fails with a {@link BadLocationException}, any of
 * <code>CharacterIterator</code> methods as well as <code>charAt</code>may
 * return {@link CharacterIterator#DONE}.
 *
 * @since 4.0
 */
public class DocumentCharacterIterator implements CharacterIterator, CharSequence {

    private int fIndex= -1;
    private final IDocument fDocument;
    private final int fFirst;
    private final int fLast;

    private void invariant() {
        Assert.isTrue(fIndex >= fFirst);
        Assert.isTrue(fIndex <= fLast);
    }

    /**
     * Creates an iterator for the entire document.
     *
     * @param document the document backing this iterator
     */
    public DocumentCharacterIterator(IDocument document) {
        this(document, 0);
    }

    /**
     * Creates an iterator, starting at offset <code>first</code>.
     *
     * @param document the document backing this iterator
     * @param first the first character to consider
     * @throws IllegalArgumentException if the indices are out of bounds
     */
    public DocumentCharacterIterator(IDocument document, int first) {
        this(document, first, document.getLength());
    }

    /**
     * Creates an iterator for the document contents from <code>first</code>
     * (inclusive) to <code>last</code> (exclusive).
     *
     * @param document the document backing this iterator
     * @param first the first character to consider
     * @param last the last character index to consider
     * @throws IllegalArgumentException if the indices are out of bounds
     */
    public DocumentCharacterIterator(IDocument document, int first, int last) {
        if (document == null)
            throw new NullPointerException();
        if (first < 0 || first > last)
            throw new IllegalArgumentException();
        if (last > document.getLength())
            throw new IllegalArgumentException();
        fDocument= document;
        fFirst= first;
        fLast= last;
        fIndex= first;
        invariant();
    }

    @Override
    public char first() {
        return setIndex(getBeginIndex());
    }

    @Override
    public char last() {
        if (fFirst == fLast)
            return setIndex(getEndIndex());
        return setIndex(getEndIndex() - 1);
    }

    @Override
    public char current() {
        if (fIndex >= fFirst && fIndex < fLast)
            try {
                return fDocument.getChar(fIndex);
            } catch (BadLocationException e) {
                // ignore
            }
        return DONE;
    }

    @Override
    public char next() {
        return setIndex(Math.min(fIndex + 1, getEndIndex()));
    }

    @Override
    public char previous() {
        if (fIndex > getBeginIndex()) {
            return setIndex(fIndex - 1);
        }
        return DONE;
    }

    @Override
    public char setIndex(int position) {
        if (position >= getBeginIndex() && position <= getEndIndex())
            fIndex= position;
        else
            throw new IllegalArgumentException();

        invariant();
        return current();
    }

    @Override
    public int getBeginIndex() {
        return fFirst;
    }

    @Override
    public int getEndIndex() {
        return fLast;
    }

    @Override
    public int getIndex() {
        return fIndex;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public int length() {
        return getEndIndex() - getBeginIndex();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that, if the document is modified concurrently, this method may
     * return {@link CharacterIterator#DONE} if a {@link BadLocationException}
     * was thrown when accessing the backing document.
     * </p>
     *
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public char charAt(int index) {
        if (index >= 0 && index < length())
            try {
                return fDocument.getChar(getBeginIndex() + index);
            } catch (BadLocationException e) {
                // ignore and return DONE
                return DONE;
            }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0)
            throw new IndexOutOfBoundsException();
        if (end < start)
            throw new IndexOutOfBoundsException();
        if (end > length())
            throw new IndexOutOfBoundsException();
        return new DocumentCharacterIterator(fDocument, getBeginIndex() + start, getBeginIndex() + end);
    }

    @Override
    public String toString() {
        int length = length();
        char[] chs = new char[length];
        for (int i=0; i<length; ++i) {
            chs[i] = charAt(i);
        }
        return new String(chs);
    }
}
