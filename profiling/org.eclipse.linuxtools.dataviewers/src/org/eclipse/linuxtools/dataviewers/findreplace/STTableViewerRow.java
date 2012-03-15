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

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This is a copy of TableViewerRow is the Table implementation of ViewerRow.
 * It's useful to use some methods from protected to public that in the original
 * TableViewerRow can't be used
 * 
 */
public class STTableViewerRow extends STViewerRow<TableItem> {
	
	/**
	 * Create a new instance of the receiver from item.
	 * @param item
	 */
	public STTableViewerRow(TableItem item) {
		super(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return getItem().getBounds();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getBounds(int)
	 */
	@Override
	public Rectangle getBounds(int columnIndex) {
		return getItem().getBounds(columnIndex);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return getItem().getParent().getColumnCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getBackground(int)
	 */
	@Override
	public Color getBackground(int columnIndex) {
		return getItem().getBackground(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getFont(int)
	 */
	@Override
	public Font getFont(int columnIndex) {
		return getItem().getFont(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getForeground(int)
	 */
	@Override
	public Color getForeground(int columnIndex) {
		return getItem().getForeground(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getImage(int)
	 */
	@Override
	public Image getImage(int columnIndex) {
		return getItem().getImage(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getText(int)
	 */
	@Override
	public String getText(int columnIndex) {
		return getItem().getText(columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#setBackground(int, org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setBackground(int columnIndex, Color color) {
		getItem().setBackground(columnIndex, color);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#setFont(int, org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(int columnIndex, Font font) {
		getItem().setFont(columnIndex, font);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#setForeground(int, org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForeground(int columnIndex, Color color) {
		getItem().setForeground(columnIndex, color);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#setImage(int, org.eclipse.swt.graphics.Image)
	 */
	@Override
	public void setImage(int columnIndex, Image image) {
		Image oldImage = getItem().getImage(columnIndex);
		if (oldImage != image) {
			getItem().setImage(columnIndex,image);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#setText(int, java.lang.String)
	 */
	@Override
	public void setText(int columnIndex, String text) {
		getItem().setText(columnIndex, text == null ? "" : text); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getControl()
	 */
	@Override
	public Table getControl() {
		return getItem().getParent();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.findreplace.STViewerRow#getRowAbove()
	 */
	@Override
	protected STTableViewerRow getRowAbove(boolean sameLevel) {
		int index = getItem().getParent().indexOf(getItem()) - 1;
		if( index >= 0 ) {
			return new STTableViewerRow(getItem().getParent().getItem(index)); 
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.findreplace.STViewerRow#getRowBelow()
	 */
	@Override
	protected STTableViewerRow getRowBelow(boolean sameLevel) {
		int index = getItem().getParent().indexOf(getItem()) + 1;
		if( index < getItem().getParent().getItemCount() ) {
			TableItem tmp = getItem().getParent().getItem(index);
			//TODO NULL can happen in case of VIRTUAL => How do we deal with that
			if( tmp != null ) {
				return new STTableViewerRow(tmp);
			}
		}
		return null;
	}

	public TreePath getTreePath() {
		return new TreePath(new Object[] {getItem().getData()});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#clone()
	 */
	@Override
	public STTableViewerRow clone() {
		return new STTableViewerRow(getItem());
	}
}
