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
package org.eclipse.linuxtools.systemtap.localgui.graphing;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.systemtap.localgui.core.MP;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;


/**
 * This class is used only in the case that we are rendering a graph
 * using GEF (specifically Zest).  
 *
 * After a stap command is sent to be executed, and after data is stored
 * into some temporary file, the data must be parsed to be used. This class
 * handles all of the parsing. All data is stored into Maps and this class
 * also starts the job responsible for taking the parsed data and rendering it.
 */
public class StapGraphParser extends Job{
	private IProgressMonitor monitor;
	public  HashMap<Integer, Long> timeMap;
	public  TreeMap<Integer, String> serialMap;
	public  HashMap<Integer, ArrayList<Integer>> outNeighbours;
	public  HashMap<String, Long> cumulativeTimeMap;
	public  HashMap<String, Integer> countMap;
	public  ArrayList<Integer> callOrderList;
	public  HashMap<Integer, String> markedMap;
	public String markedNodes;
	public int validator;
	private String filePath;
	public Long endingTimeInNS;
	public long totalTime;
	public int lastFunctionCalled;
	
	public StapGraphParser(String name, String filePath) {
		super(name);
		
		//BY DEFAULT READ/WRITE FROM HERE
		if (filePath != null)
			this.filePath = filePath;
		else
			filePath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
		
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
	}
	
	public void setFile(String filePath) {
		this.filePath = filePath;
	}
	
	
	public void launchFileDialogError(){
		SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(
				Messages.getString("StapGraphParser.0"), //$NON-NLS-1$
				Messages.getString("StapGraphParser.1"), //$NON-NLS-1$
				Messages.getString("StapGraphParser.2")+filePath+ //$NON-NLS-1$
				Messages.getString("StapGraphParser.3")); //$NON-NLS-1$ 
		err.schedule();
	}
	
	public String text;
	
	public IStatus executeParsing(){
		//Clear maps (in case a previous execution left values hanging)
		outNeighbours.clear();
		timeMap.clear();
		serialMap.clear();
		cumulativeTimeMap.clear();
		countMap.clear();
		text = "";
		callOrderList.clear();
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
			String tmp;
			while ((tmp = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				if (tmp.equals("PROBE_BEGIN")){ //$NON-NLS-1$
					text = buff.readLine();
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						endingTimeInNS = Long.parseLong(tmp);
					else {
						launchFileDialogError();
						return Status.CANCEL_STATUS;
					}
					tmp = buff.readLine();
					if (tmp != null && tmp.length() > 0)
						totalTime = Long.parseLong(tmp);
					else {
						launchFileDialogError();
						return Status.CANCEL_STATUS;
					}
				}
			}
			buff.close();
					
		} catch (IOException e) {
			launchFileDialogError();
			return Status.CANCEL_STATUS;
		}
		
		
		if (text.length() > 0) {
			
			boolean encounteredMain = false;
			
			ArrayList<Integer> shouldGetEndingTimeForID = new ArrayList <Integer>();
			String[] callsAndReturns = text.split(";");
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
						
						args = s.substring(1, s.length()).split(",,");
						// args[0] = name
						// args[1] = id
						// arsg[2] = time of event
						id = Integer.parseInt(args[1]);
						time = Long.parseLong(args[2]);
						name = args[0];
						
						//If we haven't encountered a main function yet and the name isn't clean,
						//and the name contains "__", then this is probably a C directive
						if (!encounteredMain && !isNameClean(name) && name.contains("__")) {
							skippedDirectives = true;
							break;
						}
						name = cleanName(name);
						if (name.equals("main"))
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
						args = s.substring(1, s.length()).split(",,");
						//args[0] = name
						//args[1] = time of event
						name = args[0];
						
						
						//If we haven't encountered a main function yet and the name isn't clean,
						//and the name contains "__", then this is probably a C directive
						if (!encounteredMain && !isNameClean(name) && name.contains("__")) {
							skippedDirectives = true;							
							break;
						}
						name = cleanName(name);
						int lastOccurance = nameList.lastIndexOf(name);
						if (lastOccurance < 0) {
							parsingError("Encountered return without matching call for function " + name);
							return Status.CANCEL_STATUS;
						}
						
						nameList.remove(lastOccurance);
						id = idList.remove(lastOccurance);
						
						
						if (timeMap.get(id) == null) {
							parsingError("No start time could be found for function " + name);
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
						parsingError("Unexpected symbol when parsing: '" + s.charAt(0) +
								"' encountered, while expecting < or >." );
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
				markedMap.put(lastFunctionCalled, ":::Program terminated here");
			}
				
			//timecheck is true if the total execution time is less than 10ms
			//and the first function is more than 1% off from the total time.
			boolean timeCheck = totalTime < 10000000 && 
								(((float)timeMap.get(firstNode)/totalTime) > 1.01 ||
								((float)timeMap.get(firstNode)/totalTime) < 0.99);
			
			
								
			if (skippedDirectives || timeCheck) {
				totalTime = timeMap.get(firstNode);
				String markedMessage = "";
				if (markedMap.containsKey(firstNode)) {
					markedMessage = markedMap.get(firstNode) + "\n";
				}
				if (skippedDirectives)
					markedMessage += "\n:::SystemTap detected functions that appeared to be C directives.";
				if (timeCheck)
					markedMessage += "\n:::Program terminated in less than 10ms and first function is not ~100%.";
				
				markedMessage += "\n:::Total time for this run has been set to the total time taken by this node.";
				
				markedMap.put(firstNode, markedMessage);
			}
			
			
			} catch (NumberFormatException e) {
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages("Unexpected Number", 
						"Unexpected symbol", "Unexpected symbol encountered while trying to " +
						"process id/time values.");
				mess.schedule();
				
				return Status.CANCEL_STATUS;
			}
		} else {
			parsingError("Could not find data in target file. Ensure target file contains data and try again.");
			return Status.CANCEL_STATUS;
		}
		
		//Create a UIJob to handle the rest
		GraphUIJob uijob = new GraphUIJob(Messages.getString("StapGraphParser.5"), this); //$NON-NLS-1$
		uijob.schedule(); 
		return Status.OK_STATUS;
			
	}
	
	/**
	 * Cleans names of form 'name").return', returning just the name
	 * @param name
	 */
	private String cleanName(String name) {
		return name.split("\"")[0];
		
	}
	
	private void parsingError(String message) {
		SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
				"ParseError", "Unexpected symbol", message);
		mess.schedule();
	}
	
 
	private boolean isNameClean(String name) {
		if (name.contains("\"") || name.contains(")"))
			return false;
		return true;
	}
	
	@SuppressWarnings("unused")
	private void printArrayListMap(HashMap<Integer, ArrayList<Integer>> blah) {
		int amt = 0;
		for (int a : blah.keySet()) {
			amt++;
			MP.print(a + " ::> "); //$NON-NLS-1$
			for (int c : blah.get(a)) {
					System.out.print(c + " ");					 //$NON-NLS-1$
			}
			MP.println("");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printMap(Map blah) {
		int amt = 0;
		for (Object a : blah.keySet()) {
			amt++;
			MP.println(a + " ::> "+blah.get(a)); //$NON-NLS-1$
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;
		return executeParsing();
		
	}
	
	
	public String getFile() {
		return filePath;
	}
	

	public IProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * For easier JUnit testing only. Allows public access to run method without scheduling an extra job.
	 *  
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return run(m);
	}
	
}
