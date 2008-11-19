/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

public class MassifViewPart extends ViewPart implements IValgrindToolView {

	protected MassifSnapshot[] snapshots;
	
	protected Composite top;
	protected StackLayout stackLayout;
	protected TableViewer viewer;
	protected MassifTreeViewer treeViewer;
	protected MassifHeapTreeNode[] nodes;

	protected static final int COLUMN_SIZE = 125;

	protected Action treeAction;

	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		top.setLayout(stackLayout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer = new TableViewer(top, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);

		Table table = viewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnTitles = { Messages.getString("MassifViewPart.Snapshot"), Messages.getString("MassifViewPart.Time"), Messages.getString("MassifViewPart.Total"), Messages.getString("MassifViewPart.Useful_Heap"), Messages.getString("MassifViewPart.Extra_Heap"), Messages.getString("MassifViewPart.Stacks") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		for (int i = 0; i < columnTitles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(columnTitles[i]);
			column.getColumn().setWidth(COLUMN_SIZE);
			column.getColumn().setResizable(true);
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());	
		viewer.setLabelProvider(new MassifLabelProvider());
		
		treeViewer = new MassifTreeViewer(top);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				MassifSnapshot snapshot = (MassifSnapshot) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (snapshot.getType() != SnapshotType.EMPTY) {
					treeAction.setChecked(true);
					setTopControl(treeViewer.getControl());
					treeViewer.setSelection(new StructuredSelection(snapshot.getRoot()), true);
					treeViewer.expandToLevel(snapshot.getRoot(), TreeViewer.ALL_LEVELS);
				}
			}	
		});
		
		createToolbar();
		
		setTopControl(viewer.getControl());
	}

	protected void createToolbar() {
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		treeAction = new Action(Messages.getString("MassifViewPart.Show_Heap_Tree"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				if (isChecked()) {
					setTopControl(treeViewer.getControl());
				}
				else {
					setTopControl(viewer.getControl());
				}				
			}
		};
		treeAction.setImageDescriptor(MassifPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/call_hierarchy.gif")); //$NON-NLS-1$
		treeAction.setToolTipText(Messages.getString("MassifViewPart.Show_Heap_Tree")); //$NON-NLS-1$
		manager.add(treeAction);
		view.getViewSite().getActionBars().updateActionBars();
	}
	
	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public void refreshView() {
		if (snapshots != null) {
			viewer.setInput(snapshots);
			MassifSnapshot[] detailed = getDetailed(snapshots);
			nodes = new MassifHeapTreeNode[detailed.length];
			for (int i = 0; i < detailed.length; i++) {
				nodes[i] = detailed[i].getRoot();
			}
			treeViewer.setInput(nodes);
		}
	}

	protected void setTopControl(Control control) {
		stackLayout.topControl = control;
		top.layout(true);
	}
	
	public void setSnapshots(MassifSnapshot[] snapshots) {
		this.snapshots = snapshots;
	}

	protected class MassifLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if (columnIndex == 0) {
				MassifSnapshot snapshot = (MassifSnapshot) element;
				switch (snapshot.getType()) {
				case EMPTY:
					break;
				case PEAK:
				case DETAILED:
					image = MassifPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/call_hierarchy.gif").createImage(); //$NON-NLS-1$
				}
			}
			return image;
		}

		public String getColumnText(Object element, int columnIndex) {
			MassifSnapshot snapshot = (MassifSnapshot) element;
			switch (columnIndex) {
			case 0:
				return String.valueOf(snapshot.getNumber());
			case 1:
				return String.valueOf(snapshot.getTime());
			case 2:
				return String.valueOf(snapshot.getTotal());
			case 3:
				return String.valueOf(snapshot.getHeapBytes());
			case 4:
				return String.valueOf(snapshot.getHeapExtra());
			default:
				return String.valueOf(snapshot.getStacks());
			}
		}

		public Font getFont(Object element) {
			Font font = null;
			MassifSnapshot snapshot = (MassifSnapshot) element;
			switch (snapshot.getType()) {
			case EMPTY:
			case DETAILED:
				break;
			case PEAK:
				font = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
			return font;
		}		
	}
	
	private MassifSnapshot[] getDetailed(MassifSnapshot[] snapshots) {
		ArrayList<MassifSnapshot> list = new ArrayList<MassifSnapshot>();
		for (MassifSnapshot snapshot : snapshots) {
			if (snapshot.getType() != SnapshotType.EMPTY) {
				list.add(snapshot);
			}
		}
		return list.toArray(new MassifSnapshot[list.size()]);
	}
	
}
