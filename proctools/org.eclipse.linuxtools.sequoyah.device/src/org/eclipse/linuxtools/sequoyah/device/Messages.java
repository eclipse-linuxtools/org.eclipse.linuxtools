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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device;

import org.eclipse.osgi.util.NLS;

/**
 * @author Otavio Ferranti
 *
 */
public class Messages extends NLS {

	private static String BUNDLE_NAME = "org.eclipse.linuxtools.sequoyah.device.messages"; //$NON-NLS-1$
	public static String TML_Plugin_Name;
	public static String TML_Error;
	public static String TML_Resource_Not_Available;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}	
}
