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

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This wrapper extends AbstractSTViewer {@link AbstractSTViewer} It is designed to be instantiated with a TableViewer
 * JFace control
 *
 */
public abstract class AbstractSTTableViewer extends AbstractSTViewer {

	public AbstractSTTableViewer(Composite parent) {
		super(parent, SWT.BORDER |SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI| SWT.FULL_SELECTION);
	}


	public AbstractSTTableViewer(Composite parent,boolean init) {
		super(parent,SWT.BORDER |SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI| SWT.FULL_SELECTION,init);
	}

	public AbstractSTTableViewer(Composite parent, int style) {
		super(parent,style,true);
	}

	public AbstractSTTableViewer(Composite parent, int style,boolean init) {
		super(parent,style,init);
	}

	/**
	 * It creates the wrapped TableViewer
	 * @param parent - the parent Composite
	 * @param style - the table style
	 * @return a TableViewer
	 * @since 5.0
	 */
	@Override
	protected TableViewer createViewer(Composite parent, int style) {
		Table t = createTable(parent, style);
		return new TableViewer(t);
	}

	/**
	 * Create the main table control
	 *
	 * @param parent
	 * @return Table
	 */
	protected Table createTable(Composite parent, int style) {
		Table table = new Table(parent, style);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		return table;
	}

	@Override
	/**
	 * Create the columns in the table.
	 *
	 */
	protected void createColumns() {
		Table table = getViewer().getTable();
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		for (int i = 0; i < getAllFields().length; i++) {
			ISTDataViewersField field = getAllFields()[i];
			TableColumn tc = new TableColumn(table, field.getAlignment(), i);
			tc.setText(field.getColumnHeaderText());
			tc.setToolTipText(field.getColumnHeaderTooltip());
			tc.setImage(field.getColumnHeaderImage());
			tc.setWidth(field.getPreferredWidth());
			tc.setResizable(true);
			tc.setMoveable(true);

			tc.addSelectionListener(createHeaderListener());
			tc.setData(field);

			// defining the column label provider.
			// this has to be done after setting the column's data.
			TableViewerColumn viewerColumn =
				new TableViewerColumn(getViewer(), tc);
			viewerColumn.setLabelProvider(createColumnLabelProvider(tc));
		}


		table.addMouseListener(new MouseAdapter(){
					@Override
                    public void mouseDoubleClick(MouseEvent e) {
						Table table = (Table)e.widget;
						TableItem item = table.getItem(new Point(e.x,e.y));
						if (item != null){
							for(int i=0;i<table.getColumnCount();i++){
								ISTDataViewersField field = getAllFields()[i];
								if (field.isHyperLink(item.getData())){
									Rectangle bounds = item.getBounds(i);
									if (bounds.contains(e.x,e.y)){
										handleHyperlink(field,item.getData());
										return;
									}
								}
							}
						}
					}
				});

		table.addMouseMoveListener(new MouseMoveListener(){

						@Override
						public void mouseMove(MouseEvent e) {
							Table table = (Table)e.widget;
							TableItem item = table.getItem(new Point(e.x,e.y));
							if (item == null) return;

							for(int i=0;i<table.getColumnCount();i++){
								ISTDataViewersField field = getAllFields()[i];
								Cursor cursor = null ;
								if (field.isHyperLink(item.getData())){
									Rectangle bounds = item.getBounds(i);
								if (bounds.contains(e.x,e.y)){
										cursor = e.display.getSystemCursor(SWT.CURSOR_HAND);
										table.setCursor(cursor);
										return;
									}
								}
								cursor = e.display.getSystemCursor(SWT.CURSOR_ARROW);
								table.setCursor(cursor);
							}

						}

					});
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getColumns()
     * @since 5.0
	 */
	@Override
	public TableColumn[] getColumns() {
		return getViewer().getTable().getColumns();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#updateDirectionIndicator(org.eclipse.swt.widgets.Item)
	 */
	@Override
	public void updateDirectionIndicator(Item column) {
		getViewer().getTable().setSortColumn((TableColumn)column);
		if (getTableSorter().getTopPriorityDirection() == STDataViewersComparator.ASCENDING)
			getViewer().getTable().setSortDirection(SWT.UP);
		else
			getViewer().getTable().setSortDirection(SWT.DOWN);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getColumnOrder()
	 */
	@Override
    public int[] getColumnOrder() {
		return getViewer().getTable().getColumnOrder();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#setColumnOrder(int[])
	 */
	@Override
    protected void setColumnOrder(int[] order) {
		getViewer().getTable().setColumnOrder(order);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getColumnIndex(org.eclipse.swt.widgets.Item)
	 */
	@Override
    public int getColumnIndex(Item column) {
		return getViewer().getTable().indexOf((TableColumn)column);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getColumnWidth(org.eclipse.swt.widgets.Item)
	 */
	@Override
	public int getColumnWidth(Item column) {
		return ((TableColumn)column).getWidth();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#setColumnResizable(org.eclipse.swt.widgets.Item, boolean)
	 */
	@Override
	public void setColumnResizable(Item column, boolean resizable) {
		((TableColumn)column).setResizable(resizable);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#setColumnWidth(org.eclipse.swt.widgets.Item, int)
	 */
	@Override
	public void setColumnWidth(Item column, int width) {
		((TableColumn)column).setWidth(width);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getViewer()
	 */
	@Override
    public TableViewer getViewer() {
		return (TableViewer)super.getViewer();
	}


	public abstract void handleHyperlink(ISTDataViewersField field,Object data);
}
