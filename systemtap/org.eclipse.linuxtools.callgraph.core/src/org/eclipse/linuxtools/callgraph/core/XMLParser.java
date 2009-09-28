package org.eclipse.linuxtools.callgraph.core;

import java.util.ArrayList;
import java.util.HashMap;

public class XMLParser {
	
	private HashMap<Integer, HashMap<String,String>> keyValues;
	private ArrayList<Integer> idList;
	private int id;
	private int currentlyIn;
	private static String ATTR_NAME = "name"; 
	
	XMLParser() {
		id = 0;
		currentlyIn = 0;
		if (keyValues != null)
			keyValues.clear();
		keyValues = new HashMap<Integer,HashMap<String,String>>();
		
		if (idList != null)
			idList.clear();
		idList = new ArrayList<Integer>();
	}
	
	public void parse(String message) {
		String tabstrip = message.replaceAll("\t", "");
		String[] lines = tabstrip.split("\n");
		
		for (String line : lines) {
			if (line.length() < 1)
				continue;
				
			if (line.charAt(0) == '<') {
				//Either an open or close tag
				if (line.charAt(1) == '/') {
					//Closing tag -- assume properly formed
					idList.remove((Integer) currentlyIn);
					currentlyIn = -1;
					if (idList.size() > 0)
						currentlyIn = idList.get(idList.size()-1);

				} else if (line.substring(line.length()-2, line.length() - 1) == "/>") {
					//This tag opens and closes in one line
					id++;
					String[] tokens = line.split(" ");
					HashMap<String,String> map = new HashMap<String,String>();
					map.put(ATTR_NAME, tokens[0]);
					keyValues.put(id,map);
					
					addAttributes(tokens, 1);
					
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
					
					addAttributes(tokens, 1);
				}
			} else {
				//Attribute addition
				if (currentlyIn < 0 )
					continue;
				String[] tokens = line.split(" ");
				addAttributes(tokens, 0);
			}
		}
	}
	
	public static final String noName = "NoName";
	public void addAttributes(String[] tokens, int start) {
		HashMap<String,String> map = keyValues.get(currentlyIn);
		int nameless = 0;

		for (int j = start; j < tokens.length; j++) {
			String[] kvPair = tokens[j].split("=");
			if (kvPair.length < 2) {
				map.put(noName + nameless, kvPair[0].replaceAll(">", ""));
				nameless++;
				continue;
			}

			map.put(kvPair[0], kvPair[1].replaceAll(">", ""));
		}
		
		keyValues.put(currentlyIn, map);
	}
	
	public HashMap<Integer, HashMap<String,String>> getKeyValues() {
		return keyValues;
	}
	
}
