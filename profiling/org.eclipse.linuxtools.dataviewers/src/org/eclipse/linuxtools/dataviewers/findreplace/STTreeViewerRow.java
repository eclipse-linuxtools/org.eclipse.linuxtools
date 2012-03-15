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

import java.util.LinkedList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This is a copy of TreeViewerRow is the Tree implementation of ViewerRow.
 * It's useful to use some methods from protected to public that in the original
 * TreeViewerRow can't be used
 * 
 */
public class STTreeViewerRow extends STViewerRow<TreeItem> {
	
	/**
	 * Create a new instance of the receiver.
	 * @param item
	 */
	public STTreeViewerRow(TreeItem item) {
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
		if (image != oldImage) {
			getItem().setImage(columnIndex, image);
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
	public Tree getControl() {
		return getItem().getParent();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.findreplace.STViewerRow#getRowBelow(boolean)
	 */
	@Override
	protected ViewerRow getRowBelow(boolean sameLevel) {
		Tree tree = getItem().getParent();
		
		// This means we have top-level item
		if( getItem().getParentItem() == null ) {
			if( sameLevel || ! getItem().getExpanded() ) {
				int index = tree.indexOf(getItem()) + 1;
				if( index < tree.getItemCount() ) {
					return new STTreeViewerRow(tree.getItem(index));
				}
			} else if( getItem().getExpanded() && getItem().getItemCount() > 0 ) {
				return new STTreeViewerRow(getItem().getItem(0));
			}
		} else {
			if( sameLevel || ! getItem().getExpanded() ) {
				TreeItem parentItem = getItem().getParentItem();
				
				int nextIndex = parentItem.indexOf(getItem()) + 1;
				int totalIndex = parentItem.getItemCount();
				
				TreeItem itemAfter;
				// This would mean that it was the last item
				if( nextIndex == totalIndex ) {
					itemAfter = findNextItem( parentItem );
				} else {
					itemAfter = parentItem.getItem(nextIndex);
				}
				
				if( itemAfter != null ) {
					return new STTreeViewerRow(itemAfter);
				}
			} else if( getItem().getExpanded() && getItem().getItemCount() > 0 ) {
				return new STTreeViewerRow(getItem().getItem(0));
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.findreplace.STViewerRow#getRowAbove(boolean)
	 */
	@Override
	protected ViewerRow getRowAbove(boolean sameLevel) {
		Tree tree = getItem().getParent();
		
		// This means we have top-level item
		if( getItem().getParentItem() == null ) {
			int index = tree.indexOf(getItem()) - 1;
			TreeItem nextTopItem = null;
			if (index >= 0) {
				nextTopItem = tree.getItem(index);
			}
			if (nextTopItem != null) {
				if (sameLevel) {
					return new STTreeViewerRow(nextTopItem);
				}
				return new STTreeViewerRow(findLastVisibleItem(nextTopItem));
			}
		} else {
			TreeItem parentItem = getItem().getParentItem();
			int previousIndex = parentItem.indexOf(getItem()) - 1;
			
			TreeItem itemBefore;
			if( previousIndex >= 0 ) {
				if( sameLevel ) {
					itemBefore = parentItem.getItem(previousIndex);
				} else {
					itemBefore = findLastVisibleItem(parentItem.getItem(previousIndex));
				}
			} else {
				itemBefore = parentItem;
			}
			if( itemBefore != null ) {
				return new STTreeViewerRow(itemBefore);
			}
		}
		return null;
	}

	private TreeItem findLastVisibleItem(TreeItem parentItem) {
		TreeItem rv = parentItem;
		while( rv.getExpanded() && rv.getItemCount() > 0 ) {
			rv = rv.getItem(rv.getItemCount()-1);
		}
		return rv;
	}
		
	private TreeItem findNextItem(TreeItem item) {
		TreeItem rv = null;
		Tree tree = item.getParent();
		TreeItem parentItem = item.getParentItem();
		
		int nextIndex;
		int totalItems;
		
		if( parentItem == null ) {
			nextIndex = tree.indexOf(item) + 1;
			totalItems = tree.getItemCount();
		} else {
			nextIndex = parentItem.indexOf(item) + 1;
			totalItems = parentItem.getItemCount();
		}
		
		// This is once more the last item in the tree
		// Search on
		if( nextIndex == totalItems ) {
			if( item.getParentItem() != null ) {
				rv = findNextItem(item.getParentItem());
			}
		} else {
			if( parentItem == null ) {
				rv = tree.getItem(nextIndex);
			} else {
				rv = parentItem.getItem(nextIndex);
			}
		}
		
		return rv;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getTreePath()
	 */
	@Override
	public TreePath getTreePath() {
		TreeItem tItem = getItem();
		LinkedList<Object> segments = new LinkedList<Object>();
		while (tItem != null) {
			Object segment = tItem.getData();
			Assert.isNotNull(segment);
			segments.addFirst(segment);
			tItem = tItem.getParentItem();
		}
		return new TreePath(segments.toArray());
	}
	 		
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#clone()
	 */
	@Override
	public STTreeViewerRow clone() {
		return new STTreeViewerRow(getItem());
	}
}
