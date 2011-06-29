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

import java.util.Map;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

public abstract class RemoteTab extends AbstractLaunchConfigurationTab {
	
	private TableViewer tableViewer;
	private boolean isInitializing;
	private Map<String, IPeer> peers;
	private String name;

	private class RemoteLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = null;
			IPeer peer = (IPeer) element;
			switch (columnIndex) {
			case 0:
				text = peer.getID();
				break;
			case 1:
				text = peer.getName();
				break;
			case 2:
				text = peer.getOSName();
				break;
			case 3:
				text = peer.getTransportName();
				break;
			}
			return text;
		}
		
	}
	
	private class TCFPeerListener implements ILocator.LocatorListener {

		public void peerAdded(IPeer peer) {
			peers.put(peer.getID(), peer);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshPeerViewer();
				}
			});
		}

		public void peerChanged(IPeer peer) {
			peers.put(peer.getID(), peer);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshPeerViewer();
				}
			});
		}

		public void peerRemoved(String id) {
			peers.remove(id);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refreshPeerViewer();
				}
			});
		}

		public void peerHeartBeat(String id) {
		}
		
	}

	public RemoteTab(String name) {
		this.name = name;
		// Query Locator service for available peers
		ILocator locator = Protocol.getLocator();
		peers = locator.getPeers();
		// Register for updates
		locator.addListener(new TCFPeerListener());
	}
	
	protected void localCreateControl(Composite top) {}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Create Peers table
		Label peersLabel = new Label(top, SWT.NONE);
		peersLabel.setText(RemoteMessages.RemoteTab_label_peers);
		
		tableViewer = new TableViewer(top, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		String[] titles = { RemoteMessages.RemoteTab_header_ID, RemoteMessages.RemoteTab_header_name, RemoteMessages.RemoteTab_header_OS, RemoteMessages.RemoteTab_header_transport };
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
		tableViewer.setLabelProvider(new RemoteLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		
		tableViewer.setInput(peers.values().toArray(new IPeer[peers.size()]));
		
		localCreateControl(top);
		
		setControl(top);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, RemoteLaunchConstants.DEFAULT_REMOTE_PEERID);
	}

	public void localInitializeFrom(ILaunchConfiguration configuration) throws CoreException {}

	public void initializeFrom(ILaunchConfiguration configuration) {
		isInitializing = true;
		try {
			String peerID = configuration.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, RemoteLaunchConstants.DEFAULT_REMOTE_PEERID);
			if (peerID != null) {
				IPeer[] peers = (IPeer[]) tableViewer.getInput();
				
				// Search for corresponding peer and select in table
				for (int i = 0; i < peers.length; i++) {
					if (peers[i].getID().equals(peerID)) {
						tableViewer.setSelection(new StructuredSelection(peers[i]));
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
			configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, (String) null);
		}
		else if (selected instanceof IStructuredSelection) {
			IPeer peer = (IPeer) ((IStructuredSelection) selected).getFirstElement();
			configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, peer.getID());
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
		return ProfileRemoteLaunchPlugin.imageDescriptorFromPlugin(ProfileRemoteLaunchPlugin.PLUGIN_ID, "icons/tcf.gif").createImage();
	}
	
	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}		
	}
	
	private void refreshPeerViewer() {
		if (tableViewer != null && tableViewer.getContentProvider() != null) {
			tableViewer.setInput(peers.values().toArray(new IPeer[peers.size()]));
			tableViewer.refresh();
		}
	}
}
