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

package org.eclipse.linuxtools.sequoyah.device.tools.cpuload;

import org.eclipse.osgi.util.NLS;

/**
 * @author Otavio Ferranti
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"org.eclipse.linuxtools.sequoyah.device.tools.cpuload.messages"; //$NON-NLS-1$
	public static String CpuLoadProcessor_Msg_Executing_the_command;
	public static String CpuLoadProcessor_Msg_Got_The_Result;
	public static String CpuLoadView_Action_Connect;
	public static String CpuLoadView_Action_Disconnect;
	public static String CpuLoadView_Action_Options;
	public static String CpuLoadView_Action_Pause;
	public static String CpuLoadView_Action_Run;
	public static String CpuLoadView_Col_Label_Cpu;
	public static String CpuLoadView_Col_label_HIrq;
	public static String CpuLoadView_Col_Label_Idle;
	public static String CpuLoadView_Col_Label_Nice;
	public static String CpuLoadView_Col_Label_SIrq;
	public static String CpuLoadView_Col_Label_System;
	public static String CpuLoadView_Col_Label_User_Mode;
	public static String CpuLoadView_Col_Label_Wait;
	public static String OptionsDialog_Label_Refresh_Rate;
	public static String OptionsDialog_Window_Message;
	public static String OptionsDialog_Window_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
