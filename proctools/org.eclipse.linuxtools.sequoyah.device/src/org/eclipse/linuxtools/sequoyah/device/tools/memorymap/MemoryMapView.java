/********************************************************************************
 * Copyright (c) 2008 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.tools.memorymap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.sequoyah.device.LinuxToolsPlugin;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;
import org.eclipse.linuxtools.sequoyah.device.tools.IListener;
import org.eclipse.linuxtools.sequoyah.device.tools.INotifier;
import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.linuxtools.sequoyah.device.ui.DialogLogin;
import org.eclipse.linuxtools.sequoyah.device.ui.IToolViewPart;
import org.eclipse.linuxtools.sequoyah.device.ui.ViewActionConnect;
import org.eclipse.linuxtools.sequoyah.device.ui.ViewActionDisconnect;
import org.eclipse.linuxtools.sequoyah.device.ui.ViewActionRefresh;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Otavio Ferranti
 */
public class MemoryMapView extends ViewPart implements IToolViewPart, IListener {
	
	private class AddressSorter extends ViewerSorter {
	
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;
			try {
				int a = new Integer(((String[]) e1)[MAX_COLUMNS - 1]).intValue();
				int b = new Integer(((String[]) e2)[MAX_COLUMNS - 1]).intValue();
				if (a > b) {
					result = 1;
				} else if (a < b) {
					result = -1;
				};
			}
			catch (NumberFormatException nfe) {
				//TODO: Nothing ?
			}
			return result;
		}
	}
	final private String COL_LABEL_ADDRESS_START = Messages.MemoryMapView_Col_Label_Address_Start;
	final private String COL_LABEL_ADDRESS_END = Messages.MemoryMapView_Col_Label_Address_End;
	final private String COL_LABEL_REGION = Messages.MemoryMapView_Col_label_Region;
	
	final private int MAX_COLUMNS = 4;
	
	private ITool tool = null;
	private TableViewer viewer;

	private Action refreshAction;
	private Action disconnectAction;
	private Action connectAction;

	private IPartListener partActivationListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
		}
		
		public void partBroughtToTop(IWorkbenchPart part) {
		}
			 
		public void partClosed(IWorkbenchPart part) {
			if (MemoryMapView.this.getSite().getPart() == part) {
				ITool tool = MemoryMapView.this.getTool();
				if (null != tool) {
					tool.disconnect();
				}
			}
		}
		
		public void partDeactivated(IWorkbenchPart part) {
		}
			 
		public void partOpened(IWorkbenchPart part) {
		}
	};
	
	/**
	 * The constructor.
	 */
	public MemoryMapView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		
		viewer = new TableViewer(parent, SWT.FULL_SELECTION |
				SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new MemoryMapViewContentProvider());
		viewer.setLabelProvider(new MemoryMapVViewLabelProvider());
		viewer.setSorter(new AddressSorter());

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		
		new TableColumn(table, SWT.LEFT).setText(COL_LABEL_ADDRESS_START);
		new TableColumn(table, SWT.LEFT).setText(COL_LABEL_ADDRESS_END);
		new TableColumn(table, SWT.LEFT).setText(COL_LABEL_REGION);
		
		refresh();
		resize();
		
		makeActions();
		// hookDoubleClickAction();
		addToToolBar();
		
		getViewSite()
			.getWorkbenchWindow()
			.getPartService()
			.addPartListener(partActivationListener);
		
		setConnectEnabled(true);
		refreshAction.setEnabled(false);
	}

	private void addToToolBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarMmanager = actionBars.getToolBarManager();
		toolBarMmanager.add(refreshAction);
		toolBarMmanager.add(disconnectAction);
		toolBarMmanager.add(connectAction);
	}
	
	private void makeActions() {
		
		refreshAction = new Action() {
			public void run() {
				IViewActionDelegate delegate = new ViewActionRefresh();
		        delegate.init(MemoryMapView.this);
	            delegate.run(this);
	            this.setEnabled(false);
			}
		};
		refreshAction.setToolTipText(Messages.MemoryMapView_Action_Refresh);
		refreshAction.setImageDescriptor(
				LinuxToolsPlugin.getDefault().getImageDescriptor(LinuxToolsPlugin.ICON_REFRESH));
		
		disconnectAction = new Action() {
			public void run() {
				IViewActionDelegate delegate = new ViewActionDisconnect();
		        delegate.init(MemoryMapView.this);
	            delegate.run(this);
			}
		};
		disconnectAction.setToolTipText(Messages.MemoryMapView_Action_Disconnect);
		disconnectAction.setImageDescriptor(
				LinuxToolsPlugin.getDefault().getImageDescriptor(LinuxToolsPlugin.ICON_DISCONNECT));
		
		connectAction = new Action() {
			public void run() {
				IViewActionDelegate delegate = new ViewActionConnect();
		        delegate.init(MemoryMapView.this);
	            delegate.run(this);
			}
		};
		connectAction.setToolTipText(Messages.MemoryMapView_Action_Connect);
		connectAction.setImageDescriptor(
				LinuxToolsPlugin.getDefault().getImageDescriptor(LinuxToolsPlugin.ICON_CONNECT));
		
	}
	
	private void setConnectEnabled(boolean bool) {
		connectAction.setEnabled(bool);
		disconnectAction.setEnabled(!bool);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.ui.IToolView#getTool()
	 */
	public ITool getTool() {
		if(null == tool) {
			tool = new MemoryMapTool();
			tool.addListener(this);
		}
		return tool;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IListener#notify(org.eclipse.linuxtools.sequoyah.device.network.INotifier, org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode, java.lang.Object)
	 */
	public void notify(INotifier notifier, EventCode event, Object result) {
		if (notifier == tool) {
			final Object finalResult = result;
			final EventCode finalEvent = event;
			final ViewPart finalView = this;
			final ITool finalTool = this.tool;
			
			this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					switch(finalEvent) {
						case EVT_TOOL_REFRESH_VIEW:
							viewer.setInput(finalResult);
							refreshAction.setEnabled(true);
		        			refresh();
		        			resize();
						break;
						case EVT_TOOL_CONNECT_FINISHED:
						case EVT_TOOL_LOGIN_FINISHED:
							switch ((OperationCode)finalResult) {
								case SUCCESS: 
									setConnectEnabled(false);
									refreshAction.setEnabled(true);
								break;
								case LOGIN_REQUIRED: {
									final DialogLogin dialog = new DialogLogin(
											finalView.getViewSite().getShell(),
											finalTool, false);
									dialog.open();
								}
								break;
								case LOGIN_FAILED: {
									final DialogLogin dialog = new DialogLogin(
											finalView.getViewSite().getShell(),
											finalTool, true);
									dialog.open();
								}
								break;
							}
						break;
						case EVT_TOOL_DISCONNECT_FINISHED:
							setConnectEnabled(true);
							refreshAction.setEnabled(false);
						break;
					}
				}
			});
		}
	}
	
	/**
	 * 
	 */
	public void refresh() {
		viewer.refresh();
	}

	/**
	 * 
	 */
	public void resize() {
		Table table = viewer.getTable();
	    for (int i = 0, n = table.getColumnCount(); i < n; i++) {
	    	table.getColumn(i).pack();
	    }
	}
	
	/**
	 * @param data
	 */
	public void setData (Object data) {
		viewer.setInput(data);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}