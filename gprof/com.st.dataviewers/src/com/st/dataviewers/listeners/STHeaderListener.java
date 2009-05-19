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
package com.st.dataviewers.listeners;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Item;

import com.st.dataviewers.abstractviewers.AbstractSTViewer;
import com.st.dataviewers.abstractviewers.ISTDataViewersField;
import com.st.dataviewers.abstractviewers.STDataViewersComparator;

/**
 * Handles the case of user selecting the header area.
 */
public class STHeaderListener implements SelectionListener {

	private AbstractSTViewer stViewer;
	
	public STHeaderListener(AbstractSTViewer stViewer){
		this.stViewer = stViewer;
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		final Item column = (Item) e.widget;
		final ISTDataViewersField field = (ISTDataViewersField) column.getData();
		resortTable(column, field);
	}

	/**
	 * Resort the table based on field.
	 * 
	 * @param column
	 *            the column being updated
	 * @param field
	 * @param monitor
	 */
	private void resortTable(final Item column,final ISTDataViewersField field) {
		STDataViewersComparator sorter = stViewer.getTableSorter();

		if (column.equals(sorter.getTopColumn()))
			sorter.reverseTopPriority();
		else
			sorter.setTopPriority(column, field);
		
		BusyIndicator.showWhile(null,new Runnable() {
			public void run() {
				stViewer.getViewer().refresh();
				stViewer.updateDirectionIndicator(column);
			}
		});
	}
}
