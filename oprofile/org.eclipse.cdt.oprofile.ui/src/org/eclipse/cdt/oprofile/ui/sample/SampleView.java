/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.sample;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.oprofile.ui.OprofilePlugin;
import org.eclipse.cdt.oprofile.ui.OprofileUIMessages;
import org.eclipse.cdt.oprofile.ui.internal.IProfileElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
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
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author keiths
 */
public class SampleView extends ViewPart
{
	private TableTreeViewer _viewer;
	private IMemento _memento = null;

	private static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
	
	// Persistence tags
	private static final String TAG_COLUMN = "column"; //$NON-NLS-1$
	private static final String TAG_NUMBER = "number"; //$NON-NLS-1$
	private static final String TAG_WIDTH = "width"; //$NON-NLS-1$
	private static final String TAG_SORT_COLUMN = "sorterColumn"; //$NON-NLS-1$
	private static final String TAG_SORT_REVERSED = "sorterReversed"; //$NON-NLS-1$

	public static final int COLUMN_VMA = 0;
	public static final int COLUMN_SAMPLES = 1;
	public static final int COLUMN_PERCENT = 2;
	public static final int COLUMN_LINE = 3;
	public static final int COLUMN_NAME = 4;

	private static String[] _columnHeaders =
	{
		OprofileUIMessages.getString("sampleview.table.column.vma.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.count.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.percentage.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.line.text"), //$NON-NLS-1$
		OprofileUIMessages.getString("sampleview.table.column.name.text") //$NON-NLS-1$
	};
	
	private static ColumnLayoutData[] _columnLayouts =
	{
		// FIXME SUCK: There must be a better way to get the length of
		// strings as they would be displayed on the screen... For now
		// just set something sane. The user can resize the columns.
		new ColumnPixelData(75, true),
		new ColumnPixelData(75, true),
		new ColumnPixelData(50, true),
		new ColumnPixelData(50, true),
		new ColumnWeightData(1, false)
	};
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent)
	{
		TableTree tableTree = new TableTree(parent, SWT.H_SCROLL | SWT.FULL_SELECTION);
		Table table = tableTree.getTable();
		table.setHeaderVisible(true);
		
		_viewer = new TableTreeViewer(tableTree);
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
				sorter = new SampleSorter(this, sortColumn.intValue());
				sorter.setReversed(reversed);
			}
		}

		if (sorter == null)
		{
			// By default, sort by reverse sample count
			sorter = new SampleSorter(this, COLUMN_SAMPLES);
			sorter.setReversed(true);
		}
		_viewer.setSorter(sorter);
		
		// Clear the memento
		_memento = null;

		// Register ourselves as the sample viewer for the profile views
		OprofilePlugin.getDefault().setSampleView(this);
	}
	
	// Shows the selection in a suitable editor.
	private void _showSelectionInEditor()
	{
		TableTreeItem[] items = _viewer.getTableTree().getSelection();
		if (items.length > 0)
		{
			// Show for first item
			IProfileElement element = (IProfileElement) items[0].getData();
			String filename = element.getFileName();
			int line = element.getLineNumber();
			if (filename.length() > 0 && line > 0)
			{
				// Show in editor -- see if it is already displayed
				// NOTE: This doesn't seem to be necessary for files in projects.
				// But since it is needed for random, non-project files, we'll do it anyway.
				IEditorPart editor = _editorForFilename(filename);
				if (editor == null)
				{
					// File not in an editor
					IPath path = new Path(filename);
					IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
					if (ifile == null)
					{
						IStorage storage = new FileStorage(path);
						editor = _openEditor(storage, path.lastSegment());
					}
					else
						editor = _openEditor(ifile);
				}
				
				// Go to line
				if (editor != null)
					_gotoLine (editor, line);
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
					catch (BadLocationException x)
					{
						// ignore
					}
				}
			}
		}
	}
	
	 // Opens an editor for the given file resource.
	private IEditorPart _openEditor(IFile file)
	{
		IWorkbenchWindow win= OprofilePlugin.getActiveWorkbenchWindow();
		if (win != null)
		{
			IWorkbenchPage page = win.getActivePage();
			if (page != null)
			{
				try
				{
					IEditorPart part = IDE.openEditor (page, file);
					return part;
				}
				catch (PartInitException pie)
				{
					System.out.println("could not open file " + file.getName());
				}
			}
		}
		
		return null;
	}


	// Opens an editor in the currently active workbench page
	private IEditorPart _openEditor(IStorage storage, String name)
	{
		IWorkbenchWindow win= OprofilePlugin.getActiveWorkbenchWindow();
		if (win != null)
		{
			// Check if the active editor already contains the given file
			IWorkbenchPage page = win.getActivePage();
			if (page != null)
			{
				IEditorPart part = null;
				IEditorInput ei = new ExternalEditorInput(storage);
				
				try
				{
					part = page.openEditor(ei, _getEditorID(name));
				}
				catch (PartInitException pie)
				{
					System.out.println("could not open file " + storage.getName());
				}
				
				return part;
			}
		}
		
		return null;
	}

	// Check for an editor showing the given filename.
	private IEditorPart _editorForFilename(String filename)
	{
		IWorkbenchWindow win = OprofilePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (win != null)
		{
			IWorkbenchPage page  = win.getActivePage();
			if (page != null)
			{
				IEditorReference[] refs = page.getEditorReferences();
				for (int i = 0; i < refs.length; ++i)
				{
					IEditorPart editor = refs[i].getEditor(false);
					if (editor != null)
					{
						IEditorInput ei = editor.getEditorInput();
						if (ei instanceof ExternalEditorInput)
						{
							ExternalEditorInput input = (ExternalEditorInput) ei;
							if (input.getFullPath().equals(filename))
								return editor;
						}
						else if (ei instanceof FileEditorInput)
						{
							// NOTE: This doesn't seem to be necessary. When a file
							// is part of a project, the editor will automagically reuse the
							// editor.
							/*
							FileEditorInput input = (FileEditorInput) ei;
							if (input.getFile().getFullPath().toOSString().equals(filename))
								return editor;
							*/
						}
						else
						{
							// What to do in this case?
							System.out.println("editor input = " + ei);
						}
					}
				}
			}
		}
			
		return null;
	}
	
	// Returns the ID of the editor which should be used to open a file
	private static String _getEditorID(String name)
	{
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null)
		{
			IEditorDescriptor desc = registry.getDefaultEditor(name);
			if (desc != null)
				return desc.getId();
			else
				return registry.findEditor(DEFAULT_TEXT_EDITOR_ID).getId();
		}

		return null;
	}
	
	public void dispose()
	{
		// Unregister ourselves from the profile views
		OprofilePlugin.getDefault().setSampleView(null);
	}
		
	protected void _createColumns(Table table)
	{
		SelectionListener headerListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				int column = _viewer.getTableTree().getTable().indexOf((TableColumn) se.widget);
				SampleSorter oldSorter = (SampleSorter) _viewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber())
				{
					oldSorter.setReversed(!oldSorter.isReversed());
					_viewer.refresh();
				}
				else
					_viewer.setSorter(new SampleSorter(SampleView.this, column));
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

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		_viewer.getTableTree().getTable().setFocus();
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
			Table table = _viewer.getTableTree().getTable();
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
}
