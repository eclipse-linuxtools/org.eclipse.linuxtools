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

package org.eclipse.linuxtools.sequoyah.device.tools.memorymap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.eclipse.linuxtools.sequoyah.device.LinuxToolsPlugin;
import org.eclipse.linuxtools.sequoyah.device.network.IConnectionProvider;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.CommandCode;
import org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode;
import org.eclipse.linuxtools.sequoyah.device.tools.AbstractNotifier;
import org.eclipse.linuxtools.sequoyah.device.tools.IListener;
import org.eclipse.linuxtools.sequoyah.device.tools.INotifier;
import org.eclipse.sequoyah.device.common.utilities.logger.ILogger;

/**
 * @author Otavio Ferranti
 */
public class MemoryMapProcessor extends AbstractNotifier implements IListener {

	final private String CMD_FETCH_IOMEM = "/proc/iomem"; //$NON-NLS-1$
	final private String PARSE_PATTERN = "\\s*(\\w{8})-(\\w{8})\\s*:\\s*(.*)"; //$NON-NLS-1$

	final private String MSG_EXECUTING_COMMAND =
		Messages.MemoryMapProcessor_Msg_Executing_The_Command;

	final private String MSG_GOT_RESULT =
		Messages.MemoryMapProcessor_Msg_Got_The_Result;
	
	final private int MAX_COLUMNS = 4;
	private IConnectionProvider connectionProvider = null;

	private ILogger logger = null;
	
	/**
	 * The constructor;
	 * @param connectionProvider
	 */
	public MemoryMapProcessor(IConnectionProvider connectionProvider) {
		setConnectionProvider(connectionProvider);
		logger = LinuxToolsPlugin.getLogger();
	}
	
	/**
	 * @throws IOException
	 */
	public void gatherData() throws IOException {
		connectionProvider.setResponseLength(8192);
		connectionProvider.sendCommand(CommandCode.FETCH_FILE, this.CMD_FETCH_IOMEM);
		logger.debug(MSG_EXECUTING_COMMAND + "\n" + this.CMD_FETCH_IOMEM); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IListener#notify(org.eclipse.linuxtools.sequoyah.device.network.INotifier, org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode, java.lang.Object)
	 */
	public void notify(INotifier notifier,
						EventCode event,
						Object result) {
		if (notifier == this.connectionProvider &&
				event == EventCode.EVT_PROVIDER_SENDCOMMAND_FINISHED) {
			this.connectionProvider.setResponseLength(1024);
			Object[][] parsedResult = parseIomem((StringBuffer) result);
			this.notifyListeners(EventCode.EVT_PROCESSOR_GATHERDATA_FINISHED,
					parsedResult);
		}
	}
	
	/**
	 * @param connectionProvider
	 */
	public void setConnectionProvider (IConnectionProvider connectionProvider) {
		if (null != this.connectionProvider) {
			this.connectionProvider.removeListener(this);
		}
		this.connectionProvider = connectionProvider;
		if (null != this.connectionProvider) {
			this.connectionProvider.addListener(this);
		}
	}

	/**
	 * @param data
	 * @return
	 */
	private Object[][] parseIomem(StringBuffer data) {
		logger.debug(MSG_GOT_RESULT + "\n" + data.toString());
		
		Scanner s1 = new Scanner(data.toString());
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		int j = 0;
		
		while (s1.hasNextLine()) {
			Scanner s2 = new Scanner(s1.nextLine());
			s2.findInLine(PARSE_PATTERN);

			String[] entry = null;
			try {
				MatchResult result = s2.match();
				entry = new String[MAX_COLUMNS];
			    for (int i = 1; i <= result.groupCount() && i <= MAX_COLUMNS - 1; i++) {
			    	entry[i-1] = result.group(i);
			    }
			    entry[MAX_COLUMNS - 1] = new Integer(j).toString();
			    j++;
			} catch (IllegalStateException ise) {
				//TODO: Nothing ?
			}

		    s2.close();
		    if (null != entry) {
		    	list.add(entry);
		    }
		}
	    s1.close();
	    
	    String[][] retVal = new String[list.size()][MAX_COLUMNS];
	    for (int i = 0; i < retVal.length; i++) {
	    	retVal[i] = list.get(i);
	    }
		return retVal;
	}
}
