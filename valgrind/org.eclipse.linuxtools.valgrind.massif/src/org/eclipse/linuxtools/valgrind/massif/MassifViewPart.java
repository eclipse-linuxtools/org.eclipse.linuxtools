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

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.valgrind.massif.birt.ChartEditorInput;
import org.eclipse.linuxtools.valgrind.massif.birt.HeapChart;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

public class MassifViewPart extends ViewPart implements IValgrindToolView {

	protected static final String TITLE_STACKS = Messages
			.getString("MassifViewPart.Stacks"); //$NON-NLS-1$
	protected static final String TITLE_EXTRA = Messages
			.getString("MassifViewPart.Extra_Heap"); //$NON-NLS-1$
	protected static final String TITLE_USEFUL = Messages
			.getString("MassifViewPart.Useful_Heap"); //$NON-NLS-1$
	protected static final String TITLE_TOTAL = Messages
			.getString("MassifViewPart.Total"); //$NON-NLS-1$
	protected static final String TITLE_TIME = Messages
			.getString("MassifViewPart.Time"); //$NON-NLS-1$
	protected static final String TITLE_NUMBER = Messages
			.getString("MassifViewPart.Snapshot"); //$NON-NLS-1$
	protected static final String TREE_ACTION = MassifPlugin.PLUGIN_ID
			+ ".treeAction"; //$NON-NLS-1$
	public static final String CHART_ACTION = MassifPlugin.PLUGIN_ID
			+ ".chartAction"; //$NON-NLS-1$

	protected MassifSnapshot[] snapshots;

	protected Composite top;
	protected StackLayout stackLayout;
	protected TableViewer viewer;
	protected MassifTreeViewer treeViewer;
	protected MassifHeapTreeNode[] nodes;

	protected static final int COLUMN_SIZE = 125;

	protected Action treeAction;
	protected Action chartAction;

	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		top.setLayout(stackLayout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TableViewer(top, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);

		Table table = viewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));


		String[] columnTitles = { TITLE_NUMBER, TITLE_TIME, TITLE_TOTAL,
				TITLE_USEFUL, TITLE_EXTRA, TITLE_STACKS };

		for (int i = 0; i < columnTitles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(columnTitles[i]);
			column.getColumn().setWidth(COLUMN_SIZE);
			column.getColumn().setResizable(true);
			column.getColumn().addSelectionListener(getHeaderListener());
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new MassifLabelProvider());

		treeViewer = new MassifTreeViewer(top);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				MassifSnapshot snapshot = (MassifSnapshot) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				if (snapshot.getType() != SnapshotType.EMPTY) {
					treeAction.setChecked(true);
					setTopControl(treeViewer.getControl());
					treeViewer.setSelection(new StructuredSelection(snapshot
							.getRoot()), true);
					treeViewer.expandToLevel(snapshot.getRoot(),
							TreeViewer.ALL_LEVELS);
				}
			}
		});

		setTopControl(viewer.getControl());
	}

	private String getUnitString(MassifSnapshot[] snapshots2) {
		String result;
		MassifSnapshot snapshot = snapshots[0];
		switch (snapshot.getUnit()) {
		case BYTES:
			result = "B"; //$NON-NLS-1$
			break;
		case INSTRUCTIONS:
			result = "i"; //$NON-NLS-1$
			break;
		default:
			result = "ms"; //$NON-NLS-1$
			break;
		}
		return result;
	}

	private SelectionListener getHeaderListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableColumn column = (TableColumn) e.widget;
				Table table = viewer.getTable();
				if (column.equals(table.getSortColumn())) {
					int direction = table.getSortDirection() == SWT.UP ? SWT.DOWN
							: SWT.UP;
					table.setSortDirection(direction);
				} else {
					table.setSortDirection(SWT.DOWN);
				}
				table.setSortColumn(column);
				viewer.setComparator(new ViewerComparator() {
					@Override
					public int compare(Viewer viewer, Object o1, Object o2) {
						Table table = ((TableViewer) viewer).getTable();
						int direction = table.getSortDirection();
						MassifSnapshot s1 = (MassifSnapshot) o1;
						MassifSnapshot s2 = (MassifSnapshot) o2;
						long result;
						TableColumn column = table.getSortColumn();
						if (column.getText().equals(TITLE_NUMBER)) {
							result = s1.getNumber() - s2.getNumber();
						} else if (column.getText().startsWith(TITLE_TIME)) {
							result = s1.getTime() - s2.getTime();
						} else if (column.getText().equals(TITLE_TOTAL)) {
							result = s1.getTotal() - s2.getTotal();
						} else if (column.getText().equals(TITLE_USEFUL)) {
							result = s1.getHeapBytes() - s2.getHeapBytes();
						} else if (column.getText().equals(TITLE_EXTRA)) {
							result = s1.getHeapExtra() - s2.getHeapExtra();
						} else {
							result = s1.getStacks() - s2.getStacks();
						}
						
						// overflow check
						if (result > Integer.MAX_VALUE) {
							result = Integer.MAX_VALUE;
						} else if (result < Integer.MIN_VALUE) {
							result = Integer.MIN_VALUE;
						}
						return (int) (direction == SWT.DOWN ? result : -result);
					}
				});
			}
		};
	}

	public IAction[] getToolbarActions() {
		chartAction = new Action(
				Messages.getString("MassifViewPart.Display_Heap_Allocation"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				if (snapshots.length > 0) {
					IProgressService ps = PlatformUI.getWorkbench()
							.getProgressService();
					final IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					try {
						ps.busyCursorWhile(new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException,
									InterruptedException {
								monitor.beginTask(Messages.getString("MassifViewPart.Rendering_Chart"), 2); //$NON-NLS-1$
								displayChart(page, monitor); // this can be long running
								monitor.done();
							}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		chartAction.setId(CHART_ACTION);
		chartAction.setImageDescriptor(MassifPlugin.imageDescriptorFromPlugin(
				MassifPlugin.PLUGIN_ID, "icons/linecharticon.gif")); //$NON-NLS-1$
		chartAction.setToolTipText(Messages
				.getString("MassifViewPart.Display_Heap_Allocation")); //$NON-NLS-1$

		treeAction = new Action(
				Messages.getString("MassifViewPart.Show_Heap_Tree"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				if (isChecked()) {
					setTopControl(treeViewer.getControl());
				} else {
					setTopControl(viewer.getControl());
				}
			}
		};
		treeAction.setId(TREE_ACTION);
		treeAction.setImageDescriptor(MassifPlugin.imageDescriptorFromPlugin(
				MassifPlugin.PLUGIN_ID, "icons/call_hierarchy.gif")); //$NON-NLS-1$
		treeAction.setToolTipText(Messages
				.getString("MassifViewPart.Show_Heap_Tree")); //$NON-NLS-1$

		return new IAction[] { chartAction, treeAction };
	}

	protected void displayChart(final IWorkbenchPage page,
			final IProgressMonitor monitor) {
		final HeapChart chart = new HeapChart(snapshots);
		final String title = ValgrindUIPlugin.getDefault().getView()
				.getContentDescription();
		chart.getTitle().getLabel().getCaption().setValue(title);

		if (page != null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						String name = getInputName(title);
						final ChartEditorInput input = new ChartEditorInput(
								chart, name);
						monitor.worked(1);
						page.openEditor(input, MassifPlugin.EDITOR_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
			monitor.worked(1);
		}
	}

	protected String getInputName(String description) {
		String launchName = description.substring(0, description
				.indexOf("[massif]")); //$NON-NLS-1$
		return launchName.trim();
	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public void refreshView() {
		if (snapshots != null) {
			viewer.setInput(snapshots);
			
			String timeWithUnit = TITLE_TIME + " (" + getUnitString(snapshots) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			for (TableColumn column : viewer.getTable().getColumns()) {
				if (column.getText().startsWith(TITLE_TIME)) {
					column.setText(timeWithUnit);
					viewer.getTable().layout(true);
				}
			}
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

	public MassifSnapshot[] getSnapshots() {
		return snapshots;
	}

	public TableViewer getTableViewer() {
		return viewer;
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	protected class MassifLabelProvider extends LabelProvider implements
			ITableLabelProvider, IFontProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if (columnIndex == 0) {
				MassifSnapshot snapshot = (MassifSnapshot) element;
				switch (snapshot.getType()) {
				case EMPTY:
					break;
				case PEAK:
				case DETAILED:
					image = MassifPlugin
							.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID,
									"icons/call_hierarchy.gif").createImage(); //$NON-NLS-1$
				}
			}
			return image;
		}

		public String getColumnText(Object element, int columnIndex) {
			MassifSnapshot snapshot = (MassifSnapshot) element;
			DecimalFormat df = new DecimalFormat("#,##0"); //$NON-NLS-1$
			switch (columnIndex) {
			case 0:
				return df.format(snapshot.getNumber());
			case 1:
				return df.format(snapshot.getTime());
			case 2:
				return df.format(snapshot.getTotal());
			case 3:
				return df.format(snapshot.getHeapBytes());
			case 4:
				return df.format(snapshot.getHeapExtra());
			default:
				return df.format(snapshot.getStacks());
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
				font = JFaceResources.getFontRegistry().getBold(
						JFaceResources.DIALOG_FONT);
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
