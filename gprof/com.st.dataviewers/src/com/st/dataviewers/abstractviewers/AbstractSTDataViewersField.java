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
package com.st.dataviewers.abstractviewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.st.dataviewers.listeners.ISpecialDrawerListener;


/**
 * Simple implementation of ISTField
 */
public abstract class AbstractSTDataViewersField implements ISTDataViewersField {

	private boolean showing = true;
		
	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return STDataViewersComparator.ASCENDING;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getDescription()
	 */
	public String getDescription() {
		return getColumnHeaderText();
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		return 100;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#isShowing()
	 */
	public boolean isShowing() {
		return this.showing;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#setShowing(boolean)
	 */
	public void setShowing(boolean showing) {
		this.showing = showing;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getSpecialDrawer()
	 */
	public ISpecialDrawerListener getSpecialDrawer(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getToolTipText(java.lang.Object)
	 */
	public String getToolTipText(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.ISTField#getColumnHeaderTooltip()
	 */
	public String getColumnHeaderTooltip() {
		return getColumnHeaderText();
	}

	@Override
	public String toString() {
		return getColumnHeaderText();
	}
	
	public int getAlignment(){
		return SWT.NONE;
	}
	
	public boolean isHyperLink(Object element){
		return false;
	}
}
