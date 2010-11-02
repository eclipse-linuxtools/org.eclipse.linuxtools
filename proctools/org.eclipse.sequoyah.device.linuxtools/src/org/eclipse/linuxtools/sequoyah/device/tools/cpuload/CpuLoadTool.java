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
 * Otavio Ferranti - Eldorado Research Institute - Bug 255255 [tml][proctools] Add extension points 
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.tools.cpuload;

import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;
import org.eclipse.linuxtools.sequoyah.device.tools.AbstractNotifier;
import org.eclipse.linuxtools.sequoyah.device.tools.IListener;
import org.eclipse.linuxtools.sequoyah.device.tools.INotifier;
import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.linuxtools.sequoyah.device.utilities.Extensions;
import org.eclipse.linuxtools.sequoyah.device.utilities.ProtocolDescriptor;

/**
 * @author Otavio Ferranti
 */
public class CpuLoadTool extends AbstractNotifier implements IListener, ITool {

	public enum ToolStateEnum {RUNNING, STOPPED, STOP_SCHEDULED};
	
	private IConnectionProvider connectionProvider = null;
	private CpuLoadProcessor processor = null;
	private int delay = 1000;
	
	private ToolStateEnum toolState = ToolStateEnum.STOPPED;
	
	private String[] requiredCapabilities = {"GET_FILE"}; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.ITool#createConnection(java.lang.String, int, java.lang.String)
	 */
	public void connect (String host,
								int port,
								ProtocolDescriptor protocol) {
	
		Class<IConnectionProvider> connectionProviderClass =
			protocol.getConnectionProviderClass();
		
		try {
			Object aux = connectionProviderClass.newInstance();
			connectionProvider = (IConnectionProvider) aux;
		} catch (InstantiationException ie) {
		} catch (IllegalAccessException iae) {
		} catch (ClassCastException cce) {
		}
	
		connectionProvider.addListener(this);
		try {
			connectionProvider.connect(host, port);
		} catch (IOException ie) { }
		processor = new CpuLoadProcessor(connectionProvider);

		processor.addListener(this);
		toolState = ToolStateEnum.STOPPED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.ITool#closeConnection()
	 */
	public void disconnect() {
		if (null != connectionProvider) {
			try {
				connectionProvider.disconnect();
			} catch (IOException ie) {
			}
			connectionProvider = null;
			toolState = ToolStateEnum.STOPPED;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.ITool#login(java.lang.String, java.lang.String)
	 */
	public void login (String user, String password) {
		try {
			connectionProvider.login(user, password);
		} catch (IOException ie) {
			//TODO: Nothing ?
		}
	}
	
	public List<ProtocolDescriptor> getProtocolsDescriptors() {
		List <ProtocolDescriptor> pdList = Extensions.findProcotols(requiredCapabilities);
		return pdList;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.IListener#notify(org.eclipse.linuxtools.sequoyah.device.tools.INotifier, org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode, java.lang.Object)
	 */
	public void notify(INotifier notifier, EventCode event, Object result) {
		try {
			if (notifier == connectionProvider) {
				switch (event) {
					case EVT_PROVIDER_CONNECT_FINISHED:
						if (OperationCode.SUCCESS == result) {
							start();
						}
						this.notifyListeners(
								EventCode.EVT_TOOL_CONNECT_FINISHED,
								result);
					break;
					case EVT_PROVIDER_CONNECT_ERROR:
						
					break;
					case EVT_PROVIDER_LOGIN_FINISHED:
						if (OperationCode.SUCCESS == result) {
							start();
						}
						this.notifyListeners(
								EventCode.EVT_TOOL_LOGIN_FINISHED,
								result);	
					break;
					case EVT_PROVIDER_SENDCOMMAND_FINISHED:
					break;
					case EVT_PROVIDER_SENDCOMMAND_ERROR:
					break;
					case EVT_PROVIDER_SENDDATA_FINISHED:
					break;
					case EVT_PROVIDER_SENDDATA_ERROR:
					break;
					case EVT_PROVIDER_DISCONNECT_FINISHED:
						this.notifyListeners(
								EventCode.EVT_TOOL_DISCONNECT_FINISHED,
								result);	
					break;	
				}
			}
			if (notifier == processor) {
				switch (event) {
					case EVT_PROCESSOR_GATHERDATA_FINISHED:
						if (ToolStateEnum.RUNNING == toolState) {
							try {
								notifyListeners(EventCode.EVT_TOOL_REFRESH_VIEW,
										result);
								Thread.sleep(delay);
								processor.gatherData();
							} catch (InterruptedException ie) {
							}
						} else if (ToolStateEnum.STOP_SCHEDULED == toolState) {
							toolState = ToolStateEnum.STOPPED;
						}
					break;
					case EVT_PROCESSOR_GATHERDATA_ERROR:
					break;
				}
			}
		} catch (IOException ie) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.ITool#getRefreshDelay()
	 */
	public int getRefreshDelay() {
		return this.delay;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.tools.ITool#setRefreshDelay(int)
	 */
	public void setRefreshDelay(int delay) {
		this.delay = delay;
	}
	
	/**
	 * 
	 */
	public void start() {
		try {
			if (null != processor) {
				if (ToolStateEnum.STOPPED == toolState) {
					toolState = ToolStateEnum.RUNNING;
					processor.gatherData();
				} else if (ToolStateEnum.STOP_SCHEDULED == toolState) {
					toolState = ToolStateEnum.RUNNING;
				}
			}
		} catch (IOException ie) {
			//TODO: Nothing ?
		}
	}
	
	/**
	 * 
	 */
	public void stop () {
		toolState = ToolStateEnum.STOP_SCHEDULED;
	}

	public void refresh() {
		// TODO Auto-generated method stub
	}
}
