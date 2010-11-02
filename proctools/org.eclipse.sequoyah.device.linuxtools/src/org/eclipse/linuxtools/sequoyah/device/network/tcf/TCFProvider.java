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

package org.eclipse.linuxtools.sequoyah.device.network.tcf;

import java.io.IOException;

import org.eclipse.linuxtools.sequoyah.device.LinuxToolsPlugin;
import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.CommandCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.OperationCode;
import org.eclipse.linuxtools.sequoyah.device.tools.AbstractNotifier;
import org.eclipse.sequoyah.device.common.utilities.logger.ILogger;

public class TCFProvider extends AbstractNotifier implements
		IConnectionProvider {

	private TCFWrapper connectionWrapper = null;
	private ILogger logger = null;
	
	private Thread connectThread = null;
	private Thread sendCommandThread = null;
	
	/**
	 * The constructor.
	 */
	public TCFProvider() {
		connectionWrapper = new TCFWrapper();
		logger = LinuxToolsPlugin.getLogger();
	}

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 */
	public void connect(String host, int port) throws IOException {
		final String hostAux = host;
		final int portAux = port;
		final TCFWrapper connectionWrapperAux = connectionWrapper;

		/*TODO: Enhance this. It would be great if all these executed-in-other threads
				were located under a synchronized block */ 
		
		connectThread = new Thread() {
			public void run() {
				try {
					connectionWrapperAux.connect(hostAux, portAux);
					TCFProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_CONNECT_FINISHED,
							OperationCode.SUCCESS);
				} catch (IOException ie) {
					TCFProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_CONNECT_ERROR,
							OperationCode.UNEXPECTED_RESULT);					
				}
				
			}
		};
		connectThread.start();
	}

	public void disconnect() throws IOException {
		if (null != connectThread &&
				connectThread.isAlive()) {
			connectThread.interrupt();
		}

		if (null != sendCommandThread &&
				sendCommandThread.isAlive()) {
			sendCommandThread.interrupt();
		}
		connectionWrapper.disconnect();
		notifyListeners(
				EventCode.EVT_PROVIDER_DISCONNECT_FINISHED, OperationCode.SUCCESS);
	}

	public StringBuffer getLastResponde() {
		return connectionWrapper.getLastResponde();
	}

	public void login(String user, String password) throws IOException {

	}

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 */
	public void sendCommand(CommandCode cmd, String cmdStr) throws IOException {

		if (CommandCode.FETCH_FILE != cmd) {
			return;
		}

		final TCFWrapper connectionWrapperAux = connectionWrapper;
	
		final String path = cmdStr.substring(0, cmdStr.lastIndexOf("/"));
		final String fileName = cmdStr.substring(cmdStr.lastIndexOf("/"));

		sendCommandThread = new Thread() {
			public void run() {
				try {
					
					if (null != connectionWrapperAux.fetchFile(path, fileName)) {
						StringBuffer result = connectionWrapperAux.getLastResponde();
						TCFProvider.this.notifyListeners(
								EventCode.EVT_PROVIDER_SENDCOMMAND_FINISHED, result);
					} else {
						logger.info("##### NULL ######");
					}
				} catch (IOException ie) {
					TCFProvider.this.notifyListeners(
							EventCode.EVT_PROVIDER_SENDCOMMAND_ERROR, null);					
				}
				
			}
		};
		sendCommandThread.start();
	}

	public void sendData(String out) {
		connectionWrapper.sendData(out);
	}

	public void setResponseLength(int maxLength) {
		connectionWrapper.setResponseLength(maxLength);
	}
}
