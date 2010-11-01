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

package org.eclipse.sequoyah.device.linuxtools.tools.memorymap;

import java.io.IOException;
import java.util.List;

import org.eclipse.sequoyah.device.linuxtools.network.IConnectionProvider;
import org.eclipse.sequoyah.device.linuxtools.network.IConstants.EventCode;
import org.eclipse.sequoyah.device.linuxtools.network.IConstants.OperationCode;
import org.eclipse.sequoyah.device.linuxtools.tools.AbstractNotifier;
import org.eclipse.sequoyah.device.linuxtools.tools.IListener;
import org.eclipse.sequoyah.device.linuxtools.tools.INotifier;
import org.eclipse.sequoyah.device.linuxtools.tools.ITool;
import org.eclipse.sequoyah.device.linuxtools.utilities.Extensions;
import org.eclipse.sequoyah.device.linuxtools.utilities.ProtocolDescriptor;

/**
 * @author Otavio Ferranti
 */
public class MemoryMapTool extends AbstractNotifier implements IListener, ITool {

	private IConnectionProvider connectionProvider = null;
	private MemoryMapProcessor processor = null;

	private String[] requiredCapabilities = {"GET_FILE"}; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.memorymap.iTool#createInstance(java.lang.String, int, java.lang.String)
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
		processor = new MemoryMapProcessor(connectionProvider);

		processor.addListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.ITool#closeConnection()
	 */
	public void disconnect() {
		if (null != connectionProvider) {
			try {
				connectionProvider.disconnect();
			} catch (IOException ie) {
			}
			connectionProvider = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.memorymap.iTool#login(java.lang.String, java.lang.String)
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
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.IListener#notify(org.eclipse.sequoyah.device.linuxtools.tools.INotifier, org.eclipse.sequoyah.device.linuxtools.network.IConstants.EventCode, java.lang.Object)
	 */
	public void notify(INotifier notifier, EventCode event, Object result) {
		try {
			if (notifier == connectionProvider) {
				switch (event) {
					case EVT_PROVIDER_CONNECT_FINISHED:
						if (OperationCode.SUCCESS == result) {
							processor.gatherData();
						}
						this.notifyListeners(
								EventCode.EVT_TOOL_CONNECT_FINISHED,
								result);
					break;
					case EVT_PROVIDER_CONNECT_ERROR:
					break;
					case EVT_PROVIDER_LOGIN_FINISHED:
						if (OperationCode.SUCCESS == result) {
							processor.gatherData();
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
						notifyListeners(EventCode.EVT_TOOL_REFRESH_VIEW,
										result);
					break;
					case EVT_PROCESSOR_GATHERDATA_ERROR:
					break;
				}
			}
		} catch (IOException ie) {
		}
	}

	/**
	 * 
	 */
	public void refresh() {
		try {
			if (null != processor) {
				processor.gatherData();
			}
		} catch (IOException ie) {
			//TODO: Nothing ?
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.ITool#getRefreshDelay()
	 */
	public int getRefreshDelay() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sequoyah.device.linuxtools.tools.ITool#setRefreshDelay(int)
	 */
	public void setRefreshDelay(int delay) {
		// TODO Auto-generated method stub
	}

	public void start() {
		// TODO Auto-generated method stub
	}

	public void stop() {
		// TODO Auto-generated method stub
	}
}
