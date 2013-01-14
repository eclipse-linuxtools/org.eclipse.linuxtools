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
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import java.util.ArrayList;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.ide.FileStoreEditorInput;

public abstract class AbstractSTAnnotatedSourceEditorInput extends FileStoreEditorInput {
    private ArrayList<ISTAnnotationColumn> columns;

    public AbstractSTAnnotatedSourceEditorInput(IFileStore fileStore) {
        super(fileStore);
    }

    /**
     * gets how many STColumn exist
     * 
     * @return
     */
    public int getColumnCount() {
        if (columns == null) {
            columns = getColumns();
        }
        return columns != null ? columns.size() : 0;
    }

    /**
     * gets the title of a ST Column
     * 
     * @param column
     * @return
     */
    public String getTitle(ISTAnnotationColumn column) {
        if (columns == null) {
            columns = getColumns();
        }
        return column != null ? column.getTitle() : null;
    }

    /**
     * gets the background column of a editor line
     * 
     * @param line
     * @return
     */
    public abstract Color getColor(int line);

    public String getAnnotation(int line, ISTAnnotationColumn column) {
        if (columns == null) {
            columns = getColumns();
        }
        return column != null ? column.getAnnotation(line) : null;
    }

    public String getLongDescription(int line, ISTAnnotationColumn column) {
        if (columns == null) {
            columns = getColumns();
        }
        return column != null ? column.getLongDescription(line) : null;
    }

    public String getTooltip(int line, ISTAnnotationColumn column) {
        if (columns == null) {
            columns = getColumns();
        }
        return column != null ? column.getTooltip(line) : null;
    }

    /**
     * gets the ISTAnnotationColumn objects list
     * 
     * @return
     */
    public abstract ArrayList<ISTAnnotationColumn> getColumns();
}
