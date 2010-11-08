/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.network;

import java.io.IOException;

import org.eclipse.linuxtools.sequoyah.device.network.IConstants.CommandCode;
import org.eclipse.linuxtools.sequoyah.device.tools.INotifier;

/**
 * @author Otavio Ferranti
 */
public interface IConnectionProvider extends INotifier{

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public void connect(String host, int port) throws IOException ;
	
	/**
	 * @throws IOException
	 */
	public void disconnect() throws IOException ;

	/**
	 * This method will be executed in a separated thread and will produce
	 * an event to be sent to the registered listeners. 
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	public void login(String user, String password) throws IOException ;
	
	/**
	 * Retrieves the last response.
	 * @return
	 */
	public StringBuffer getLastResponde() ;

	/**
	 * Sends a command to be executed and waits for the results.
	 * @param command
	 * @throws IOException
	 */
	public void sendCommand(CommandCode cmd, String cmdStr) throws IOException ;

	/**
	 * Sends some command or data without waiting any response or result.
	 * @param out
	 */
	public void sendData(String out) ;
	
	/**
	 * Sets the maximum response length.
	 * @param maxLength
	 */
	public void setResponseLength(int maxLength) ;
	
}
