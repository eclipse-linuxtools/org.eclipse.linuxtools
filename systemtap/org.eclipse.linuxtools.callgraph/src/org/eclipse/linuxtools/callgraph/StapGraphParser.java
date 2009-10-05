/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.callgraph.core.Helper;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.swt.widgets.Shell;


/**
 * This class is used only in the case that we are rendering a graph
 * using GEF (specifically Zest).  
 *
 * After a stap command is sent to be executed, and after data is stored
 * into some temporary file, the data must be parsed to be used. This class
 * handles all of the parsing. All data is stored into Maps and this class
 * also starts the job responsible for taking the parsed data and rendering it.
 */
public class StapGraphParser extends SystemTapParser {
	
	private static final String NEW_LINE = "\n"; //$NON-NLS-1$
	public  HashMap<Integer, Long> timeMap;
	public  TreeMap<Integer, String> serialMap;
	public  HashMap<Integer, ArrayList<Integer>> outNeighbours;
	public  HashMap<String, Long> cumulativeTimeMap;
	public  HashMap<String, Integer> countMap;
	public  ArrayList<Integer> callOrderList;
	public  HashMap<Integer, String> markedMap;
	public String markedNodes;
	public int validator;
	public Long endingTimeInNS;
	public long totalTime;
	public int lastFunctionCalled;
	public ICProject project;

	
	public String text;
	
	@Override
	protected void initialize() {
		//INITIALIZE MAPS
		outNeighbours = new HashMap<Integer, ArrayList<Integer>>();
		timeMap = new HashMap<Integer, Long>();
		serialMap = new TreeMap<Integer, String>();
		cumulativeTimeMap = new HashMap<String, Long>();
		countMap = new HashMap<String, Integer>();
		endingTimeInNS = 0l;
		callOrderList = new ArrayList<Integer>();
		markedMap = new HashMap<Integer, String>();
		lastFunctionCalled = 0;
		project = null;
	}

	
	public IStatus nonRealTimeParsing(){
		//Clear maps (in case a previous execution left values hanging)
		outNeighbours.clear();
		timeMap.clear();
		serialMap.clear();
		cumulativeTimeMap.clear();
		countMap.clear();
		text = ""; //$NON-NLS-1$
		callOrderList.clear();
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
			String tmp;
			while ((tmp = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				if (tmp.equals("PROBE_BEGIN")){ //$NON-NLS-1$
					tmp = buff.readLine();
					
					if (tmp != null && tmp.length() > 0) {
						project = CoreModel.getDefault().getCModel().getCProject(tmp);
					}
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
					
					text = buff.readLine();
					
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						endingTimeInNS = Long.parseLong(tmp);
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
					
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						totalTime = Long.parseLong(tmp);
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
				}
			}
			buff.close();
					
		} catch (IOException e) {
			launchFileErrorDialog();
			return Status.CANCEL_STATUS;
		}
		
		
		if (text.length() > 0) {
			
			boolean encounteredMain = false;
			
			ArrayList<Integer> shouldGetEndingTimeForID = new ArrayList <Integer>();
			String[] callsAndReturns = text.split(";"); //$NON-NLS-1$
			String[] args;
			ArrayList<String> nameList = new ArrayList<String>();
			ArrayList<Integer> idList = new ArrayList<Integer>();
			boolean skippedDirectives = false; 
			
			String name;
			int id;
			long time;
			long cumulativeTime;
			int parentID;
			int firstNode = -1;
			try {
			for (String s : callsAndReturns) {
				switch (s.charAt(0)) {
					case '<' :
						
						args = s.substring(1, s.length()).split(",,"); //$NON-NLS-1$
						// args[0] = name
						// args[1] = id
						// arsg[2] = time of event
						id = Integer.parseInt(args[1]);
						time = Long.parseLong(args[2]);
						name = args[0];
						
						//If we haven't encountered a main function yet and the name isn't clean,
						//and the name contains "__", then this is probably a C directive
						if (!encounteredMain && !isFunctionNameClean(name) && name.contains("__")) { //$NON-NLS-1$
							skippedDirectives = true;
							break;
						}
						name = cleanFunctionName(name);
						if (name.equals("main")) //$NON-NLS-1$
							encounteredMain = true;
						if (firstNode == -1) {
							firstNode = id;
						}
						
						serialMap.put(id, name);
						timeMap.put(id, time);
						
						if (cumulativeTimeMap.get(name) == null){
							cumulativeTimeMap.put(name, (long) 0);
						}

						//IF THERE ARE PREVIOUS FUNCTIONS WITH THE SAME NAME
						//WE ARE IN ONE OF THEM SO DO NOT ADD TO CUMULATIVE TIME
						if (nameList.indexOf(name) == -1) {
							cumulativeTime = cumulativeTimeMap.get(name) - time;
							cumulativeTimeMap.put(name, cumulativeTime);
							shouldGetEndingTimeForID.add(id);
						}
						
						
						if (countMap.get(name) == null){
							countMap.put(name, 0);
						}
						countMap.put(name, countMap.get(name) + 1);
						
						nameList.add(name);
						idList.add(id);
						
						if (outNeighbours.get(id) == null){
							outNeighbours.put(id, new ArrayList<Integer>());
						}
						
						if (idList.size() > 1) {
							parentID = idList.get(idList.size() - 2);
							outNeighbours.get(parentID).add(id);
						}
						
						callOrderList.add(id);
						lastFunctionCalled = id;

						break;
					case '>' :
						args = s.substring(1, s.length()).split(",,"); //$NON-NLS-1$
						//args[0] = name
						//args[1] = time of event
						name = args[0];
						
						
						//If we haven't encountered a main function yet and the name isn't clean,
						//and the name contains "__", then this is probably a C directive
						if (!encounteredMain && !isFunctionNameClean(name) && name.contains("__")) { //$NON-NLS-1$
							skippedDirectives = true;							
							break;
						}
						name = cleanFunctionName(name);
						int lastOccurance = nameList.lastIndexOf(name);
						if (lastOccurance < 0) {
							parsingError(Messages.getString("StapGraphParser.12") + name); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}
						
						nameList.remove(lastOccurance);
						id = idList.remove(lastOccurance);
						
						
						if (timeMap.get(id) == null) {
							parsingError(Messages.getString("StapGraphParser.13") + name); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}		
						time =  Long.parseLong(args[1]) - timeMap.get(id);
						timeMap.put(id, time);
						
						
						//IF AN ID IS IN THIS ARRAY IT IS BECAUSE WE NEED THE ENDING TIME
						// TO BE ADDED TO THE CUMULATIVE TIME FOR FUNCTIONS OF THIS NAME
						if (shouldGetEndingTimeForID.contains(id)){
							cumulativeTime = cumulativeTimeMap.get(name) + Long.parseLong(args[1]);
							cumulativeTimeMap.put(name, cumulativeTime);
						}
						
						
						//Use + for end times
//						cumulativeTime = cumulativeTimeMap.get(name) + Long.parseLong(args[1]);
//						cumulativeTimeMap.put(name, cumulativeTime);
						
						break;
					default : 
						parsingError(Messages.getString("StapGraphParser.14") + s.charAt(0) + //$NON-NLS-1$
								Messages.getString("StapGraphParser.15") ); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					
				} 
				
			} 
			
			
			//CHECK FOR EXIT() CALL
			if (idList.size() != 0){
				for (int val : idList){
					name = serialMap.get(val);
					time =  endingTimeInNS - timeMap.get(val);
					timeMap.put(val, time);
					if (shouldGetEndingTimeForID.contains(val)){
						cumulativeTime = cumulativeTimeMap.get(name) + endingTimeInNS;
						cumulativeTimeMap.put(name, cumulativeTime);
					}
					
//					if (name.equals("main")) {
//						totalTime = time;
//					}
					lastFunctionCalled = val;
				}
				markedMap.put(lastFunctionCalled, Messages.getString("StapGraphParser.16")); //$NON-NLS-1$
			}
				
			//timecheck is true if the total execution time is less than 10ms
			//and the first function is more than 1% off from the total time.
			boolean timeCheck = totalTime < 50000000 && 
								(((float)timeMap.get(firstNode)/totalTime) > 1.01 ||
								((float)timeMap.get(firstNode)/totalTime) < 0.99);
			
			
								
			if (skippedDirectives || timeCheck) {
				totalTime = timeMap.get(firstNode);
				String markedMessage = ""; //$NON-NLS-1$
				if (markedMap.containsKey(firstNode)) {
					markedMessage = markedMap.get(firstNode) + "\n"; //$NON-NLS-1$
				}
				if (skippedDirectives)
					markedMessage += Messages.getString("StapGraphParser.19"); //$NON-NLS-1$
				if (timeCheck)
					markedMessage += Messages.getString("StapGraphParser.20"); //$NON-NLS-1$
				
				markedMessage += Messages.getString("StapGraphParser.21"); //$NON-NLS-1$
				
				markedMap.put(firstNode, markedMessage);
			}
			
			
			} catch (NumberFormatException e) {
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("StapGraphParser.22"),  //$NON-NLS-1$
						Messages.getString("StapGraphParser.23"), Messages.getString("StapGraphParser.24") + //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("StapGraphParser.25")); //$NON-NLS-1$
				mess.schedule();
				
				return Status.CANCEL_STATUS;
			}
		} else {
			parsingError(Messages.getString("StapGraphParser.26")); //$NON-NLS-1$
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	

	public void saveData(String filePath) {
		File file = new File(filePath);
		String content = Messages.getString("CallgraphView.25") //$NON-NLS-1$
		+ project.getElementName()
		+ NEW_LINE
		+ text
		+ NEW_LINE
		+ endingTimeInNS
		+ NEW_LINE
		+ totalTime;
		try {
			// WAS THE FILE CREATED OR DOES IT ALREADY EXIST
			if (file.createNewFile()) {
				Helper.writeToFile(filePath, content);
			} else {
				if (MessageDialog
						.openConfirm(
								new Shell(),
								Messages
										.getString("CallgraphView.FileExistsTitle"), //$NON-NLS-1$
								Messages
										.getString("CallgraphView.FileExistsMessage"))) { //$NON-NLS-1$
					file.delete();
					file.createNewFile();
					Helper.writeToFile(filePath, content);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}


	@Override
	public IStatus realTimeParsing() {
		return Status.CANCEL_STATUS;
	}

}
