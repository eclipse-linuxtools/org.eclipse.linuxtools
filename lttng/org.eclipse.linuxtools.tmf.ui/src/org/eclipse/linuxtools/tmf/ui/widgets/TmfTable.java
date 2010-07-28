/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Francois Chouinard - Refactoring and minor adjustments
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TmfVirtualTable</u></b>
 * <p>
 * The TmfTable is a table that allows scrolling through arbitrarily large
 * set of TmfEvents.
 * 
 * It is a composite of 2 synchronized widgets that simulates an SWT table:
 * - a fixed-size Table acting as a window on the data (like a regular table) 
 * - a Slider acting as a vertical scroll bar.
 */
public class TmfTable extends Composite {

	// The "fixed" size table
	private Table fTable          = null;
	private int   fFirstRowOffset = 0;
	private int   fRelativeRow    = 0; 
	private int   fAbsoluteRow    = 0; 
	private int   fItemCount      = 0;

	// Selection handling
	private TableItem fSelectedItem[] = null;

	// The slider
	private Slider fSlider;

	// A placeholder for the controls (table and slider)
	private Control fControl[]; 

	// ???
	private int fSliderMax; 
	private TableItem fTableItems[];
	private int fNumberOfVisibleRows;


	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TmfTable(Composite parent, int style, ColumnData columnData[]) {
		super(parent, style);

		// Create the table widget
		createEventsTable(style, columnData);

		initialize();
	}

	// ------------------------------------------------------------------------
	// Table
	// ------------------------------------------------------------------------

	private void createEventsTable(int style, ColumnData columnData[]) {

		// Adjust the style
		int tableStyle = style | (SWT.NO_SCROLL) & (~SWT.MULTI) & (~SWT.V_SCROLL) & (~SWT.VIRTUAL);
		fTable = new Table(this, tableStyle);
		setColumnHeaders(columnData);

		fTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleTableSelection();
			}
		});

		fTable.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleTableKeyEvent(event);
			}
			public void keyReleased(KeyEvent event) {
			}
		});
	}

	public void setItemCount(int count)
	{
		count = (count > 0) ? count : 0;
		if (count != fItemCount) {
			fItemCount = count; 
			resize();
		}
	}

	/*
	 * setColumnHeaders
	 * 
	 * @param columnData
	 */
	private void setColumnHeaders(ColumnData columnData[]) {
		for (int i = 0; i < columnData.length; i++) {
            TableColumn column = new TableColumn(fTable, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            column.setWidth(columnData[i].width);
        }
	}

	/*
	 * handleTableSelection
 	 *
 	 * Update the selection and broadcast the timestamp of the selected event
	 */
	private void handleTableSelection() {
		fRelativeRow     = fTable.getSelectionIndices()[0];
		fAbsoluteRow     = fRelativeRow + fFirstRowOffset; 
		fSelectedItem    = new TableItem[1];
		fSelectedItem[0] = fTable.getSelection()[0];

		TmfTimestamp ts = (TmfTimestamp) fTable.getSelection()[0].getData();
		TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(fTable, ts));
	}

	/*
	 * handleTableKeyEvent
	 * 
	 * Allow the selection to move within the visible area of the table only
	 * Simulate scrolling by adjusting fFirstRowOffset and re-populating the table
	 * 
	 * @param event
	 */
	private void handleTableKeyEvent(KeyEvent event)
	{
		boolean updateDisplay = false; 
		int firstRow = 0;
		int lastRow  = fItemCount - 1;
		int firstRowOfLastPage = (fItemCount > fNumberOfVisibleRows) ? (lastRow - fNumberOfVisibleRows + 1) : 0;

		switch (event.keyCode) {

			case SWT.ARROW_DOWN: {
				event.doit = false;
				if (fAbsoluteRow < lastRow) {
					fAbsoluteRow++;
					if (fRelativeRow < fNumberOfVisibleRows - 1) {
						fRelativeRow++;
						fTable.setSelection(fRelativeRow);
					} else {
						fFirstRowOffset++;
						updateDisplay = true;
					}
				}
				break;
			}

			case SWT.ARROW_UP: {
				event.doit = false;
				if (fAbsoluteRow > firstRow) {
					fAbsoluteRow--;
					if (fRelativeRow > 0) {
						fRelativeRow--;
						fTable.setSelection(fRelativeRow);
					} else {
						fFirstRowOffset--;
						updateDisplay = true;
					}
				}
				break;
			}

			case SWT.PAGE_DOWN: {
				event.doit = false;
				if (fAbsoluteRow < lastRow) {
					fAbsoluteRow += fNumberOfVisibleRows;
					if (fAbsoluteRow > lastRow) {
						fAbsoluteRow    = lastRow;
						fFirstRowOffset = firstRowOfLastPage;
						fRelativeRow    = fNumberOfVisibleRows - 1;
						fTable.setSelection(fRelativeRow);
					} else {
						fFirstRowOffset += fNumberOfVisibleRows;
					}
					updateDisplay = true;
				}
				break;
			}

			case SWT.PAGE_UP: {
				event.doit = false;
				if (fAbsoluteRow > firstRow) {
					fAbsoluteRow -= fNumberOfVisibleRows;
					if (fAbsoluteRow < firstRow) {
						fAbsoluteRow    = firstRow;
						fFirstRowOffset = firstRow;
						fRelativeRow    = firstRow;
						fTable.setSelection(fRelativeRow);
					} else {
						fFirstRowOffset -= fNumberOfVisibleRows;
					}
					updateDisplay = true;
				}
				break;
			}

			case SWT.HOME: {
				event.doit = false;
				fAbsoluteRow = firstRow;
				fRelativeRow = firstRow;
				fTable.setSelection(fRelativeRow);
				if (fFirstRowOffset != firstRow) {
					fFirstRowOffset  = firstRow;
					updateDisplay = true;
				}
				break;
			}

			case SWT.END: {
				event.doit = false;
				fAbsoluteRow = lastRow;
				fRelativeRow = fNumberOfVisibleRows - 1;
				fTable.setSelection(fRelativeRow);
				if (fFirstRowOffset != firstRowOfLastPage) {
					fFirstRowOffset  = firstRowOfLastPage;
					updateDisplay = true;
				}
				break;
			}

		};

		if (updateDisplay) {
			refresh();
			fTable.setSelection(fRelativeRow);
			fSlider.setSelection(fFirstRowOffset + fRelativeRow);
		}

	}

	public void refresh() {
		for (int i = 0; i < fTableItems.length; i++) {
			setDataItem(fTableItems[i]);
		}
	}

	private void setDataItem(TableItem item) {
		int index = fTable.indexOf(item); 
		if( index != -1) {
			Event event = new Event();
			event.item  = item;
			event.index = index + fFirstRowOffset;
			event.doit  = true;
			notifyListeners(SWT.SetData, event);
		}
	}

	// ------------------------------------------------------------------------
	// Table
	// ------------------------------------------------------------------------

	/**
	 * 
	 */
	private void initialize() {

		// Instantiate the table
//		fTable = new Table(this, fTableStyle);

		// Instantiate the slider
		fSlider = new Slider(this, SWT.VERTICAL);
		fSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setSelection();
			}
		}); 

		// Fill the control placeholder
		fControl = new Control[2];
		fControl[0] = fTable;
		fControl[1] = fSlider;

		// Set the initial layout
		Rectangle bounds = getClientArea();
		fTable.setSize(bounds.width - 24, bounds.height);
		fSlider.setSize(24, bounds.height);

		// Add the widget-level listeners
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				resize(); 
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent event) {
				fFirstRowOffset -= event.count;
				if (fFirstRowOffset > fSliderMax) {
					fFirstRowOffset = fSliderMax;
				} else if (fFirstRowOffset < 0) {
					fFirstRowOffset = 0;
				}
				fSlider.setSelection(fFirstRowOffset);
				setSelection();
			}
		});
	}
	
	private void setSelection() {
		if ((fAbsoluteRow > fFirstRowOffset)
				&& (fAbsoluteRow < (fFirstRowOffset + fNumberOfVisibleRows))) {
			fRelativeRow = fAbsoluteRow - fFirstRowOffset;
			fTable.setSelection(fRelativeRow);

		} else {
			fTable.deselect(fRelativeRow);
		}

		for (int i = 0; i < fNumberOfVisibleRows; i++) {
			setDataItem(fTableItems[i]);
		}
	}

	// ------------------------------------------------------------------------
	// Control event handlers
	// ------------------------------------------------------------------------



	
	// ------------------------------------------------------------------------
	// Composite
	// ------------------------------------------------------------------------

	@Override
	public Control[] getChildren() {
		return fControl;
	}

	@Override
	public boolean setFocus() {
		boolean isVisible = isVisible();
		if (isVisible) {
			for (int i = 0; i < fControl.length; i++) {
				fControl[i].setFocus();
			}
		}
		return isVisible;
	}
	
	// ------------------------------------------------------------------------
	// Table API 
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// 
	// ------------------------------------------------------------------------

	/*
	 * 
	 */
	public TableItem[] getSelection()
	{
		return fSelectedItem; 
	}
	
	public void setLinesVisible( boolean b)
	{
		fTable.setLinesVisible(b);
	}

	public void addSelectionListener( SelectionAdapter sa)
	{
		fTable.addSelectionListener(sa);
	}
	
	
	
	
	/*
	 * 
	 */
	public void setHeaderVisible(boolean b) {
		fTable.setHeaderVisible(b);
	}
	/*
	 * 
	 */
	public void resize()
	{
		Rectangle bounds = this.getClientArea();
		int sl_width = fSlider.getBounds().width;
		int tab_width = bounds.width - sl_width;
		int tab_height = bounds.height - 10;
		int item_height = fTable.getItemHeight();
		fNumberOfVisibleRows = tab_height/item_height;
		if( fItemCount == 0)
			fNumberOfVisibleRows = 0;
		if( fNumberOfVisibleRows > 0 )
		{
			if( fTable.getItemCount() != fNumberOfVisibleRows)
			{
				int delta = fTable.getItemCount() - fNumberOfVisibleRows;
				if(delta != 0)
				{
					fTable.removeAll();
					if(fTableItems != null)
					{
						for( int i = fTableItems.length-1 ; i > 0 ; i-- )
						{
							if( fTableItems[i] != null )
							{
								fTableItems[i].dispose();
							}
							fTableItems[i] = null;
						}
					}
					fTableItems = new TableItem[fNumberOfVisibleRows];
					for( int i = 0 ; i < fTableItems.length; i++ )
					{
						fTableItems[i]= new TableItem(fTable, i);
					}
				}
			}
			if( fItemCount < fNumberOfVisibleRows )
			{
				fTable.setBounds(bounds);
				fTable.setSize(bounds.width, bounds.height+6);
				fSlider.setVisible(false);
			}
			else
			{
				fTable.setBounds(0, 0, tab_width, bounds.height);
				fTable.setSize(tab_width,bounds.height+6);
				fSlider.setBounds(tab_width, 0, sl_width, bounds.height);
				fSlider.setSize(sl_width, bounds.height);
				fSlider.setVisible(true);
				fSliderMax =  fItemCount - fNumberOfVisibleRows;
				fSlider.setMaximum( fSliderMax);
				fSlider.setMinimum(0);
			}	
		}
		else
		{
			fTable.setBounds(bounds);
			fTable.setSize(bounds.width, bounds.height+6);
			fSlider.setVisible(false);
		}
	}

    

	public int getTopIndex() {
		
		return fFirstRowOffset;
	}

	public void setTopIndex( int i){
		fSlider.setSelection(i);
	}
	
	public int indexOf(TableItem ti)
	{
		return fTable.indexOf(ti) +  getTopIndex();
	}
	
	public int removeAll()
	{
		fSlider.setMaximum(0);
		fTable.removeAll();
		return 0;
	}

	public void setSelection(int i)
	{
		if (fTableItems != null)
		{
			i = Math.min(i, fItemCount);
			i = Math.max(i, 0);
			fSlider.setSelection(i);
			setSelection();
		}
	}

//	public void createColumnHeaders(ColumnData[] columnData, boolean b) {
//		for (int i = 0; i < columnData.length; i++) {
//            final TableColumn column = new TableColumn(this.fTable, columnData[i].alignment, i);
//            column.setText(columnData[i].header);
//            column.setWidth(columnData[i].width);
//            // TODO: Investigate why the column resizing doesn't work by default
//            // Anything to do with SWT_VIRTUAL?
//            column.addSelectionListener(new SelectionListener() {
//				public void widgetDefaultSelected(SelectionEvent e) {
//					// TODO Auto-generated method stub
//				}
//				public void widgetSelected(SelectionEvent e) {
//					column.pack();
//				}
//            });
//        }
//		
//	}

}