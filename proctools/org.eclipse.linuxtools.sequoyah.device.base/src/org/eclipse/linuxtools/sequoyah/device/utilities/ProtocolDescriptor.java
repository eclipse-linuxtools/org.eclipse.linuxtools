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
 * Otavio Ferranti - Eldorado Research Institute - Bug 255255 [tml][proctools] Add extension points
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah 
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.utilities;

import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;

public class ProtocolDescriptor {

	private Class<IConnectionProvider> connectionProviderClass = null;
	private String name = null;
	private String id = null;
	private int defaultPort = -1;
		
	ProtocolDescriptor(Class<IConnectionProvider> connectionProviderClass,
						String name, String id, int defaultPort) {
		this.connectionProviderClass = connectionProviderClass;
		this.name = name;
		this.id = id;
		this.defaultPort = defaultPort;
	}
	
	public Class<IConnectionProvider> getConnectionProviderClass() {
		return connectionProviderClass;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}

	public int getDefaultPort() {
		return defaultPort;
	}
}
