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

/**
 * @author Otavio Ferranti
 */
public interface IConstants {

	public enum EventCode { EVT_PROVIDER_CONNECT_FINISHED,
							EVT_PROVIDER_CONNECT_ERROR,
							EVT_PROVIDER_LOGIN_FINISHED,
							EVT_PROVIDER_LOGIN_ERROR,
							EVT_PROVIDER_SENDCOMMAND_FINISHED,
							EVT_PROVIDER_SENDCOMMAND_ERROR,
							EVT_PROVIDER_SENDDATA_FINISHED,
							EVT_PROVIDER_SENDDATA_ERROR,
							EVT_PROVIDER_DISCONNECT_FINISHED,
							EVT_PROCESSOR_GATHERDATA_FINISHED,
							EVT_PROCESSOR_GATHERDATA_ERROR,
							EVT_TOOL_REFRESH_VIEW,
							EVT_TOOL_CONNECT_FINISHED,
							EVT_TOOL_LOGIN_FINISHED,
							EVT_TOOL_DISCONNECT_FINISHED };

	public enum OperationCode { SUCCESS,
								LOGIN_REQUIRED,
								LOGIN_FAILED,
								UNEXPECTED_RESULT };
								
	public enum CommandCode { FETCH_FILE };

}
