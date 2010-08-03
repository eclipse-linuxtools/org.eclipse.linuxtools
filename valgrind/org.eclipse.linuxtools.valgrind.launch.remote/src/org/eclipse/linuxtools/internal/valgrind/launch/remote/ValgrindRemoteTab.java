/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

public class ValgrindRemoteTab extends AbstractLaunchConfigurationTab {
	
	private TableViewer tableViewer;
	private Text destDirText;
	private Text tmpDirText;
	private Text valgrindLocText;
	private boolean isInitializing;
	private Map<String, IPeer> peers;

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

	public ValgrindRemoteTab() {
		// Query Locator service for available peers
		ILocator locator = Protocol.getLocator();
		peers = locator.getPeers();
		// Register for updates
		locator.addListener(new TCFPeerListener());
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Create Peers table
		Label peersLabel = new Label(top, SWT.NONE);
		peersLabel.setText(Messages.ValgrindRemoteTab_label_peers);
		
		tableViewer = new TableViewer(top, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		String[] titles = { Messages.ValgrindRemoteTab_header_ID, Messages.ValgrindRemoteTab_header_name, Messages.ValgrindRemoteTab_header_OS, Messages.ValgrindRemoteTab_header_transport };
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
		
		Composite pathTop = new Composite(top, SWT.NONE);
		pathTop.setLayout(new GridLayout(2, false));
		pathTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Valgrind location
		Label valgrindLocLabel = new Label(pathTop, SWT.NONE);
		valgrindLocLabel.setText(Messages.ValgrindRemoteTab_label_location_VG);
		
		valgrindLocText = new Text(pathTop, SWT.BORDER);
		valgrindLocText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		valgrindLocText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		// Destination directory
		Label destDirLabel = new Label(pathTop, SWT.NONE);
		destDirLabel.setText(Messages.ValgrindRemoteTab_label_dest_wd);
		
		destDirText = new Text(pathTop, SWT.BORDER);
		destDirText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		destDirText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		Label tmpDirLabel = new Label(pathTop, SWT.NONE);
		tmpDirLabel.setText(Messages.ValgrindRemoteTab_label_tmp_dir);
		
		tmpDirText = new Text(pathTop, SWT.BORDER);
		tmpDirText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		tmpDirText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		setControl(top);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_PEERID, RemoteLaunchConstants.DEFAULT_REMOTE_PEERID);
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, RemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC);
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_DESTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR);
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR);
	}

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
			
			String valgrindLoc = configuration.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, RemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC);
			if (valgrindLoc != null) {
				valgrindLocText.setText(valgrindLoc);
			}
			
			String destDir = configuration.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_DESTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR);
			if (destDir != null) {
				destDirText.setText(destDir);
			}
			
			String tmpDir = configuration.getAttribute(RemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, RemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR);
			if (tmpDir != null) {
				tmpDirText.setText(tmpDir);
			}
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

		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, valgrindLocText.getText());
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_DESTDIR, destDirText.getText());
		configuration.setAttribute(RemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, tmpDirText.getText());
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		boolean valid = false;
		
		ISelection selected = tableViewer.getSelection();
		valid = selected != null && selected instanceof IStructuredSelection
				&& !((IStructuredSelection) selected).isEmpty();
		if (valid) {
			valid = valgrindLocText.getText().length() > 0;
			if (valid) {
				valid = destDirText.getText().length() > 0;
				if (valid) {
					valid = tmpDirText.getText().length() > 0;
					if (!valid) {
						setErrorMessage(Messages.ValgrindRemoteTab_error_tmp_dir);
					}
				}
				else {
					setErrorMessage(Messages.ValgrindRemoteTab_error_dest_wd);
				}
			}
			else {
				setErrorMessage(Messages.ValgrindRemoteTab_error_location_VG);
			}
		}
		else {
			setErrorMessage(Messages.ValgrindRemoteTab_error_peer);
		}
		
		return valid;
	}

	public String getName() {
		return Messages.ValgrindRemoteTab_tab_name;
	}

	@Override
	public Image getImage() {
		return RemoteLaunchPlugin.imageDescriptorFromPlugin(RemoteLaunchPlugin.PLUGIN_ID, "icons/tcf.gif").createImage();
	}
	
	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}		
	}
	
	private void refreshPeerViewer() {
		if (tableViewer != null) {
			tableViewer.setInput(peers.values().toArray(new IPeer[peers.size()]));
			tableViewer.refresh();
		}
	}
}
