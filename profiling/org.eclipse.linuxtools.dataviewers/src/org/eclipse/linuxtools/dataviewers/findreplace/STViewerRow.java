package org.eclipse.linuxtools.dataviewers.findreplace;

import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

/**
 * 
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 * @param <T> one of {@link TreeItem}, {@link TableItem}
 */
public abstract class STViewerRow<T extends Item> extends ViewerRow {

	private final T item;
	
	public STViewerRow(T item) {
		this.item = item;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getItem()
	 */
	@Override
	public T getItem() {
		return item;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getElement()
	 */
	@Override
	public Object getElement() {
		return item.getData();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getNeighbor(int, boolean)
	 */
	@Override
	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		if( direction == ViewerRow.ABOVE ) {
			return getRowAbove(sameLevel);
		} else if( direction == ViewerRow.BELOW ) {
			return getRowBelow(sameLevel);
		} else {
			throw new IllegalArgumentException("Illegal value of direction argument."); //$NON-NLS-1$
		}
	}


	protected abstract ViewerRow getRowBelow(boolean sameLevel);

	protected abstract ViewerRow getRowAbove(boolean sameLevel);
	
}
