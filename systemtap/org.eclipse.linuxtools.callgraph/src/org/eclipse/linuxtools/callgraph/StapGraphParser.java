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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;


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
	
	public  HashMap<Integer, Long> timeMap;
	public  TreeMap<Integer, String> serialMap;
	public  HashMap<Integer, ArrayList<Integer>> outNeighbours;
	public  HashMap<String, Long> aggregateTimeMap;
	public  HashMap<String, Integer> countMap;
	public  ArrayList<Integer> callOrderList;
	public  HashMap<Integer, String> markedMap;
	public String markedNodes;
	public int validator;
	public Long endingTimeInNS;
	public long totalTime;
	public int lastFunctionCalled;
	public ICProject project;
	private String[] markedMessages;
	private static final String DELIM = ",,"; //$NON-NLS-1$
	
	private boolean encounteredMain = false;
	private ArrayList<Integer> shouldGetEndingTimeForID = new ArrayList <Integer>();
	
	private ArrayList<String> nameList = new ArrayList<String>();
	private ArrayList<Integer> idList = new ArrayList<Integer>();
	private boolean skippedDirectives = false; 			
	private int firstNode = -1;
	
	public long startTime = -1;
	
	public String text;
	
	@Override
	protected void initialize() {
		//INITIALIZE MAPS
		outNeighbours = new HashMap<Integer, ArrayList<Integer>>();
		timeMap = new HashMap<Integer, Long>();
		serialMap = new TreeMap<Integer, String>();
		aggregateTimeMap = new HashMap<String, Long>();
		countMap = new HashMap<String, Integer>();
		endingTimeInNS = 0l;
		callOrderList = new ArrayList<Integer>();
		markedMap = new HashMap<Integer, String>();
		lastFunctionCalled = 0;
		project = null;
		markedMessages = null;
		startTime = -1;
	}

	
	public IStatus nonRealTimeParsing(){
		//Clear maps (in case a previous execution left values hanging)
		outNeighbours.clear();
		timeMap.clear();
		serialMap.clear();
		aggregateTimeMap.clear();
		countMap.clear();
		text = ""; //$NON-NLS-1$
		callOrderList.clear();
		shouldGetEndingTimeForID.clear();
		nameList.clear();
		idList.clear();
		encounteredMain = false;
		skippedDirectives = false; 			
		firstNode = -1;
		startTime = -1;
		
		BufferedReader buff = null;
		try {
			buff = new BufferedReader(new FileReader(sourcePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			if (buff == null) {
				launchFileErrorDialog();
				return Status.CANCEL_STATUS;
			}
			
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
					
					StringBuilder builder = new StringBuilder();
					text = buff.readLine();
					if (text == null || text.length() < 1) {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
					builder.append(text);
					while ((tmp = buff.readLine()) != null) {
						if (tmp.charAt(0) == '-' || tmp.charAt(0) == '+')
							break;
						builder.append(tmp);
					}
					text = builder.toString();
					
					if (tmp != null && tmp.length() > 0)
						endingTimeInNS = Long.parseLong(tmp.substring(1));
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
					
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						totalTime = Long.parseLong(tmp.substring(1));
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
					
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						markedMessages = tmp.substring(1).split(";"); //$NON-NLS-1$
					else
						//Not having any marked messages is not an error
						markedMessages = null;
				}
			}
			
		} catch (IOException e) {
			launchFileErrorDialog();
			return Status.CANCEL_STATUS;
		} finally {
			if (buff != null)
				try {
					buff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		
		if (text.length() > 0) {
			if (parse(text) == Status.OK_STATUS) {  //$NON-NLS-1$
				parseEnd();
				parseMarked();
			}
			else
				return Status.CANCEL_STATUS;
		} else {
			parsingError(Messages.getString("StapGraphParser.26")); //$NON-NLS-1$
			return Status.CANCEL_STATUS;
		}
		
		return Status.OK_STATUS;
	}

	
	private void parseEnd() {

		
		//CHECK FOR EXIT() CALL
		if (idList.size() > 1) {
			for (int val : idList){
				String name = serialMap.get(val);
				long time =  endingTimeInNS - timeMap.get(val);
				timeMap.put(val, time);
				if (val == firstNode)
					showTime(val, time);
				if (shouldGetEndingTimeForID.contains(val)){
					long cumulativeTime = aggregateTimeMap.get(name) + endingTimeInNS;
					aggregateTimeMap.put(name, cumulativeTime);
				}
				
				lastFunctionCalled = val;
			}
			String tmp = markedMap.get(lastFunctionCalled);
			if (tmp == null) tmp = "";
			markedMap.put(lastFunctionCalled, 
					tmp + "\n" + Messages.getString("StapGraphParser.16")); //$NON-NLS-1$
		}
		
		
		//timecheck is true if the total execution time is less than 10ms
		//and the first function is more than 1% off from the total time.
		boolean timeCheck = totalTime < 50000000 && 
							(((float)timeMap.get(firstNode)/totalTime) > 1.01 ||
							((float)timeMap.get(firstNode)/totalTime) < 0.99);

		/*
		 * Indicate whether or not we had to manipulate total time, and why
		 */
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
	}
	
	private void parseMarked() {
		/*
		 * Parse marked messages for marked nodes 
		 */
		if (markedMessages != null) {
			//Search for marked nodes
			for (String s : markedMessages) {
				String[] tokens = s.split(DELIM);
				if (tokens.length > 1) {
					String msg = tokens[1];
					if (msg.equals("<unknown>")) { //$NON-NLS-1$
						msg = msg + Messages.getString("StapGraphParser.UnknownMarkers"); //$NON-NLS-1$
					}
					int i = Integer.parseInt(tokens[0]);
					markedMap.put(i, markedMap.get(i) + msg);
				}
			}
		}
		
	}
	
	private IStatus parse(String data) {
		String[] callsAndReturns = data.split(";"); //$NON-NLS-1$

		try {
		for (String s : callsAndReturns) {
			if (s.length() < 1)
				continue;
			switch (s.charAt(0)) {
				case '<' :
					/*
					 * 
					 * Open tag -- function call
					 * 
					 * 
					 */
					String[] args = s.substring(1, s.length()).split(DELIM); //$NON-NLS-1$
					// args[0] = name
					// args[1] = id
					// arsg[2] = time of event
					int id = Integer.parseInt(args[1]);
					long time = Long.parseLong(args[2]);
					String name = args[0];
					
					//If we haven't encountered a main function yet and the name isn't clean,
					//and the name contains "__", then this is probably a C directive
					if (!encounteredMain && !isFunctionNameClean(name) && name.contains("__")) { //$NON-NLS-1$
						skippedDirectives = true;
						break;
					}
					if (startTime < 1) {
						startTime = time;
					}
					endingTimeInNS=time;
					
					name = cleanFunctionName(name);
					if (name.equals("main")) //$NON-NLS-1$
						encounteredMain = true;
					if (firstNode == -1) {
						firstNode = id;
					}
					
					serialMap.put(id, name);
					timeMap.put(id, time);
					
					if (aggregateTimeMap.get(name) == null){
						aggregateTimeMap.put(name, (long) 0);
					}

					//IF THERE ARE PREVIOUS FUNCTIONS WITH THE SAME NAME
					//WE ARE IN ONE OF THEM SO DO NOT ADD TO CUMULATIVE TIME
					if (nameList.indexOf(name) == -1) {
						long cumulativeTime = aggregateTimeMap.get(name) - time;
						aggregateTimeMap.put(name, cumulativeTime);
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
						int parentID = idList.get(idList.size() - 2);
						outNeighbours.get(parentID).add(id);
					}
					
					callOrderList.add(id);
					lastFunctionCalled = id;

					break;
				case '>' :
					
					/*
					 * 
					 * Close tag -- Function return
					 * 
					 * 
					 */
					
					args = s.substring(1, s.length()).split(DELIM); //$NON-NLS-1$
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
					endingTimeInNS=Long.parseLong(args[1]);
					time = endingTimeInNS - timeMap.get(id);
					timeMap.put(id, time);
					if (id == firstNode)
						showTime(id, time);
					
					
					//IF AN ID IS IN THIS ARRAY IT IS BECAUSE WE NEED THE ENDING TIME
					// TO BE ADDED TO THE CUMULATIVE TIME FOR FUNCTIONS OF THIS NAME
					if (shouldGetEndingTimeForID.contains(id)){
						long cumulativeTime = aggregateTimeMap.get(name) + Long.parseLong(args[1]);
						aggregateTimeMap.put(name, cumulativeTime);
					}
					break;
				default : 
					/*
					 * 
					 * Anything else -- error
					 * 
					 */
					
//					parsingError(Messages.getString("StapGraphParser.14") + s.charAt(0) + //$NON-NLS-1$
//							Messages.getString("StapGraphParser.15") ); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				
			} 
			
		} 
		} catch (NumberFormatException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("StapGraphParser.22"),  //$NON-NLS-1$
					Messages.getString("StapGraphParser.23"), Messages.getString("StapGraphParser.24") + //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("StapGraphParser.25")); //$NON-NLS-1$
			mess.schedule();
			
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	

	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;

		BufferedReader buff = (BufferedReader) internalData;

		String line;
		boolean draw = false;
		try {
			while ((line = buff.readLine()) != null) {
				draw = true; 
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (line.length() < 1)
					continue;
				
				if (line.equals("PROBE_BEGIN")) { //$NON-NLS-1$
					String tmp = buff.readLine();
					
					if (tmp != null && tmp.length() > 0) {
						project = CoreModel.getDefault().getCModel().getCProject(tmp);
					}
					else {
						launchFileErrorDialog();
						return Status.CANCEL_STATUS;
					}
				} else if (line.charAt(0) == '-') {
					endingTimeInNS = Long.parseLong(line.substring(1));
				} else if (line.charAt(0) == '+') {
					totalTime = Long.parseLong(line.substring(1));
				} else if (line.charAt(0) == '?') {
					//Messages should be the last line in the output
					markedMessages = line.substring(1).split(";"); //$NON-NLS-1$
					parseEnd();
					parseMarked();
				} else {
					parse(line);
				}
			}
			if (draw)
				view.update();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

	/**
	 * Mark node id with a message giving its actual time.
	 */
	private void showTime(int id, long time) {
		String tmp = markedMap.get(id);
		if (tmp == null) tmp = "";
		markedMap.put(id, tmp + 
				"\nActual time: " + time/1000000 + "ms");
	}

}
