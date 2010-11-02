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
public class CpuLoadProcessor extends AbstractNotifier implements IListener {

	final private String CMD_FETCH_STAT = "/proc/stat"; //$NON-NLS-1$
	final private String PARSE_PATTERN_1 = "cpu(\\d*)(.*)"; //$NON-NLS-1$;
	final private String PARSE_PATTERN_2 = "\\s+(\\d+)"; //$NON-NLS-1$;
	final private String DATA_FORMAT = "%1$02.1f %%"; //$NON-NLS-1$;
	
	final private String MSG_EXECUTING_COMMAND =
		Messages.CpuLoadProcessor_Msg_Executing_the_command;

	final private String MSG_GOT_RESULT =
		Messages.CpuLoadProcessor_Msg_Got_The_Result;
	
	final private int MAX_COLUMNS = 100;
	
	private int[][] previousData = null;
	private long[] previousTotal = null;
	private IConnectionProvider connectionProvider = null;
	
	private ILogger logger = null;
	
	/**
	 * The constructor;
	 * @param connectionProvider
	 */
	public CpuLoadProcessor(IConnectionProvider connectionProvider) {
		setConnectionProvider(connectionProvider);
		logger = LinuxToolsPlugin.getLogger();
	}
	
	/**
	 * Requests data.
	 * @throws IOException
	 */
	public void gatherData() throws IOException {
		this.connectionProvider.sendCommand(CommandCode.FETCH_FILE, this.CMD_FETCH_STAT);
		logger.debug(MSG_EXECUTING_COMMAND + "\n" + this.CMD_FETCH_STAT); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.sequoyah.device.network.IListener#notify(org.eclipse.linuxtools.sequoyah.device.network.INotifier, org.eclipse.linuxtools.sequoyah.device.network.IConstants.EventCode, java.lang.Object)
	 */
	public void notify(INotifier notifier,
						EventCode event,
						Object result) {
		if (notifier == this.connectionProvider &&
				event == EventCode.EVT_PROVIDER_SENDCOMMAND_FINISHED) {
			Object[][] parsedResult = parseStat((StringBuffer) result);
			this.notifyListeners(EventCode.EVT_PROCESSOR_GATHERDATA_FINISHED,
					parsedResult);
		}
	}
	
	/**
	 * Set the connection provider.
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
	 * @param currentDataStr
	 * @return
	 */
	private Object[][] calculateTimes(String[][] currentDataStr) {
		String[][] resultStr = null;
		
		if (null == currentDataStr) {
			currentDataStr = new String[][] {{"0"}};
		}
		if (null == previousData) {
			previousData = new int[][] {{0}};
			previousTotal = new long[] {1};
		}
		if (previousData.length != currentDataStr.length ||
			previousData[0].length != currentDataStr[0].length) {
			previousData = String2int(currentDataStr);
			previousTotal = new long[previousData.length];
			
			for (int j = 0; j < previousData.length; j++) {
				long total = 0;
				for (int i = 0; i < previousData[0].length; i++) {
					total += previousData[j][i];
				}
				previousTotal[j] = total;
			}
		}
		final int[][] currentDataInt = String2int(currentDataStr);
		float[][] resultFloat = new float[currentDataStr.length][currentDataStr[0].length];

		for (int j = 0; j < currentDataInt.length; j++) {
			long total = 0;
			for (int i = 0; i < currentDataInt[0].length; i++) {
				total += currentDataInt[j][i];
			}
			float deltaTotal = total - previousTotal[j];
			previousTotal[j] = total;
			for (int i = 0; i < currentDataInt[0].length; i++) {
				float deltaData = currentDataInt[j][i] - previousData[j][i];
				if (0 == deltaTotal) {
					resultFloat[j][i] = 0;
				} else {
					resultFloat[j][i] = 100* deltaData / deltaTotal;
				}
			}
		}
		previousData = currentDataInt;
		resultStr = float2StringFormated(resultFloat, DATA_FORMAT);
		return resultStr;
	}

	/**
	 * @param target
	 * @param Source
	 */
	private void copyData(Object[][] target, Object[][] Source) {
		for (int j = 0; j < target.length; j++) {
			for (int i = 1; i < target[0].length; i++) {
				target[j][i] = Source[j][i-1];
			}
		}		
	}
	
	/**
	 * @param input
	 * @return
	 */
	private String[][] float2StringFormated (float[][] input, String format) {
		String[][] output = null;
		if (null != input) {
			output = new String[input.length][input[0].length];
			for (int j = 0; j < output.length; j++) {
				for (int i = 0; i < output[0].length; i++) {
					output[j][i] = String.format(format, new Float(input[j][i]));
				}
			}
		}
		return output;
	}

	/**
	 * @param data
	 * @return
	 */
	private Object[][] parseStat(StringBuffer data) {
		logger.debug(MSG_GOT_RESULT + "\n" + data.toString());

		int requiredColumns = 0;
		
		Scanner s1 = new Scanner(data.toString());
		ArrayList<String[]> list = new ArrayList<String[]>();
		
		while (s1.hasNextLine()) {
			String[] entry = new String[MAX_COLUMNS];;
			
			Scanner s2 = new Scanner(s1.nextLine());
			s2.findInLine(PARSE_PATTERN_1);
			
			try {

				MatchResult result = s2.match();
				entry[0] = result.group(1).trim();
		    
				String aux = result.group(2);
				
		    	Scanner s3 = new Scanner(aux);
		    	entry[1] = s3.findInLine(PARSE_PATTERN_2).trim();
		    	
		    	s3.match();
		    	int i = 2;
		    	
		    	while (s3.hasNext() && i < MAX_COLUMNS) {
		    		entry[i] = s3.next().trim();

		    		if (i > requiredColumns) {
			    		requiredColumns = i;
			    	}
			    	i++;
		    	}
				s3.close();
			    if (null != entry) {
			    	list.add(entry);
			    }
			} catch (IllegalStateException ise) {
			}
		    s2.close();
		}
	    s1.close();
	    
	    String[][] dataAux = new String[list.size()][requiredColumns + 1];
	    for (int j = 0; j < dataAux.length; j++) {
		    for (int i = 0; i < dataAux[0].length; i++) {
		    	dataAux[j][i] = list.get(j)[i];
		    }
	    }
	    String[][] dataStrippedAux = stripFirstColumn(dataAux);
	    Object[][] result = calculateTimes(dataStrippedAux);
	    copyData(dataAux, result);
		return dataAux;
	}

	/**
	 * @param input
	 * @return
	 */
	private int[][] String2int (String[][] input) {
		int[][] output = null;
		if (null != input) {
			output = new int[input.length][input[0].length];
			for (int j = 0; j < output.length; j++) {
				for (int i = 0; i < output[0].length; i++) {
					try {
						output[j][i] = new Integer(input[j][i]).intValue();
					}
					catch (NumberFormatException nfe) {
						output[j][i] = -1;
					}
				}
			}
		}
		return output;
	}
	
	/**
	 * @param input
	 * @return
	 */
	private String[][] stripFirstColumn (String[][] input) {
		String output[][] = null;
		if (null != input) {
			output = new String[input.length][input[0].length-1];
			for (int j = 0; j < output.length; j++) {
				for (int i = 0; i < output[0].length; i++) {
					output[j][i] = input[j][i+1];
				}
			}
		}
		return output;
	}
}
