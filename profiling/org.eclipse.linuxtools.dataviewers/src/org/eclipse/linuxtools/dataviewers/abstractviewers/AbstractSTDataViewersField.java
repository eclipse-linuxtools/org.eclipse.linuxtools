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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Simple implementation of ISTField
 */
public abstract class AbstractSTDataViewersField implements ISTDataViewersField {

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderImage()
     */
    @Override
    public Image getColumnHeaderImage() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getDefaultDirection()
     */
    @Override
    public int getDefaultDirection() {
        return STDataViewersComparator.ASCENDING;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getDescription()
     */
    @Override
    public String getDescription() {
        return getColumnHeaderText();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getDescriptionImage()
     */
    @Override
    public Image getDescriptionImage() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object obj) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getPreferredWidth()
     */
    @Override
    public int getPreferredWidth() {
        return 100;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#isShowingByDefault()
     */
    /**
	 * @since 5.0
	 */
    @Override
    public boolean isShowingByDefault() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getSpecialDrawer(java.lang.Object)
     */
    @Override
    public ISpecialDrawerListener getSpecialDrawer(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getBackground(java.lang.Object)
     */
    @Override
    public Color getBackground(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField#getColumnHeaderTooltip()
     */
    @Override
    public String getColumnHeaderTooltip() {
        return getColumnHeaderText();
    }

    @Override
    public String toString() {
        return getColumnHeaderText();
    }

    @Override
    public int getAlignment() {
        return SWT.NONE;
    }
}
