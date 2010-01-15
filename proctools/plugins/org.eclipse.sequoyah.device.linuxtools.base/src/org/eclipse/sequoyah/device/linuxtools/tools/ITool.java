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

package org.eclipse.sequoyah.device.linuxtools.tools;

import java.util.List;

import org.eclipse.sequoyah.device.linuxtools.utilities.ProtocolDescriptor;

/**
 * @author Otavio Ferranti
 */
public interface ITool extends INotifier {

	/**
	 * 
	 */
	public void disconnect();

	/**
	 * @param host
	 * @param port
	 * @param protocol
	 * @param viewer
	 */
	public void connect(String host, int port, ProtocolDescriptor protocol);

	/**
	 * @param user
	 * @param password
	 */
	public void login(String user, String password);
	
	/**
     *
	 */
	public List<ProtocolDescriptor> getProtocolsDescriptors();
	
	/**
	 * @param delay
	 */
	public int getRefreshDelay();

	/**
	 * 
	 */
	public void refresh();
	
	/**
	 * @param delay
	 */
	public void setRefreshDelay(int delay);
	
	/**
	 * 
	 */
	public void start();
	
	/**
	 * 
	 */
	public void stop ();
	
}