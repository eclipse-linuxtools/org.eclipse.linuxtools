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
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractTreeViewer;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.SnapshotType;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditorInput;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartPNG;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.HeapChart;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
	public static final String PID_ACTION = MassifPlugin.PLUGIN_ID
	+ ".pidAction"; //$NON-NLS-1$
	public static final String SAVE_CHART_ACTION = MassifPlugin.PLUGIN_ID
	+ ".saveChartAction"; //$NON-NLS-1$

	protected MassifOutput output;
	protected Integer pid;

	protected Composite top;
	protected StackLayout stackLayout;
	protected TableViewer viewer;
	protected MassifTreeViewer treeViewer;
	protected MassifHeapTreeNode[] nodes;
	protected String chartName;

	protected static final int COLUMN_SIZE = 125;

	protected Action treeAction;
	protected Action chartAction;
	protected MassifPidMenuAction pidAction;
	protected Action saveChartAction;

	protected List<ChartEditorInput> chartInputs;

	@Override
	public void createPartControl(Composite parent) {
		chartInputs = new ArrayList<ChartEditorInput>();

		top = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		top.setLayout(stackLayout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TableViewer(top, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
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
		treeViewer.getViewer().getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				MassifSnapshot snapshot = (MassifSnapshot) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				if (snapshot.getType() != SnapshotType.EMPTY) {
					treeAction.setChecked(true);
					setTopControl(treeViewer.getViewer().getControl());
					treeViewer.getViewer().setSelection(new StructuredSelection(snapshot
							.getRoot()), true);
					treeViewer.getViewer().expandToLevel(snapshot.getRoot(),
							AbstractTreeViewer.ALL_LEVELS);
				}
			}
		});

		stackLayout.topControl = viewer.getControl();
		top.layout();
	}

	private String getUnitString(MassifSnapshot[] snapshots) {
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
					table.setSortDirection(SWT.UP);
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

						// ascending / descending
						result = direction == SWT.UP ? result : -result;

						// overflow check
						if (result > Integer.MAX_VALUE) {
							result = Integer.MAX_VALUE;
						} else if (result < Integer.MIN_VALUE) {
							result = Integer.MIN_VALUE;
						}
						return (int) result;
					}
				});
			}
		};
	}

	public IAction[] getToolbarActions() {
		pidAction = new MassifPidMenuAction(this);
		pidAction.setId(PID_ACTION);

		chartAction = new Action(
				Messages.getString("MassifViewPart.Display_Heap_Allocation"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				ChartEditorInput input = getChartInput(pid);
				if (input != null) {					
					displayChart(input);
				}
			}
		};
		chartAction.setId(CHART_ACTION);
		chartAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				MassifPlugin.PLUGIN_ID, "icons/linecharticon.gif")); //$NON-NLS-1$
		chartAction.setToolTipText(Messages
				.getString("MassifViewPart.Display_Heap_Allocation")); //$NON-NLS-1$

		saveChartAction = new Action(Messages.getString("MassifViewPart.Save_Chart"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				ChartEditorInput currentInput = getChartInput(pid);
				String path = getChartSavePath(currentInput.getName() + ".png"); //$NON-NLS-1$
				if (path != null) {
					ChartPNG renderer = new ChartPNG(currentInput.getChart());
					renderer.renderPNG(Path.fromOSString(path));
				}
			}
		};
		saveChartAction.setId(SAVE_CHART_ACTION);
		saveChartAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				MassifPlugin.PLUGIN_ID, "icons/chart-save.png")); //$NON-NLS-1$
		saveChartAction.setToolTipText(Messages.getString("MassifViewPart.Save_Chart")); //$NON-NLS-1$
		
		treeAction = new Action(
				Messages.getString("MassifViewPart.Show_Heap_Tree"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				if (isChecked()) {
					stackLayout.topControl = treeViewer.getViewer().getControl();
					top.layout();
				} else {
					stackLayout.topControl = viewer.getControl();
					top.layout();
				}
			}
		};
		treeAction.setId(TREE_ACTION);
		treeAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				MassifPlugin.PLUGIN_ID, "icons/call_hierarchy.gif")); //$NON-NLS-1$
		treeAction.setToolTipText(Messages
				.getString("MassifViewPart.Show_Heap_Tree")); //$NON-NLS-1$

		return new IAction[] { pidAction, chartAction, saveChartAction, treeAction };
	}
	
	protected String getChartSavePath(String defaultName) {
		Shell parent = new Shell(Display.getDefault());
		FileDialog dialog = new FileDialog(parent, SWT.SAVE);
		dialog.setText(Messages.getString("MassifViewPart.Save_chart_dialog_title")); //$NON-NLS-1$
		dialog.setOverwrite(true);
		dialog.setFilterExtensions(new String[] { ".png" }); //$NON-NLS-1$
		dialog.setFileName(defaultName);

		return dialog.open();
	}

	protected void createChart(MassifSnapshot[] snapshots) {
		String title = chartName + " [PID: " + pid + "]";  //$NON-NLS-1$//$NON-NLS-2$
		HeapChart chart = new HeapChart(snapshots, title);

		String name = getInputName(title);
		ChartEditorInput input = new ChartEditorInput(chart, this, name, pid);
		chartInputs.add(input);

		// open the editor
		displayChart(input);
	}

	protected void displayChart(final ChartEditorInput chartInput) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
					page.openEditor(chartInput, MassifPlugin.EDITOR_ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected String getInputName(String description) {
		String launchName;
		try {
			launchName = description.substring(0, description
					.indexOf("[massif")); //$NON-NLS-1$
		}
		catch(IndexOutOfBoundsException e) {
			launchName = "(No chart title)";
		}
		return launchName.trim();
	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public void refreshView() {
		if (output != null && pid != null) {
			MassifSnapshot[] snapshots = output.getSnapshots(pid);
			pidAction.setPids(output.getPids());
			if (snapshots != null) {
				viewer.setInput(snapshots);

				String timeWithUnit = TITLE_TIME + " (" + getUnitString(snapshots) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				for (TableColumn column : viewer.getTable().getColumns()) {
					if (column.getText().startsWith(TITLE_TIME)) {
						column.setText(timeWithUnit);
					}
					viewer.getTable().layout(true);
				}
				MassifSnapshot[] detailed = getDetailed(snapshots);
				nodes = new MassifHeapTreeNode[detailed.length];
				for (int i = 0; i < detailed.length; i++) {
					nodes[i] = detailed[i].getRoot();
				}
				treeViewer.getViewer().setInput(nodes);

				// create and display chart
				if (snapshots.length > 0) {
					ChartEditorInput input = getChartInput(pid);
					if (input == null) {
						createChart(snapshots);
					}
					else {
						displayChart(input);
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		// Close all chart editors to keep Valgrind output consistent throughout workbench
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			for (IEditorInput input : chartInputs) {
				IEditorPart part = page.findEditor(input);
				if (part != null) {
					page.closeEditor(part, false);
				}
			}
		}
		super.dispose();
	}

	public void setTopControl(Control control) {
		stackLayout.topControl = control;
		top.layout(true);
	}

	public void setOutput(MassifOutput output) {
		this.output = output;
	}

	public MassifOutput getOutput() {
		return output;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public Integer getPid() {
		return pid;
	}

	public MassifSnapshot[] getSnapshots() {
		return output != null && pid != null ? output.getSnapshots(pid) : null;
	}

	public TableViewer getTableViewer() {
		return viewer;
	}

	public MassifTreeViewer getTreeViewer() {
		return treeViewer;
	}

	protected static class MassifLabelProvider extends LabelProvider implements
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
					image = AbstractUIPlugin
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
	
	public void setChartName(String chartName) {
		this.chartName = chartName;
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

	private ChartEditorInput getChartInput(Integer pid) {
		ChartEditorInput result = null;
		for (int i = 0; i < chartInputs.size(); i++) {
			ChartEditorInput input = chartInputs.get(i);
			if (input.getPid().equals(pid)) {
				result = input;
			}
		}
		return result;
	}
}
