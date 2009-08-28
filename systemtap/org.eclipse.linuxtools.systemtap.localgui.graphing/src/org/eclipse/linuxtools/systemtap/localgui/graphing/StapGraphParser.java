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

public class StapGraphParser extends Job{
	//TODO: Test that cancelling works properly
	
	private IProgressMonitor monitor;
	public  HashMap<Integer, Long> timeMap;
	public  TreeMap<Integer, String> serialMap;
	public  HashMap<Integer, ArrayList<Integer>> outNeighbours;
	public  HashMap<String, Long> cumulativeTimeMap;
	public  HashMap<String, Integer> countMap;
	public  HashMap<Integer, String> markedMap;
	public String graphText;
	public String timeInfo;
	public String cumulativeTimeInfo;
	public String serialInfo;
	public String markedNodes;
	public int validator;
	private String filePath;
	private boolean isValidFile;
	
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
		markedMap = new HashMap<Integer, String>();
	}
	
	public void setFile(String filePath) {
		this.filePath = filePath;
	}
	
	
	public void launchFileDialogError(){
		SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(
				Messages.getString("StapGraphParser.0"), //$NON-NLS-1$
				Messages.getString("StapGraphParser.1"), //$NON-NLS-1$
		Messages.getString("StapGraphParser.2")+filePath+Messages.getString("StapGraphParser.3")); //$NON-NLS-1$ //$NON-NLS-2$
		err.schedule();
	}
	
	public IStatus executeParsing(){
		//Clear maps (in case a previous execution left values hanging)
		isValidFile = false;
		outNeighbours.clear();
		timeMap.clear();
		serialMap.clear();
		cumulativeTimeMap.clear();
		countMap.clear();
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(filePath));
			String tmp;
			while ((tmp = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				if (tmp.equals("PROBE_BEGIN")){ //$NON-NLS-1$
					isValidFile = true;
					graphText = buff.readLine();
					serialInfo = buff.readLine();
					timeInfo = buff.readLine();
					cumulativeTimeInfo = buff.readLine();
					markedNodes = buff.readLine();
				}
			}
			buff.close();
					
		} catch (IOException e) {
			launchFileDialogError();
			return Status.CANCEL_STATUS;
		}
		
		if (isValidFile){			
			for (int i = 0; i < graphText.length(); i++){
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				//Check for valid nesting
				if (graphText.charAt(i) == '<'){
					validator++;
				}else if(graphText.charAt(i) == '>'){
					validator--;
				}
			}
			
			//If brackets don't match, check for potential exit call
			if (validator != 0){	
				while (validator >= 0){
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					graphText+='>';
					validator--;
				}
			}
			
			if (timeInfo == null)
				return Status.CANCEL_STATUS;
			
			//Generate maps
			this.generateMaps();
			
			//Create a UIJob to handle the rest
			GraphUIJob uijob = new GraphUIJob(Messages.getString("StapGraphParser.5"), this); //$NON-NLS-1$
			uijob.schedule(); 
		}else{
			launchFileDialogError();
			return Status.CANCEL_STATUS;
		}

		
		return Status.OK_STATUS;
	}
	
	
	public void calculateAggregateStats(){
		//CALCULATE COUNTMAP
		for (String funcName :  serialMap.values()){
			if (countMap.get(funcName) == null){
				countMap.put(funcName, 1);
			}else{
				countMap.put(funcName, countMap.get(funcName)+1);
			}
		}
	}
	
	
	public IStatus generateMaps(){
			
			createMap(outNeighbours);
			
			if (generalParser(timeMap, timeInfo, "il") == Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			if (generalParser(serialMap, serialInfo, "is")== Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			if (generalParser(cumulativeTimeMap, cumulativeTimeInfo, "sl")== Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			
			calculateAggregateStats();
			
			if (generalParser(markedMap, markedNodes, "is")== Status.CANCEL_STATUS) //$NON-NLS-1$
				return Status.CANCEL_STATUS;

			return Status.OK_STATUS;


	}
	
	
	//TODO : 'pass by value' nightmare if map or text are sufficiently large
	@SuppressWarnings("unchecked")
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
	}
	
	
	//TODO : increase efficiency by using StringBuffer only if too slow
	private void createMap(HashMap<Integer,ArrayList<Integer>> map){
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
		
	}
	
	private ArrayList<Integer> getChildren(int pos){
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
