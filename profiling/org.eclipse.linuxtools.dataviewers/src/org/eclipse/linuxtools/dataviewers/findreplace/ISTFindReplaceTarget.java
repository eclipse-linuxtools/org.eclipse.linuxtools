/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.findreplace;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;

public interface ISTFindReplaceTarget {
    /**
     * Returns whether a find operation can be performed.
     * 
     * @return whether a find operation can be performed
     */
    boolean canPerformFind();

    /**
     * Searches for a string starting at the given widget offset and using the specified search directives. If a string
     * has been found it is selected and its start offset is returned.
     * 
     * @param widgetOffset
     *            the ViewerCell offset at which searching starts
     * @param findString
     *            the string which should be found
     * @param searchForward
     *            <code>true</code> searches forward, <code>false</code> backwards
     * @param caseSensitive
     *            <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
     * @param wholeWord
     *            if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
     * @return the ViewerCell of the specified string, or -1 if the string has not been found
     */
    ViewerCell findAndSelect(ViewerCell cellOffset, String findString, boolean searchForward, boolean caseSensitive,
            boolean wholeWord, boolean wrapSearch, boolean regExSearch);

    /**
     * Returns the ViewerCell from the search has start
     * 
     * @param index
     */
    ViewerCell getSelection(ViewerCell index);

    /**
     * 
     * @param index
     * @param direction
     * @return
     */
    ViewerCell getFirstCell(ViewerCell index, int direction);

    /**
     * Returns the text contained into the start cell
     * 
     * @param index
     * @return
     */
    String getSelectionText(ViewerCell index);

    /**
     * Returns whether a cell is editable
     * 
     */
    Boolean isEditable();

    /**
     * Returns the viewers on which the find operation is working
     * 
     * @return
     */
    ColumnViewer getViewer();

    /**
     * Set the find action associated to this find target
     * 
     * @param action
     */
    void setFindAction(STFindReplaceAction action);

    /**
     * Sets if the "Find" operation is applied only to the selected lines in the viewer
     * 
     * @param use
     */
    void useSelectedLines(boolean use);
}
