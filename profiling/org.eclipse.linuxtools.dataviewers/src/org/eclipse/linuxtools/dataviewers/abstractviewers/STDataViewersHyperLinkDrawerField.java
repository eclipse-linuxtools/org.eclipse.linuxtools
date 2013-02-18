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

import org.eclipse.linuxtools.dataviewers.listeners.ISpecialDrawerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;

/**
 * A field that implements a hyperlink drawer
 */
public abstract class STDataViewersHyperLinkDrawerField extends AbstractSTDataViewersField implements
        ISpecialDrawerListener {

    @Override
    public ISpecialDrawerListener getSpecialDrawer(Object element) {
        if (isHyperLink(element))
            return this;
        else
            return null;
    }

    /**
     * Gets the HyperLink value for the given object, as a string
     *
     * @param obj
     * @return a String value
     */
    public abstract String getHyperLink(Object obj);

    /**
     * HyperLink drawer
     */
    @Override
	public void handleEvent(Event event) {
        Item item = (Item) event.item;
        String str = getHyperLink(item.getData());
        final TextStyle styledString = new TextStyle(event.gc.getFont(), null, null);
        styledString.foreground = event.display.getSystemColor(SWT.COLOR_BLUE);
        styledString.underline = true;
        TextLayout tl = new TextLayout(event.display);
        tl.setText(str);

        tl.setStyle(styledString, 0, str.length());
        int y = event.y + event.height / 2 - event.gc.stringExtent(str).y / 2;
        tl.draw(event.gc, event.x, y);
    }

}
