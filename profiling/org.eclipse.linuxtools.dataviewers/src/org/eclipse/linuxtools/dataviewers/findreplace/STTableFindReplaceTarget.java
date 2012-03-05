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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTableViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.listeners.ISpecialDrawerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


/**
 * This class implements an ISTFindReplaceTarget for an abstractSTTableViewer instance
 * It use a representation of a ViewerRow that is an instance of STTableViewerRow
 * so that can be applied the "FIND algorithm" using the methods like: 
 * getCell(...)
 * getNeighbor(...)
 * setBackground(...)
 * ... 
 *
 */
public abstract class STTableFindReplaceTarget extends AbstractSTTableViewer implements ISTFindReplaceTarget{
	private TableViewer _viewer;
	private STFindReplaceAction action;
	private boolean scope;
	private STTableViewerRow fRow;
	
	public STTableFindReplaceTarget(Composite parent) {
		super(parent, SWT.BORDER |SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI| SWT.FULL_SELECTION);
		_viewer = getViewer();
	
		addSelectionListener();
	}
	
	
	public STTableFindReplaceTarget(Composite parent,boolean init) {
		super(parent,SWT.BORDER |SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI| SWT.FULL_SELECTION,init);
		_viewer = getViewer();
		addSelectionListener();
	}
	
	public STTableFindReplaceTarget(Composite parent, int style) {
		super(parent,style,true);
		_viewer = getViewer();
		addSelectionListener();
	}
	
	public STTableFindReplaceTarget(Composite parent, int style,boolean init) {
		super(parent,style,init);
		_viewer = getViewer();
		addSelectionListener();
	}

	public boolean canPerformFind() {
		if (_viewer != null && _viewer.getInput() != null)
			return true;
		return false;
	}

	public ViewerCell findAndSelect(ViewerCell widgetOffset, String findString,
			boolean searchForward, boolean caseSensitive, boolean wholeWord,boolean wrapSearch,boolean regExSearch) {
		return findAndSelect(widgetOffset,findString,searchForward,searchForward,caseSensitive, wholeWord,wrapSearch,regExSearch); 
	}

	public ViewerCell getSelection(ViewerCell index) {
		if (index == null){
			if (fRow != null)
				return fRow.getCell(0);
			else{
				fRow = new STTableViewerRow(_viewer.getTable().getItem(0));
				return fRow.getCell(0);
			}
		}

		return index;
	}
	
	public String getSelectionText(ViewerCell index) {
		if (index == null){
			if (fRow != null)
				return fRow.getCell(0).getText();
			else{
				fRow = new STTableViewerRow(_viewer.getTable().getItem(0));
				return fRow.getCell(0).getText();
			}
		}
		return index.getText();
	}

	public Boolean isEditable() {
		return false;
	}
	
	private ViewerCell findAndSelect(ViewerCell cell, String findString,
			boolean searchForward, boolean direction,boolean caseSensitive, boolean wholeWord,boolean wrapSearch,boolean regExSearch) {
		
		if (cell == null) return null;
		
		int dirCell = ViewerCell.RIGHT;
		
		if (!searchForward)
			dirCell = ViewerCell.LEFT;
		
		Table table = _viewer.getTable();
		

		if (!scope || table.isSelected(table.indexOf((TableItem)cell.getItem()))){
			ViewerCell cellFound = searchInRow(cell.getViewerRow(),cell.getColumnIndex(),findString,searchForward,caseSensitive,wholeWord,dirCell,regExSearch);
		
			if( cellFound != null) return cellFound;
		}

		dirCell = ViewerCell.RIGHT;
		
		int dirRow = 0;
		if (searchForward)
			dirRow = ViewerRow.BELOW;
		else
			dirRow = ViewerRow.ABOVE;
			
		ViewerRow row = cell.getViewerRow();
		
		if (table.getSelectionCount() == 0){
			while (row.getNeighbor(dirRow, true) != null){
				row = row.getNeighbor(dirRow, true);
				cell = searchInRow(row,0,findString,searchForward,caseSensitive,wholeWord,dirCell,regExSearch);
				if (cell != null)
					return cell;
			}
		}
		else{
			while (row.getNeighbor(dirRow, true) != null){
				row = row.getNeighbor(dirRow, true);
				if (!scope || table.isSelected(table.indexOf((TableItem)row.getItem()))){
					cell = searchInRow(row,0,findString,searchForward,caseSensitive,wholeWord,dirCell,regExSearch);
					if (cell != null)
						return cell;
				}
			}
		}
	
		return null;
		
	}
	
	private ViewerCell searchInRow(ViewerRow row,int index,String findString,boolean searchForward,boolean caseSensitive, boolean wholeWord,int dirCell,boolean regExSearch){
		Pattern pattern = null;
		if (regExSearch){
			 pattern = Pattern.compile(findString);
		}
		
		ISTDataViewersField[] fields = getAllFields();
		
		ViewerCell cell = row.getCell(index);

		do{
			String text = "";
			
			ISTDataViewersField field = fields[cell.getColumnIndex()];
			if (field.getSpecialDrawer(cell.getElement()) != null){
				ISpecialDrawerListener hfield = (ISpecialDrawerListener)field;
				text = hfield.getValue(cell.getElement()).trim();
			}
			else	
				text = cell.getText().trim();
			boolean ok = false;
			
			if (regExSearch){
				Matcher matcher = pattern.matcher(text);
				ok = matcher.find();
			} else {
				if (wholeWord){
					if (caseSensitive)
						ok = text.equals(findString);
					else
						ok = text.equalsIgnoreCase(findString);
				} else{
					if (caseSensitive)
						ok = text.contains(findString);
					else 
						ok = text.toLowerCase().contains(findString.toLowerCase());
				}
			}	

			if (ok){
				_viewer.reveal(cell.getElement());
				if (((TableViewer)_viewer).getTable().getSelectionCount() > 0){
					TableViewer tv = (TableViewer)_viewer;
					Table table = tv.getTable();
					table.deselect(table.indexOf((TableItem)row.getItem()));
				}
					
				return cell;
				
			}
			cell = cell.getNeighbor(dirCell, true);
		}
		while(cell != null);
		
		return null;
	}

	
	private void addSelectionListener(){
		_viewer.addSelectionChangedListener(new ISelectionChangedListener(){

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (_viewer.getTable().getSelectionCount() > 0)
					fRow = new STTableViewerRow(_viewer.getTable().getSelection()[0]);
				else
					fRow = null;
			}
			
		});
	}
	
	protected Table createTable(Composite parent, int style) {
		Table table = new Table(parent, style);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addPaintListener(new PaintListener(){

			@Override
			public void paintControl(PaintEvent e) {
				if (action != null) action.setEnabled(canPerformFind());
				
			}
			
		});
		
		return table;
	}
	
	
	public void setFindAction(STFindReplaceAction action){
		this.action = action;
	}
	
	public ViewerCell getFirstCell(ViewerCell start,int direction)
	{
		ViewerRow row = null;
		
		if (direction == ViewerRow.ABOVE){
			if (scope && _viewer.getTable().getSelectionCount() > 0)
				row = new STTableViewerRow(_viewer.getTable().getSelection()[0]);
			else
				row = new STTableViewerRow(_viewer.getTable().getItem(0));
		}
		else{
			if (scope && _viewer.getTable().getSelectionCount() > 0)
				row = new STTableViewerRow(_viewer.getTable().getSelection()[_viewer.getTable().getSelection().length -1]);
			else
				row = new STTableViewerRow(_viewer.getTable().getItem(_viewer.getTable().getItemCount()-1));
		}
		
		return row.getCell(0);
	}
	
	public void useSelectedLines(boolean use){
		this.scope = use;
	}
	

	
}
