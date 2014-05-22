/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *    Lev Ufimtsev <lufimtse@redhat.com>     - added green/red progress bars
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import java.text.NumberFormat;

import org.eclipse.linuxtools.dataviewers.listeners.ISpecialDrawerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * A field that implements a percentage drawer
 */
public abstract class AbstractPercentageDrawerField extends AbstractSTDataViewersField implements
        ISpecialDrawerListener {

    @Override
    public ISpecialDrawerListener getSpecialDrawer(Object obj) {
        return this;
    }

    @Override
    public String getValue(Object obj) {
        return getPercentage(obj) + "";
    }

    /**
     * Gets the percentage value for the given object, as a float.
     * Implementations must return values between 0 and 100.
     *
     * @param obj The object to retrieve percentage value for.
     * @return A float value, between 0 and 100.
     */
    public abstract float getPercentage(Object obj);

    /**
     * Percentage drawer.
     */
    @Override
    public void handleEvent(Event event) {
        Item item = (Item) event.item;
        Display display = event.widget.getDisplay();
        int index = event.index;
        int widthcol = 0;

        if (event.widget instanceof Tree) {
            Tree tree = (Tree) event.widget;
            widthcol = tree.getColumn(index).getWidth();
        } else { // event.widget instanceof Table
            Table table = (Table) event.widget;
            widthcol = table.getColumn(index).getWidth();
        }

        float d = getPercentage(item.getData());
        int percent = (int) (d + 0.5);
        GC gc = event.gc;

        //--Define the size of the Progress bar.
        int totalProgBarLength = (int) (widthcol * 0.3); //This makes it grow/shrink dynamically.
        if (totalProgBarLength > 0) {
            // ------------------------------------------------------
            // ------------------- Draw Progress bar
            // ------------------------------------------------------

            // ---- Shared elements
            int totalProgBarHeight = (int) (event.height * 0.5);

            // --- Draw Green (covered) part
            gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
            int progBarGreenWidth = (int) (totalProgBarLength * (percent * 0.01));
            int progBarGreenHorizontalPos = event.x;
            int progBarGreenHeight = (int) (event.height * 0.5);
            int progBarGreenVerticalPos = event.y + (totalProgBarHeight / 2) + 1;
            gc.fillRectangle(progBarGreenHorizontalPos, progBarGreenVerticalPos, progBarGreenWidth,
                    progBarGreenHeight);

            // --- Draw Red (uncovered) Part
            gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_RED));
            int progBarRedWidth = totalProgBarLength - progBarGreenWidth;
            int progBarRedHorizontalPos = progBarGreenHorizontalPos + progBarGreenWidth;
            int progBarRedHeight = progBarGreenHeight;
            int progBarRedVerticalPos = progBarGreenVerticalPos;
            gc.fillRectangle(progBarRedHorizontalPos, progBarRedVerticalPos, progBarRedWidth,
                    progBarRedHeight);
        }
        // ------------------------------------------------------
        // ------------------- Draw text next to Progress bar
        // ------------------------------------------------------

        //------- Define the 'text' format.  % text     e.g 94.0%
        String text = "%";
        text = (isSettedNumberFormat() ? getNumberFormat().format(d) : d) + text;
        Point size = gc.textExtent(text);
        int offset = Math.max(0, (event.height - size.y) / 2);

        //------- Position the text
        Color foreground = gc.getForeground();
        gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));

        //add offset of progress bar. (so that the text is to the right of the progress bar).
        int progBarOffset = totalProgBarLength + (widthcol / 20); //(prog bar size) + (20th of column widht)
        gc.drawText(text, event.x + progBarOffset, event.y + offset, true);
        gc.setForeground(foreground);
    }

    public abstract NumberFormat getNumberFormat();

    public abstract boolean isSettedNumberFormat();

}
