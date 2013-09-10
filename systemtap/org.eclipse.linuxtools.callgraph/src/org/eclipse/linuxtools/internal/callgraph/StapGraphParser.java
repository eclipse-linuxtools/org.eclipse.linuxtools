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
package org.eclipse.linuxtools.internal.callgraph;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.swt.widgets.Display;
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

	public  HashMap<Integer, Long> timeMap;
	public  TreeMap<Integer, String> serialMap;
	public  HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> neighbourMaps;
	public  HashMap<String, Long> aggregateTimeMap;
	public  HashMap<String, Integer> countMap;
	public  ArrayList<Integer> callOrderList;
	public  HashMap<Integer, String> markedMap;
	public String markedNodes;
	public int validator;
	public Long endingTimeInNS;
	public long totalTime;
	public  HashMap<Integer, Integer> lastFunctionMap;
	public ICProject project;
	private static final String DELIM = ",,"; //$NON-NLS-1$

	private boolean encounteredMain = false;
	private ArrayList<Integer> shouldGetEndingTimeForID = new ArrayList <Integer>();

	private  HashMap<Integer, ArrayList<String>> nameMaps;
	private  HashMap<Integer, ArrayList<Integer>> idMaps;
	private boolean skippedDirectives = false;
	private int firstNode = -1;

	public long startTime = -1;

	public String text;

	@Override
	protected void initialize() {
		//INITIALIZE MAPS
		neighbourMaps = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
		timeMap = new HashMap<Integer, Long>();
		serialMap = new TreeMap<Integer, String>();
		aggregateTimeMap = new HashMap<String, Long>();
		countMap = new HashMap<String, Integer>();
		endingTimeInNS = 0l;
		callOrderList = new ArrayList<Integer>();
		markedMap = new HashMap<Integer, String>();
		lastFunctionMap = new HashMap<Integer, Integer>();
		nameMaps = new HashMap<Integer, ArrayList<String>>();
		idMaps = new HashMap<Integer, ArrayList<Integer>>();
		project = null;
		startTime = -1;
	}


	@Override
	public IStatus nonRealTimeParsing(){
		//Clear maps (in case a previous execution left values hanging)
		neighbourMaps.clear();
		timeMap.clear();
		serialMap.clear();
		aggregateTimeMap.clear();
		countMap.clear();
		text = ""; //$NON-NLS-1$
		callOrderList.clear();
		shouldGetEndingTimeForID.clear();
		nameMaps.clear();
		idMaps.clear();
		encounteredMain = false;
		skippedDirectives = false;
		firstNode = -1;
		startTime = -1;

		BufferedReader buff = null;
		try {
			buff = new BufferedReader(new FileReader(sourcePath));
		} catch (FileNotFoundException e1) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run(){
					MessageDialog.openError(new Shell(), Messages.getString("StapGraphParser.FileNotFound"),  //$NON-NLS-1$
							Messages.getString("StapGraphParser.CouldNotOpen") + sourcePath); //$NON-NLS-1$

				}
			});
			return Status.CANCEL_STATUS;
		}
		internalData = buff;
		return realTimeParsing();
	}


	private void parseEnd() {


		//CHECK FOR EXIT() CALL
		for (int key : idMaps.keySet()) {
			ArrayList<Integer> idList = idMaps.get(key);
			int lastFunctionCalled = lastFunctionMap.get(key);
		if (idList.size() > 1) {
			for (int val : idList){
				String name = serialMap.get(val);
				long time =  endingTimeInNS - timeMap.get(val);
				timeMap.put(val, time);
				if (val == firstNode) {
					showTime(val, time);
				}
				if (shouldGetEndingTimeForID.contains(val)){
					long cumulativeTime = aggregateTimeMap.get(name) + endingTimeInNS;
					aggregateTimeMap.put(name, cumulativeTime);
				}

				lastFunctionCalled = val;
			}
			String tmp = markedMap.get(lastFunctionCalled);
			if (tmp == null) {
				tmp = ""; //$NON-NLS-1$
			}
			markedMap.put(lastFunctionCalled,
					tmp + "\n" + Messages.getString("StapGraphParser.Term")); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
			if (skippedDirectives) {
				markedMessage += Messages.getString("StapGraphParser.CDirectives"); //$NON-NLS-1$
			}
			if (timeCheck) {
				markedMessage += Messages.getString("StapGraphParser.TooFast"); //$NON-NLS-1$
			}

			markedMessage += Messages.getString("StapGraphParser.TimeForThisNode"); //$NON-NLS-1$

			markedMap.put(firstNode, markedMessage);
		}
	}

	private void parseMarked(String msg) {
		/*
		 * Append message
		 */
		String[] parsed = msg.split(",,", 2); //$NON-NLS-1$

		int key = Integer.parseInt(parsed[0]);

		ArrayList<Integer> idList = idMaps.get(key);
		if (idList == null || msg.length() < 1 || idList.size() < 1) {
			return;
		}
		int id = idList.get(idList.size() -1);
		if (parsed[1].equals("<unknown>")) { //$NON-NLS-1$
			parsed[1] = parsed[1] + Messages.getString("StapGraphParser.UnknownMarkers"); //$NON-NLS-1$
		}
		markedMap.put(id, (markedMap.get(id) == null ? "" : markedMap.get(id)) + parsed[1]); //$NON-NLS-1$
	}

	private IStatus parse(String s) {

		try {
		if (s.length() < 1) {
			return Status.OK_STATUS;
		}
		switch (s.charAt(0)) {
			case '<' :
				/*
				 *
				 * Open tag -- function call
				 *
				 *
				 */
				String[] args = s.substring(1, s.length()).split(DELIM);
				// args[0] = name
				// args[1] = id
				// arsg[2] = time of event
				int id = Integer.parseInt(args[1]);
				long time = Long.parseLong(args[2]);
				int tid = Integer.parseInt(args[3]);
				String name = args[0];

				//If we haven't encountered a main function yet and the name isn't clean,
				//and the name contains "__", then this is probably a C directive
				if (!encounteredMain && !isFunctionNameClean(name) && name.contains("__")) { //$NON-NLS-1$
					skippedDirectives = true;
					break;
				}

				ArrayList<String> nameList = nameMaps.get(tid);
				if (nameList == null) {
					nameList = new ArrayList<String>();
				}

				ArrayList<Integer> idList = idMaps.get(tid);
				if (idList == null) {
					idList = new ArrayList<Integer>();
				}

				HashMap<Integer, ArrayList<Integer>> outNeighbours = neighbourMaps.get(tid);
				if (outNeighbours == null) {
					outNeighbours = new HashMap<Integer, ArrayList<Integer>>();
				}

				if (startTime < 1) {
					startTime = time;
				}
				endingTimeInNS=time;

				name = cleanFunctionName(name);
				if (name.equals("main")) { //$NON-NLS-1$
					encounteredMain = true;
				}
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
				lastFunctionMap.put(tid,id);

				neighbourMaps.put(tid, outNeighbours);
				nameMaps.put(tid, nameList);
				idMaps.put(tid, idList);

				break;
			case '>' :

				/*
				 *
				 * Close tag -- Function return
				 *
				 */

				args = s.substring(1, s.length()).split(DELIM);
				//args[0] = name
				//args[1] = time of event
				name = args[0];
				tid = Integer.parseInt(args[2]);

				nameList = nameMaps.get(tid);
				if (nameList == null) {
					nameList = new ArrayList<String>();
				}

				idList = idMaps.get(tid);
				if (idList == null) {
					idList = new ArrayList<Integer>();
				}


				//If we haven't encountered a main function yet and the name isn't clean,
				//and the name contains "__", then this is probably a C directive
				if (!encounteredMain && !isFunctionNameClean(name) && name.contains("__")) { //$NON-NLS-1$
					skippedDirectives = true;
					break;
				}

				name = cleanFunctionName(name);
				int lastOccurance = nameList.lastIndexOf(name);
				if (lastOccurance < 0) {
					parsingError(Messages.getString("StapGraphParser.RetMismatch") + name); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				}

				nameList.remove(lastOccurance);
				id = idList.remove(lastOccurance);


				if (timeMap.get(id) == null) {
					parsingError(Messages.getString("StapGraphParser.NoStartTime") + name); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				}
				endingTimeInNS=Long.parseLong(args[1]);
				time = endingTimeInNS - timeMap.get(id);
				timeMap.put(id, time);
				if (id == firstNode) {
					showTime(id, time);
				}


				//IF AN ID IS IN THIS ARRAY IT IS BECAUSE WE NEED THE ENDING TIME
				// TO BE ADDED TO THE CUMULATIVE TIME FOR FUNCTIONS OF THIS NAME
				if (shouldGetEndingTimeForID.contains(id)){
					long cumulativeTime = aggregateTimeMap.get(name) + Long.parseLong(args[1]);
					aggregateTimeMap.put(name, cumulativeTime);
				}

				nameMaps.put(tid, nameList);
				idMaps.put(tid, idList);
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
		} catch (NumberFormatException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages
					(Messages.getString("StapGraphParser.BadSymbol"),  //$NON-NLS-1$
					Messages.getString("StapGraphParser.BadSymbol"),   //$NON-NLS-1$
					Messages.getString("StapGraphParser.BadSymbolMsg1") + //$NON-NLS-1$
					Messages.getString("StapGraphParser.BadSymbolMsg2")); //$NON-NLS-1$
			mess.schedule();

			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}


	private IStatus parseDotFile() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;

		BufferedReader buff = (BufferedReader) internalData;

		HashMap <Integer, ArrayList<Integer>> outNeighbours= new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<Integer> idList = new ArrayList<Integer>();
		endingTimeInNS =0l;
		totalTime=10000l;
		try {
			String line;
			while ((line = buff.readLine()) != null) {
				if (line.equals("}")) { //$NON-NLS-1$
					break;
				}
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if (line.length() < 1) {
					continue;
				}

				String[] args = new String[2];
				args = line.split(" ", 2); //$NON-NLS-1$
				if (args[0].contains("->")) { //$NON-NLS-1$
					//connection
					int[] ids = new int[2];
					int called = 1;
					try {
						ids[0] = Integer.parseInt(args[0].split("->")[0]); //$NON-NLS-1$
						ids[1] = Integer.parseInt(args[0].split("->")[1]); //$NON-NLS-1$
						int index1 = args[1].indexOf("=\""); //$NON-NLS-1$
						int index2 = args[1].indexOf("\"]"); //$NON-NLS-1$
						called = Integer.parseInt(args[1].substring(index1 + 2,index2));
					} catch (NumberFormatException e) {
						SystemTapUIErrorMessages m = new SystemTapUIErrorMessages(
								Messages.getString("StapGraphParser.idOrLabel"), Messages.getString("StapGraphParser.idOrLabel"),  //$NON-NLS-1$ //$NON-NLS-2$
								Messages.getString("StapGraphParser.nonNumericLabel")); //$NON-NLS-1$
						m.schedule();
						return Status.CANCEL_STATUS;
					}

					//Set neighbour
					ArrayList<Integer> tmpList = outNeighbours.get(ids[0]);
					if (tmpList == null) {
						tmpList = new ArrayList<Integer>();
					}

					for (int i = 0; i < called; i++) {
						tmpList.add(ids[1]);
					}

					outNeighbours.put(ids[0], tmpList);
				} else {
					//node
					try {
						int id = Integer.parseInt(args[0]);
						if (firstNode == -1) {
							firstNode = id;
						}
						int index = args[1].indexOf("=\""); //$NON-NLS-1$
						String name = args[1].substring(index + 2, args[1].indexOf(" ", index)); //$NON-NLS-1$
						double dtime = 0.0;
						dtime = Double.parseDouble(args[1].substring(args[1].indexOf(" ") + 1, args[1].indexOf("%"))); //$NON-NLS-1$ //$NON-NLS-2$
						long time = (long) (dtime*100);

						nameList.add(name);
						idList.add(id);
						timeMap.put(id, time);
						serialMap.put(id, name);
						if (countMap.get(name) == null){
							countMap.put(name, 0);
						}
						countMap.put(name, countMap.get(name) + 1);

						long cumulativeTime = (aggregateTimeMap.get(name) != null ? aggregateTimeMap.get(name) : 0) + time;
						aggregateTimeMap.put(name, cumulativeTime);
					} catch (NumberFormatException e) {
						SystemTapUIErrorMessages m = new SystemTapUIErrorMessages(
								Messages.getString("StapGraphParser.idOrTime"), Messages.getString("StapGraphParser.idOrTime"),  //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("StapGraphParser.nonNumericTime")); //$NON-NLS-1$
						m.schedule();
						return Status.CANCEL_STATUS;
					}

				}


			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buff.close();
			} catch (IOException e) {
				//Do nothing
			}
		}
		neighbourMaps.put(0, outNeighbours);
		nameMaps.put(0, nameList);
		idMaps.put(0, idList);
		try {
			view.update();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader)) {
			return Status.CANCEL_STATUS;
		}

		BufferedReader buff = (BufferedReader) internalData;

		String line;
		boolean draw = false;
		boolean first = true;
		try {
			while ((line = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if (line.length() < 1) {
					continue;
				}
				if (first && (line.contains(Messages.getString("StapGraphParser.17")))) { //$NON-NLS-1$
					return parseDotFile();
				}
				first = false;

				draw = true;
				if (line.equals("PROBE_BEGIN")) { //$NON-NLS-1$
					buff.mark(100);
					String tmp = buff.readLine();

					if (tmp != null && tmp.length() > 0) {
						char tchar = tmp.charAt(0);
						if (tchar != '-' && tchar != '+' && tchar != '?' && tchar != '>' && tchar != '<') {
							project = CoreModel.getDefault().getCModel().getCProject(tmp);
						} else {
							buff.reset();
						}
					}

				} else if (line.charAt(0) == '-') {
					endingTimeInNS = Long.parseLong(line.substring(1));
				} else if (line.charAt(0) == '+') {
					totalTime = Long.parseLong(line.substring(1));
					//Total time should be the last line in the output
					parseEnd();
				} else if (line.charAt(0) == '?') {
					if (line.length() > 1) {
						parseMarked(line.substring(1));
					}
				} else {
					if (parse(line) == Status.CANCEL_STATUS) {
						break;
					}
				}
			}
			if (draw && view != null) {
				view.update();
				//Repeat until all lines are read
				realTimeParsing();
			}
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
		if (tmp == null) {
			tmp = ""; //$NON-NLS-1$
		}
		markedMap.put(id, tmp +
				Messages.getString("StapGraphParser.ActualTime") + time/1000000  //$NON-NLS-1$
				+ Messages.getString("StapGraphParser.TimeUnits")); //$NON-NLS-1$
	}


}
