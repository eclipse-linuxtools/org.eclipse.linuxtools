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

import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.dataviewers.listeners.STColumnSizeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Item;


/*
 * This class is used to handle the hide/show column state.  
 * It also handles the save and restore into the .setting    
 */
public class STDataViewersHideShowManager {
	
	public static final int STATE_SHOWN = 1;
	
	public static final int STATE_HIDDEN = 0;
	
	private int[] defaultColumnsWidth;
	
	private int[] columnsWidth;
	
	private int[] columnsState;
	
	private Item[] columns;
	
	private AbstractSTViewer stViewer;
	
	private HashMap<Item,STColumnSizeListener> columnsSizeListener = new HashMap<Item,STColumnSizeListener>();
	
	/*
	 * Creates a new instance of STDataViewersHideShowManager
	 * Adding a ColumnSizeListener in order handle the column width  
	 */
	public STDataViewersHideShowManager(AbstractSTViewer stViewer) {
		this.stViewer = stViewer;
		
		columns = stViewer.getColumns();
		int[] widths = new int[columns.length];
		int[] states = new int[columns.length];
		for (int i = 0; i < columns.length; i++) {
			widths[i] = stViewer.getColumnWidth(columns[i]);
			states[i] = STATE_SHOWN;
		}
		
		this.columnsWidth = widths;
		this.columnsState = states;
		
		this.defaultColumnsWidth = new int[columns.length];
		for (int i = columns.length; i-->0;) {
			ISTDataViewersField field = (ISTDataViewersField)columns[i].getData();
			this.defaultColumnsWidth[i] = field.getPreferredWidth();
			STColumnSizeListener l = new STColumnSizeListener(this);
			columnsSizeListener.put(columns[i], l);
			columns[i].addListener(SWT.Resize, l);
			columns[i].addDisposeListener(new DisposeListener(){

				public void widgetDisposed(DisposeEvent e) {
					Item column = (Item)e.widget;
					column.removeListener(SWT.Resize, columnsSizeListener.get(column));
					
				}
				
			});
		}
	}
	
	/*
	 * It saves the hide/show column state inside the .setting
	 */
	public void saveState(IDialogSettings dialogSettings) {
		// delete old settings and save new ones
		IDialogSettings settings = dialogSettings.addNewSection(STDataViewersSettings.TAG_SECTION_HIDESHOW);
		
		for (int i = 0; i < columnsWidth.length; i++) {
			settings.put(
					STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_WIDTH_ + i,
					columnsWidth[i]);
		}
		
		for (int i = 0; i < columnsState.length; i++) {
			settings.put(
					STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_STATE_ + i,
					columnsState[i]);
		}	
	}
	
	
	/*
	 * Restores the columns width and the columns state using the columns state saved into the .setting
	 * 
	 * @param dialogSettings
	 */
	public void restoreState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			// no settings section
			resetState();
			return;
		}
		
		IDialogSettings settings = dialogSettings.getSection(STDataViewersSettings.TAG_SECTION_HIDESHOW);
		
		if (settings == null) {
			// no settings saved
			resetState();
			return;
		}
		
		try {
			for (int i = 0; i < columnsWidth.length; i++) {
				String width = settings.get(
						STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_WIDTH_ + i);
				
				if (width == null) {
					// no width data
					resetState();
					return;
				}
				
				columnsWidth[i] = Integer.parseInt(width);
			}
			
			for (int i = 0; i < columnsState.length; i++) {
				String state = settings.get(
						STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_STATE_ + i);
				
				if (state == null) {
					// no state data
					resetState();
					return;
				}
				
				columnsState[i] = Integer.parseInt(state);
			}
		} catch (NumberFormatException nfe) {
			// invalid entry
			resetState();
			return;
		}
	}

	/*
	 * It restores the original columns width  
	 */
	private void resetState() {
		columnsWidth = defaultColumnsWidth;
		for (int i = 0; i < columnsState.length; i++) {
			columnsState[i] = STATE_SHOWN;
		}	
	}
	
	/*
	 * It sets the column width
	 * 
	 * @param index of column
	 * @param width 
	 */
	public void setWidth(int index, int width) {
		if (columnsState[index] != STATE_HIDDEN) {
			columnsWidth[index] = width;
		}
		// ignore if this column is set to hidden
	}
	
	/*
	 * It sets the state of column
	 * 
	 * @param index of the column
	 * @state can be: STATE_SHOWN or STATE_HIDDEN
	 */
	public void setState(int index, int state) {
		columnsState[index] = state;
	}
	
	/*
	 * Gets the column width
	 * @param index of the column
	 */
	public int getWidth(int index) {
		return columnsWidth[index];
	}
	
	/*
	 * Gets the column state which can be: STATE_SHOWN or STATE_HIDDEN
	 * @param index of the column
	 */
	public int getState(int index) {
		return columnsState[index];
	}
	
	/*
	 * Gets the all columns width of the STViewer
	 * @return int[]
	 */
	public int[] getColumnsWidth() {
		return columnsWidth;
	}

	/*
	 * Gets the all columns state of the STViewer
	 * @return int[]
	 */
	public int[] getColumnsState() {
		return columnsState;
	}

	/*
	 * Updates the columns width
	 * 
	 *  @param column
	 */
	public void updateColumns(Item[] columns) {
		for (int i = columns.length; i-->0;) {
			Item column = columns[i];
			if (getState(i) == STDataViewersHideShowManager.STATE_HIDDEN) {
				stViewer.setColumnWidth(column, 0);
				stViewer.setColumnResizable(column, false);
			} else {
				stViewer.setColumnWidth(column, getWidth(i));
				stViewer.setColumnResizable(column, true);
			}
		}
	}
	
	/*
	 * Gets the STViewer hooked to this Hide/Show Manager
	 * @return AbstractSTViewer
	 */
	public AbstractSTViewer getSTViewer(){
		return stViewer;
	}

}
