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
	//public  HashMap<Integer, String> markedMap;
//	public String graphText;
//	public String timeInfo;
//	public String cumulativeTimeInfo;
//	public String serialInfo;
	//public String markedNodes;
	public int validator;
	private String filePath;
	public Long endingTimeInNS;
	public long totalTime;
	
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
		//markedMap = new HashMap<Integer, String>();
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
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
			String tmp;
			while ((tmp = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				if (tmp.equals("PROBE_BEGIN")){ //$NON-NLS-1$
					text = buff.readLine();
					endingTimeInNS = Long.parseLong(buff.readLine());
					totalTime = Long.parseLong(buff.readLine());
				}
			}
			buff.close();
					
		} catch (IOException e) {
			launchFileDialogError();
			return Status.CANCEL_STATUS;
		}
		
		
		if (text.length() > 0) {
			
			ArrayList<Integer> shouldGetEndingTimeForID = new ArrayList <Integer>();
			String[] callsAndReturns = text.split(";");
			String[] args;
			ArrayList<String> nameList = new ArrayList<String>();
			ArrayList<Integer> idList = new ArrayList<Integer>();
			String name;
			int id;
			long time;
			long cumulativeTime;
			int parentID;
			
			for (String s : callsAndReturns) {
				switch (s.charAt(0)) {
					case '<' :
						
						args = s.substring(1, s.length()).split(":");
						// args[0] = name
						// args[1] = id
						// arsg[2] = time of event
						id = Integer.parseInt(args[1]);
						time = Long.parseLong(args[2]);
						name = args[0];
						if (!isNameClean(name))
							break;
//						name = cleanName(name);
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
						break;
					case '>' :
						//args[0] = name
						//args[1] = time of event
						args = s.substring(1, s.length()).split(":");
						name = args[0];
						if (!isNameClean(name))
							break;
//						name = cleanName(name);
						int lastOccurance = nameList.lastIndexOf(name);
						if (lastOccurance < 0) {
							parsingError();
							return Status.CANCEL_STATUS;
						}
						
						nameList.remove(lastOccurance);
						id = idList.remove(lastOccurance);
						
						//Get the last function that was called but never returned
						if (idList.size() > 0) {
							parentID = idList.get(idList.size() - 1);
							outNeighbours.get(parentID).add(id);
						}
						
						if (timeMap.get(id) == null) {
							parsingError();
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
						parsingError();
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
				}
			}
			
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
	@SuppressWarnings("unused")
	private String cleanName(String name) {
		return name.split("\"")[0];
		
	}
	
	private void parsingError() {
		SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
				"ParseError", "Unexpected symbol", "Unexpected symbol when parsing.");
		mess.schedule();
	}
	
/*	public void calculateAggregateStats(){
		//CALCULATE COUNTMAP
		for (String funcName :  serialMap.values()){
			if (countMap.get(funcName) == null){
				countMap.put(funcName, 1);
			}else{
				countMap.put(funcName, countMap.get(funcName)+1);
			}
		}
	}*/
	
	
/*	public IStatus generateMaps(){
			
			createMap(outNeighbours);
			
			if (generalParser(timeMap, timeInfo, "il") == Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			if (generalParser(serialMap, serialInfo, "is")== Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			if (generalParser(cumulativeTimeMap, cumulativeTimeInfo, "sl")== Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			calculateAggregateStats();
			
//			if (generalParser(markedMap, markedNodes, "is")== Status.CANCEL_STATUS) //$NON-NLS-1$
//				return Status.CANCEL_STATUS;

			return Status.OK_STATUS;

	}*/
	
	
	/**
	 * Will parse string data of form :
	 * "a_1:b_1;a_2:b_2;a_3:b_3; ... a_n:b_n;"
	 * where all a_i, are of type (int || long || string)
	 * and all b_i as well.
	 * 
	 * @param map a map that will contain the parsed data
	 * @param text one line of data to be parsed
	 * @param mode 'typeof key''type of value', 'i'=int, 's'=string, 'l'=long
	 * @return IStatus indicating whether the action was cancelled or not
	 */
/*	@SuppressWarnings("unchecked")
	private IStatus generalParser(Map map, String text, String mode){
		String curr_serial = ""; //$NON-NLS-1$
		int pos = 0;
		String tmp_val = ""; //$NON-NLS-1$
		
		while (pos < text.length()){
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			//Read serial
			
			while (text.charAt(pos) != ':'){
				curr_serial += text.charAt(pos);
				pos++;
			}
			
			pos++;
			
			//Read serial data
			while (text.charAt(pos) != ';'){
				tmp_val +=  text.charAt(pos);
				pos++;
			}
			
			if (mode.equals("il")){ //$NON-NLS-1$
				map.put(Integer.valueOf(curr_serial),Long.valueOf(tmp_val));				
			}else if (mode.equals("is")){ //$NON-NLS-1$
				map.put(Integer.valueOf(curr_serial),tmp_val);
			}else if(mode.equals("si")){ //$NON-NLS-1$
				map.put(curr_serial,Integer.valueOf(tmp_val));
			}else if (mode.equals("sl")){ //$NON-NLS-1$
				map.put(curr_serial,Long.valueOf(tmp_val));
			}else{				
				map.put(curr_serial,tmp_val);
			}
			
			pos++;
			curr_serial = ""; //$NON-NLS-1$
			
			tmp_val = ""; //$NON-NLS-1$
		}
		
		return Status.OK_STATUS;
	}*/
	
	
	/**
	 * Will parse string data of form :
	 * "0<1<2<3<>>>4<>5<>>"
	 * which can be thought of as an S-Expression. This can be represented
	 * as a Tree data structure. The Map stores a node as a key and a List
	 * of its immediate children as values. If a node has no children
	 * (ie. a leaf), it has a child of '-1'.
	 * 
	 * This method is used only to get the call structure of a 
	 * 
	 * @param map map a map that will contain the parsed data
	 */
/*	private void createMap(HashMap<Integer,ArrayList<Integer>> map){
		String str_parent = ""; //$NON-NLS-1$
		int parent = 0;
		char val;
		for (int i = 0; i < graphText.length(); i++){
			val = graphText.charAt(i);
			if (val == '<'){
				parent = Integer.valueOf(str_parent);
				if (graphText.charAt(i+1) != '>'){
					//FUNCTION HAS INNER CHILDREN
					if (map.get(parent) == null){
						map.put(parent, new ArrayList<Integer>());
					}
					//GET THE DIRECT INNER CHILDREN
					map.put(parent, getChildren(i));
				}else{
					//FUNCTION HAS NO INNER CHILDREN
					if (map.get(parent) == null){
						map.put(parent, new ArrayList<Integer>());
					}
					map.get(parent).add(-1);
				}
				str_parent = ""; //$NON-NLS-1$
			//TEXT CHARACTER
			}else if (val != '>'){
				str_parent += val;
			}
		}
		
	}*/
	
	/**
	 * Helper method for createMap, to get the children of some node.
	 * The case of no children is handled by createMap.
	 * 
	 * Eg. Given, "0<1<2<>3<>>>", to get the children of node 0, we would 
	 * call : getChildren(1), and would get a list with just a value of 1.
	 * To get the children of node 1, we would call : getChildren(3), and
	 * we would get a list with 2, and 3.
	 * 
	 * @param pos position of the '<' that is to the immediate left of the
	 * first child
	 * @return A List of the children
	 */
	/*private ArrayList<Integer> getChildren(int pos){
		int nested = 0;
		String func = ""; //$NON-NLS-1$
		ArrayList<Integer> ret = new ArrayList<Integer>();
		char val;
		
		while(pos <  graphText.length()){
			val = graphText.charAt(pos);
			//WE ARE ENTERING A NEW FUNCTION SO SAVE THE CURRENT ONE
			if (val == '<'){
				if (func.length() > 0){
					//GRAB A UNIQE IDENTIFIER FOR THE FUNCTION
					ret.add(Integer.valueOf(func));															
				}
				func = ""; //$NON-NLS-1$
				nested++;
			}else if (val == '>'){
				//IF WE ARE CLOSING THIS FUNCTION STOP LOOKING
				if (nested == 1){
					return ret;			
				}
				nested--;
			}else{
				//IF WE ARE DIRECTLY IN THE FUNCTION COLLECT THIS STRING
				if (nested == 1){
					func += val;
				}
			}
			pos++;
		}
		
		return ret;
	}*/
 
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
