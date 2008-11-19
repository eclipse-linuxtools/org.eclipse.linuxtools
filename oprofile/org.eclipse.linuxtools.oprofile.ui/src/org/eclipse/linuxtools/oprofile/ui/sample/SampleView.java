/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.ui.sample;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUIMessages;
import org.eclipse.linuxtools.oprofile.ui.internal.IProfileElement;
import org.eclipse.linuxtools.oprofile.ui.system.SystemProfileExecutable;
import org.eclipse.linuxtools.oprofile.ui.system.SystemProfileShLib;
import org.eclipse.linuxtools.oprofile.ui.system.SystemProfileSymbol;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class SampleView extends ViewPart
{
	private TableViewer _viewer;
	private IMemento _memento = null;

	// Persistence tags
	private static final String TAG_COLUMN = "column"; //$NON-NLS-1$
	private static final String TAG_NUMBER = "number"; //$NON-NLS-1$
	private static final String TAG_WIDTH = "width"; //$NON-NLS-1$
	private static final String TAG_SORT_COLUMN = "sorterColumn"; //$NON-NLS-1$
	private static final String TAG_SORT_REVERSED = "sorterReversed"; //$NON-NLS-1$

	public static final int COLUMN_PERCENT = 0;
	public static final int COLUMN_SAMPLES = 1;
	public static final int COLUMN_LINE = 2;
	public static final int COLUMN_VMA = 3;
	public static final int COLUMN_NAME = 4;

	private static String[] _columnHeaders =
	{
		OprofileUIMessages.getString("sampleview.table.column.percentage.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.count.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.line.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.vma.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.name.text") //$NON-NLS-1$
	};
	
	private static ColumnLayoutData[] _columnLayouts =
	{
		// FIXME SUCK: There must be a better way to get the length of
		// strings as they would be displayed on the screen... For now
		// just set something sane. The user can resize the columns.
		//
		// .. there is tablecolumn.pack(), but it cuts it a bit too
		//    close to the edge, and makes loading the samples 
		//    unbelievably slow; these new values seem ok  --ksebasti
		new ColumnPixelData(60, true),
		new ColumnPixelData(95, true),
		new ColumnPixelData(75, true),
		new ColumnPixelData(95, true),
		new ColumnWeightData(1, false)
	};
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent)
	{
		Table table = new Table(parent, SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		
		_viewer = new TableViewer(table);
		_viewer.setContentProvider(new SampleViewContentProvider());
		_viewer.setLabelProvider(new SampleViewLabelProvider());
		_viewer.addOpenListener(new IOpenListener()
		{
			public void open(OpenEvent oe)
			{
				_showSelectionInEditor();
			}
		});

		// Create columns
		_createColumns(table);

		// Restore column sorter
		SampleSorter sorter = null;
		if (_memento != null)
		{
			Integer sortColumn = _memento.getInteger(TAG_SORT_COLUMN);
			if (sortColumn != null)
			{
				boolean reversed = (_memento.getInteger(TAG_SORT_REVERSED).intValue() == 1);
				sorter = new SampleSorter(sortColumn.intValue());
				sorter.setReversed(reversed);
			}
		}

		if (sorter == null)
		{
			// By default, sort by reverse sample count
			sorter = new SampleSorter(COLUMN_SAMPLES);
			sorter.setReversed(true);
		}
		_viewer.setSorter(sorter);
		
		// Clear the memento
		_memento = null;

		// Register ourselves as the sample viewer for the profile views
		OprofileUiPlugin.getDefault().setSampleView(this);
	}
	
	
	protected void _createColumns(Table table)
	{
		SelectionListener headerListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				int column = _viewer.getTable().indexOf((TableColumn) se.widget);
				SampleSorter oldSorter = (SampleSorter) _viewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber())
				{
					oldSorter.setReversed(!oldSorter.isReversed());
					_viewer.refresh();
				}
				else
					_viewer.setSorter(new SampleSorter(column));
			}
		};
		
		if (_memento != null)
		{
			IMemento[] children = _memento.getChildren(TAG_COLUMN);
			if (children != null)
			{
				for (int i = 0; i < children.length; ++i)
				{
					Integer val = children[i].getInteger(TAG_NUMBER);
					if (val != null)
					{
						int idx = val.intValue();
						val = children[i].getInteger(TAG_WIDTH);
						if (val != null)
							_columnLayouts[idx] = new ColumnPixelData(val.intValue(), true);
					}
				}
			}
		}
		
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		for (int i = 0; i < _columnHeaders.length; ++i)
		{
			layout.addColumnData(_columnLayouts[i]);
			TableColumn column = new TableColumn(table, SWT.NONE, i);
			column.setResizable(_columnLayouts[i].resizable);
			column.setText(_columnHeaders[i]);
			column.addSelectionListener(headerListener);
		}		
	}	
	
	public void dispose()
	{
		// Unregister ourselves from the profile views
		OprofileUiPlugin.getDefault().setSampleView(null);
	}
		

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		_viewer.getTable().setFocus();
	}
	
	public void setInput(IProfileElement input)
	{
		_viewer.setInput(input);
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);
		_memento = memento;
	}
	
	public void saveState(IMemento memento)
	{
		if (_viewer == null)
		{
			if (_memento != null)
				memento.putMemento(_memento);
		}
		else
		{
			// Save sort column and reverse state
			SampleSorter sorter = (SampleSorter) _viewer.getSorter();
			memento.putInteger(TAG_SORT_COLUMN, sorter.getColumnNumber());
			memento.putInteger(TAG_SORT_REVERSED, (sorter.isReversed() ? 1 : 0));
			
			// Save column dimensions
			Table table = _viewer.getTable();
			TableColumn columns[] = table.getColumns();
			
			// Stolen from org.eclipse.ui.TaskViewer
			boolean shouldSave = false;
			for (int i = 0; i < columns.length; ++i)
			{
				if (_columnLayouts[i].resizable && columns[i].getWidth() != 0)
				{
					shouldSave = true;
					break;
				}
			}
			if (shouldSave)
			{
				for (int i = 0; i < columns.length; ++i)
				{
					if (_columnLayouts[i].resizable)
					{
						IMemento child = memento.createChild(TAG_COLUMN);
						child.putInteger(TAG_NUMBER, i);
						child.putInteger(TAG_WIDTH, columns[i].getWidth());
					}
				}
			}
		}
	}
	
	public void packColumn(int columnIndex) {
		_viewer.getTable().getColumn(columnIndex).pack();
	}
	
	// Opens the selected file editor, at the appropriate line number.
	private void _showSelectionInEditor()
	{
		TableItem[] items = _viewer.getTable().getSelection();
		
		if (items.length > 0)
		{
			// Show for first item
			IProfileElement element = (IProfileElement) items[0].getData();
			String filename = element.getFileName();
			int line = element.getLineNumber();
			if (filename.length() > 0 && line > 0)
			{
				IEditorPart editor = null;
				IPath path = new Path(filename);
				IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

				if (ifile == null) {
					//file not in workspace
					editor = _openEditor(path.toOSString());
				} else {
					//file is a resource in the workspace
					editor = _openEditor(ifile);
				}
				
				if (editor != null)
					_gotoLine (editor, line);

			} else if (element instanceof SystemProfileShLib || element instanceof SystemProfileExecutable || element instanceof SystemProfileSymbol) {
				//if a user dbl-clicks on a row in the sample view for a shlib/executable/symbol,
				// change the selection in the system profile view to it (which will update the 
				// sample view with its new children)
				String name = element.getFileName();
				if (name.length() > 0) {
					_setNewSelection(element);	
				}
			}
		}
	}

	 // Shows the given line number in the editor.
	private void _gotoLine(IEditorPart part, int line)
	{
		ITextEditor editor = (part instanceof ITextEditor ? (ITextEditor) part : null);
		if (editor != null)
		{
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null)
			{
				IDocument doc = provider.getDocument(editor.getEditorInput());
				if (doc != null)
				{
					try
					{
						int start = doc.getLineOffset(line - 1);
						editor.selectAndReveal(start, 0);
						
						IWorkbenchPage page = editor.getSite().getPage();
						page.activate(editor);
					}
					catch (BadLocationException x) { /* ignore */ }
				}
			}
		}
	}
	
	 // Opens an editor for the given file resource.
	private IEditorPart _openEditor(IFile file)
	{
		IEditorPart part = null;
		IWorkbenchWindow win= OprofileUiPlugin.getActiveWorkbenchWindow();

		if (win != null)
		{
			IWorkbenchPage page = win.getActivePage();
			if (page != null)
			{
				try {
					part = IDE.openEditor (page, file);
				} catch (PartInitException pie) {
					System.out.println("could not open file " + file.getName());
				}
			}
		}
		
		return part;
	}

	// Opens an editor in the currently active workbench page
	// for a file not in the workspace
	private IEditorPart _openEditor(String path)
	{
		IEditorPart part = null;
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
		IWorkbenchWindow win = OprofileUiPlugin.getActiveWorkbenchWindow();
		
		if (win != null)
		{
			IWorkbenchPage page = win.getActivePage();
			if (page != null)
			{
				try {
					part = IDE.openEditorOnFileStore(page, fileStore);
				} catch (PartInitException pie) {
					System.out.println("could not open file " + path);
				}
			}
		}
		
		return part;
	}
	
	private void _setNewSelection(IProfileElement element)
	{
		OprofileUiPlugin.getDefault().getSystemProfileView().changeSelection(element);
	}
}
