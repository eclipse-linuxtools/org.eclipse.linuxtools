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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;
import org.eclipse.sequoyah.device.common.utilities.PluginUtils;

public class Extensions {

	static final private String PROTOCOLS_EXTENSION =
		"org.eclipse.sequoyah.device.linuxtools.base.protocols"; //$NON-NLS-1$
	
	static final private String CLASS = "class"; //$NON-NLS-1$
	static final private String DEFAULT_PORT = "default_port"; //$NON-NLS-1$
	static final private String ID = "id"; //$NON-NLS-1$
	static final private String NAME = "name"; //$NON-NLS-1$
	static final private String CAPABILITY = "capability";
	
	static public List <ProtocolDescriptor> findProcotols (String[] reqCapabilities) {
		Collection<IExtension> extensions =
			PluginUtils.getInstalledExtensions(PROTOCOLS_EXTENSION);
		
		List <ProtocolDescriptor> pdList = new LinkedList<ProtocolDescriptor>();
		
		for (IExtension extension:extensions){
			IConfigurationElement[] protocolElements = extension.getConfigurationElements();
			for (IConfigurationElement protocolElement:protocolElements) {
				IConfigurationElement[] capabilityElements = protocolElement.getChildren(CAPABILITY);
				
				boolean allCapabilitiesFound = true;
				for (String reqCapability:reqCapabilities) {
					boolean capabilityMissing = true;
					for (IConfigurationElement capabilityElement:capabilityElements) {
						String capabilityStr = capabilityElement.getAttribute(ID);
						if(capabilityStr.equalsIgnoreCase(reqCapability)) {
							capabilityMissing = false; //Capability found
							break;
						}
					}
					if (true == capabilityMissing) {
						allCapabilitiesFound = false;
						break; // Could not find one of the req'd capabilities
					}
				}
				
				if(true == allCapabilitiesFound) {
					String protDefaultPortStr = protocolElement.getAttribute(DEFAULT_PORT);
					Integer protDefaultPort = new Integer(protDefaultPortStr);
					String protIdStr = protocolElement.getAttribute(ID);
					String protNameStr = protocolElement.getAttribute(NAME);
					try {
						IConnectionProvider protConnProv = (IConnectionProvider) protocolElement.createExecutableExtension(CLASS);
						pdList.add(new ProtocolDescriptor((Class) protConnProv.getClass(), protNameStr,
														  protIdStr, protDefaultPort.intValue()));
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return pdList;
	}
}
