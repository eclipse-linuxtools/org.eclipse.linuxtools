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
     * Gets the percentage value for the given object, as a float
     *
     * @param obj
     * @return a float value, between 0 and 100, please !
     */
    public abstract float getPercentage(Object obj);

    /**
     * Percentage drawer
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
        int width = (widthcol - 1) * percent / 100;
        if (width > 0) {
            Color background = gc.getBackground();
            int alpha = gc.getAlpha();
            gc.setAlpha(128);
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            gc.fillRectangle(event.x, event.y, width, event.height);
            gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
            gc.fillRectangle(event.x, event.y, width, event.height);
            gc.setAlpha(alpha);
            gc.setBackground(background);
        }
        String text = "%";
        text = (isSettedNumberFormat() ? getNumberFormat().format(d) : d) + text;
        Point size = gc.textExtent(text);
        int offset = Math.max(0, (event.height - size.y) / 2);
        Color foreground = gc.getForeground();
        gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        gc.drawText(text, event.x + 2, event.y + offset, true);
        gc.setForeground(foreground);
    }

    public abstract NumberFormat getNumberFormat();

    public abstract boolean isSettedNumberFormat();

}
