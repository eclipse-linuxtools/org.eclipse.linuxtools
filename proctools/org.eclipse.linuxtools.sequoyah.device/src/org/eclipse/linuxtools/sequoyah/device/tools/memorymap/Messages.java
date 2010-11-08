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

package org.eclipse.linuxtools.sequoyah.device.tools.memorymap;

import org.eclipse.osgi.util.NLS;

/**
 * @author Otavio Ferranti
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"org.eclipse.linuxtools.sequoyah.device.tools.memorymap.messages"; //$NON-NLS-1$

	public static String MemoryMapProcessor_Msg_Executing_The_Command;
	public static String MemoryMapProcessor_Msg_Got_The_Result;
	public static String MemoryMapView_Action_Refresh;
	public static String MemoryMapView_Action_Disconnect;
	public static String MemoryMapView_Action_Connect;
	public static String MemoryMapView_Col_Label_Address_End;
	public static String MemoryMapView_Col_Label_Address_Start;
	public static String MemoryMapView_Col_label_Region;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
