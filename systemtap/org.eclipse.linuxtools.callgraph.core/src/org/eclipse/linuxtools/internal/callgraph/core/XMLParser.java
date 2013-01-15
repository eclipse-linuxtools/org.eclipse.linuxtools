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
package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class XMLParser {
	
	private HashMap<Integer, HashMap<String,String>> keyValues;
	private ArrayList<Integer> idList;
	private int id;
	private int currentlyIn;
	private static final String ATTR_NAME = "name";
	private static final String ATTR_TEXT = "text"; 
	public static final String noName = "NoName";
	private boolean textMode;
	XMLParser() {
		id = 0;
		currentlyIn = 0;
		if (keyValues != null) {
			keyValues.clear();
		}
		keyValues = new HashMap<Integer,HashMap<String,String>>();
		
		if (idList != null) {
			idList.clear();
		}
		idList = new ArrayList<Integer>();
		
		textMode = false;
	}
	
	/**
	 * Helper method to call parse on the contents of a file.
	 * @param file
	 */
	public void parse(File file) {
		parse(getContents(file));
	}
	
	
	/**
	 * Parses a String according to XML formatting rules. Will return a HashMap indexed by an 
	 * identification number, where each value is a further HashMap of String, String pairs representing
	 * attributes.
	 * @param message
	 */
	public void parse(String message) {
		String tabstrip = message.replaceAll("\t", "");
		String[] lines = tabstrip.split("\n");
		
		for (String line : lines) {
			if (line.length() < 1) {
				continue;
			}
				
			if (line.charAt(0) == '<') {
				//Either an open or close tag
				if (line.charAt(1) == '/') {
					//Closing tag -- assume properly formed
					idList.remove((Integer) currentlyIn);
					currentlyIn = -1;
					if (idList.size() > 0) {
						currentlyIn = idList.get(idList.size()-1);
					}
					setTextMode(true);

				} else if (line.substring(line.length()-2, line.length() - 1).equals("/>")) {
					//This tag opens and closes in one line
					id++;
					String[] tokens = line.split(" ");
					HashMap<String,String> map = new HashMap<String,String>();
					map.put(ATTR_NAME, tokens[0]);
					keyValues.put(id,map);
					textMode = false;
					addAttributes(currentlyIn, tokens, 1);
					
				} else {
					//Open tag
					idList.add(id);
					id++;
					currentlyIn = id;
					
					String[] tokens = line.split(" ");

					//Add name variable
					HashMap<String,String> map = new HashMap<String,String>();
					map.put(ATTR_NAME, tokens[0]);
					keyValues.put(id,map);
					
					addAttributes(currentlyIn, tokens, 1);
				}
			} else {
				//Attribute addition
				if (currentlyIn < 0 ) {
					continue;
				}
				
				if (textMode) {
					HashMap<String,String> map = keyValues.get(currentlyIn);
					map.put(ATTR_TEXT, line);
				}
				
				String[] tokens = line.split(" ");
				addAttributes(currentlyIn, tokens, 0);
			}
		}
	}
	
	/**
	 * Turns an array of Strings of form attribute=value into attribute/value pairings for the specified node.
	 * Starts looking at token number start.
	 * @param tokens
	 * @param start
	 */
	public void addAttributes(int id, String[] tokens, int start) {
		HashMap<String,String> map = keyValues.get(id);
		int nameless = 0;
		

		for (int j = start; j < tokens.length; j++) {
			String[] kvPair = tokens[j].split("=");
			String value = "";
			String key = "";
			if (kvPair.length < 1) {
				continue;
			}
			
			if (kvPair.length < 2) {
				value = kvPair[0];
				if (value.charAt(value.length() - 1) == '>') {
					
					setTextMode(true);
					value = value.substring(0, value.length()-1);
				}
				map.put(noName + nameless, value);
				nameless++;
				continue;
			}
			
			value = kvPair[0];
			key = kvPair[1];
			if (value.charAt(value.length() - 1) == '>') {
				setTextMode(true);
				value = value.substring(0, value.length()-1);
			}
			
			map.put(key, value);
		}
		
		keyValues.put(id, map);
	}
	
	public HashMap<Integer, HashMap<String,String>> getKeyValues() {
		return keyValues;
	}
	
	

	  static public String getContents(File file) {
		    StringBuilder contents = new StringBuilder();
		    
		    try {
		      BufferedReader input =  new BufferedReader(new FileReader(file));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		          contents.append(line);
		          contents.append("\n");
		        }
		      } finally {
		        input.close();
		      }
		    } catch (IOException ex){
		      ex.printStackTrace();
		    }
		    
		    return contents.toString();
		  }

	  public void setTextMode(boolean val) {
		  textMode = val;
	  }
}
