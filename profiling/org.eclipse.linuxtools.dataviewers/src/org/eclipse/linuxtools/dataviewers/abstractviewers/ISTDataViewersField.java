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
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.dataviewers.listeners.ISpecialDrawerListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * This class defines how the data is displayed for a given column. This renderer is on charge of providing several
 * things:
 * <ul>
 * <li>the name of the column header
 * <li>the tooltip of the column header
 * <li>It computes the label to display for each object given by the {@link ITreeContentProvider}
 * <li>It computes the tooltip to display for each object given by the {@link ITreeContentProvider}
 * <li>It computes the background & foreground color to display for each object given by the
 * {@link ITreeContentProvider}
 * <li>It computes the image to display for each object given by the {@link ITreeContentProvider}
 * <li>It provides a comparator, used to compare objects given by the {@link ITreeContentProvider}
 * <li>It allows a "custom rendering", for example if you want to display percentages as progress bars or to display
 * hyperlink as underlined text
 * </ul>
 * Three abstract implementations are available: {@link AbstractSTDataViewersField} for default rendering
 * {@link AbstractPercentageDrawerField} for displaying percentages as progress bars
 * {@link STDataViewersHyperLinkDrawerField} for displaying hyperlinks as underlined text
 */
public interface ISTDataViewersField {
    /**
     * @return String the description of the field.
     */
    String getDescription();

    /**
     * @return the image associated with the description of the field or <code>null<code>.
     */
    Image getDescriptionImage();

    /**
     * @return The text to be displayed in the column header for this field.
     */
    String getColumnHeaderText();

    /**
     * @return The tooltip to be displayed in the column header for this field.
     */
    String getColumnHeaderTooltip();

    /**
     * @return The image to be displayed in the column header for this field or <code>null<code>.
     */
    Image getColumnHeaderImage();

    /**
     * @param obj
     * @return The String value of the object for this particular field displayed to the user.
     */
    String getValue(Object obj);

    /**
     * @param obj
     * @return The image value of the object for this particular field displayed to the user or <code>null<code>.
     */
    Image getImage(Object obj);

    /**
     * @param obj1
     * @param obj2
     * @return Either: <li>a negative number if the value of obj1 is less than the value of obj2 for this field. <li>
     *         <code>0</code> if the value of obj1 and the value of obj2 are equal for this field. <li>a positive number
     *         if the value of obj1 is greater than the value of obj2 for this field.
     */
    int compare(Object obj1, Object obj2);

    /**
     * Get the default direction for the receiver. Return either {@link STDataViewersComparator#ASCENDING } or
     * {@link STDataViewersComparator#DESCENDING }
     * @return int
     */
    int getDefaultDirection();

    /**
     * Get the preferred width of the receiver.
     * @return int
     */
    int getPreferredWidth();

    /**
     * Return whether the receiver is showing or not by default.
     * @return boolean
     * @since 5.0
     */
    boolean isShowingByDefault();

    /**
     * Returns special drawer, typically used to: paint percentages paint hyperlink
     * @return a special drawer
     */
    ISpecialDrawerListener getSpecialDrawer(Object element);

    /**
     * @param element
     * @return the tooltip to display this particular element.
     */
    String getToolTipText(Object element);

    /**
     * @param element
     * @return the background color for the given element
     */
    Color getBackground(Object element);

    /**
     * @param element
     * @return the foreground color for the given element
     */
    Color getForeground(Object element);

    /**
     * Customize the horizontal alignment of the columns.
     * @return one of: SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.NONE. Note that SWT.NONE is equivalent to SWT.LEFT
     */
    int getAlignment();

    /**
     * Indicates if the given element is a hyperlink to something as a view, editor, dialog,etc...
     */
    boolean isHyperLink(Object element);

}
