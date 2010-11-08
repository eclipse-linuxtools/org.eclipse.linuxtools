/********************************************************************************
 * Copyright (c) 2009 Motorola Inc. All rights reserved.
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

package org.eclipse.linuxtools.sequoyah.device.network.telnet;

import java.io.IOException;

import org.eclipse.linuxtools.sequoyah.device.LinuxToolsPlugin;
import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.CommandCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;
import org.eclipse.linuxtools.sequoyah.device.tools.AbstractNotifier;
import org.eclipse.sequoyah.device.common.utilities.logger.ILogger;

/**
 * @author Ot�vio Ferranti
 */
public class TelnetProvider extends AbstractNotifier implements IConnectionProvider {

	final private String CMD_FETCH_PREFIX = "cat "; //$NON-NLS-1$
	
	private TelnetWrapper connectionWrapper = null;
	private ILogger logger = null;
	
	private Thread connectThread = null;
	private Thread loginThread = null;
	private Thread sendCommandThread = null;
	
	/**
	 * The constructor.
	 */
	public TelnetProvider() {
		connectionWrapper = new TelnetWrapper();
		logger = LinuxToolsPlugin.getLogger();
	}

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#connect(java.lang.String, int)
	 */
	public void connect(String host, int port) throws IOException {
		final String hostAux = host;
		final int portAux = port;
		final TelnetWrapper connectionWrapperAux = connectionWrapper;

		/*TODO: Enhance this. It would be great if all these executed-in-other threads
				were located under a synchronized block */ 
		
		connectThread = new Thread() {
			public void run() {
				try {
					OperationCode opCode;
					opCode = connectionWrapperAux.connect(hostAux, portAux);
					TelnetProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_CONNECT_FINISHED, opCode);
				} catch (IOException ie) {
					TelnetProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_CONNECT_ERROR,
							OperationCode.UNEXPECTED_RESULT);					
				}
				
			}
		};
		connectThread.start();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#disconnect()
	 */
	public void disconnect() throws IOException {
		if (null != connectThread &&
				connectThread.isAlive()) {
			connectThread.interrupt();
		}
		if (null != loginThread &&
				loginThread.isAlive()) {
			loginThread.interrupt();
		}
		if (null != sendCommandThread &&
				sendCommandThread.isAlive()) {
			sendCommandThread.interrupt();
		}
		connectionWrapper.disconnect();
		notifyListeners(
				EventCode.EVT_PROVIDER_DISCONNECT_FINISHED, OperationCode.SUCCESS);
	}

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#login(java.lang.String, java.lang.String)
	 */
	public void login(String user, String password) throws IOException {
		final String userAux = user;
		final String passwordAux = password;
		final TelnetWrapper connectionWrapperAux = connectionWrapper;

		/*TODO: Enhance this. It would be great if all these executed-in-other threads
				were located under a synchronized block */ 
		loginThread = new Thread() {
			public void run() {
				try {
					OperationCode opCode;
					opCode = connectionWrapperAux.login(userAux, passwordAux);
					TelnetProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_LOGIN_FINISHED, opCode);
				} catch (IOException ie) {
					TelnetProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_LOGIN_ERROR,
							OperationCode.UNEXPECTED_RESULT);					
				}
			}
		};
		loginThread.start();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#getLastResponde()
	 */
	public StringBuffer getLastResponde() {
		return connectionWrapper.getLastResponde();
	}

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#sendCommand(java.lang.String)
	 */
	public void sendCommand(CommandCode cmd, String cmdStr) throws IOException {
		
		if (CommandCode.FETCH_FILE != cmd) {
			return;
		}

		final String commandAux = CMD_FETCH_PREFIX + cmdStr + "\n"; //$NON-NLS-1$
		
		final TelnetWrapper connectionWrapperAux = connectionWrapper;

		/*TODO: Enhance this. It would be great if all these executed-in-other threads
				were located under a synchronized block */ 
		sendCommandThread = new Thread() {
			public void run() {
				try {
					connectionWrapperAux.setResponseLength(2048);
					if (null != connectionWrapperAux.sendCommand(commandAux)) {
						StringBuffer result = connectionWrapperAux.getLastResponde();
						TelnetProvider.this.notifyListeners(
								EventCode.EVT_PROVIDER_SENDCOMMAND_FINISHED, result);
					} else {
						logger.info("##### NULL ######");
					}
				} catch (IOException ie) {
					TelnetProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_SENDCOMMAND_ERROR, null);					
				}
				
			}
		};
		sendCommandThread.start();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#sendData(java.lang.String)
	 */
	public void sendData(String out) {
		connectionWrapper.sendData(out);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tml.linuxmemorymapviewer.network.IConnectionProvider#setResponseLength(int)
	 */
	public void setResponseLength(int maxLength) {
		connectionWrapper.setResponseLength(maxLength);
	}
}
