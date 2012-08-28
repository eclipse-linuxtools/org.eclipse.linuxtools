/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - modify code to be shared among tools
 *******************************************************************************/ 
package org.eclipse.linuxtools.profiling.launch.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.internal.profiling.launch.remote.ProfileRemoteLaunchPlugin;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteLaunchConstants;
import org.eclipse.linuxtools.internal.profiling.launch.remote.RemoteMessages;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

public abstract class RemoteTab extends AbstractLaunchConfigurationTab {
	
	private TableViewer tableViewer;
	private boolean isInitializing;
	private IHost[] hosts;
	private String name;

	private static class RemoteSystemLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = null;
			IHost host = (IHost) element;
			switch (columnIndex) {
			case 0:
				text = host.getName();
				break;
			case 1:
				text = host.getHostName();
				break;
			case 2:
				text = host.getSystemType().getLabel();
				break;
			}
			return text;
		}
		
	}

	private class RemoteModelListener implements ISystemModelChangeListener {

		public void systemModelResourceChanged(ISystemModelChangeEvent arg0) {
			ISystemRegistry registry = SystemStartHere.getSystemRegistry();
			hosts = registry.getHosts();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshViewer();
				}
			});
		}
		
	}

	public RemoteTab(String name) {
		this.name = name;
		// Query Locator service for available peers
		ISystemRegistry registry = SystemStartHere.getSystemRegistry();
		hosts = registry.getHosts();
		registry.addSystemModelChangeListener(new RemoteModelListener());
	}
	
	protected void localCreateControl(Composite top) {}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Create Peers table
		Label peersLabel = new Label(top, SWT.NONE);
		peersLabel.setText(RemoteMessages.RemoteTab_label_hosts);
		
		tableViewer = new TableViewer(top, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		String[] titles = { RemoteMessages.RemoteTab_header_name, RemoteMessages.RemoteTab_header_hostname, RemoteMessages.RemoteTab_header_type };
		int[] bounds = { 200, 100, 250, 100 };

		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new RemoteSystemLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		
		while (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_ALL)) {
			try {
				RSECorePlugin.waitForInitCompletion();
			} catch (InterruptedException e2) {
				// do nothing
			}
		}

		ISystemRegistry registry = SystemStartHere.getSystemRegistry();
		hosts = registry.getHosts();
		tableViewer.setInput(hosts);
		
		localCreateControl(top);
		
		setControl(top);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_HOSTID, RemoteLaunchConstants.DEFAULT_REMOTE_HOSTID);
	}

	public void localInitializeFrom(ILaunchConfiguration configuration) throws CoreException {}

	public void initializeFrom(ILaunchConfiguration configuration) {
		isInitializing = true;
		try {
			String hostID = configuration.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_HOSTID, RemoteLaunchConstants.DEFAULT_REMOTE_HOSTID);
			if (hostID != null) {
				IHost[] hosts = (IHost[]) tableViewer.getInput();
				
				// Search for corresponding peer and select in table
				for (int i = 0; i < hosts.length; i++) {
					if (hosts[i].getName().equals(hostID)) {
						tableViewer.setSelection(new StructuredSelection(hosts[i]));
					}
				}
			}
			localInitializeFrom(configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		isInitializing = false;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ISelection selected = tableViewer.getSelection();
		if (selected == null) {
			configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_HOSTID, (String) null);
		}
		else if (selected instanceof IStructuredSelection) {
			IHost host = (IHost) ((IStructuredSelection) selected).getFirstElement();
			if (host != null)
				configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_HOSTID, host.getName());
		}
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		boolean valid = false;
		
		ISelection selected = tableViewer.getSelection();
		valid = selected != null && selected instanceof IStructuredSelection
				&& !((IStructuredSelection) selected).isEmpty();
		return valid;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		return ProfileRemoteLaunchPlugin.imageDescriptorFromPlugin(ProfileRemoteLaunchPlugin.PLUGIN_ID, "icons/system_view.gif").createImage();
	}
	
	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}		
	}
	
	private void refreshViewer() {
		if (tableViewer != null && tableViewer.getContentProvider() != null) {
			tableViewer.setInput(hosts);
			tableViewer.refresh();
		}
	}
}

