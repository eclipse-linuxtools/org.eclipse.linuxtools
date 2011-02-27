package org.eclipse.linuxtools.oprofile.core.opxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.linuxtools.oprofile.core.opxml.info.InfoAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** 
 * Caches the event data used by the CheckEventAdapter. The performance
 * improvement is targeted at the first time a call with the given arguments is
 * made. The first given call to check-event will take roughly O(n), and all
 * other calls whether they be new or recurring take O(1). Note that recurring
 * calls are handled by an entirely different cache. This particular class
 * simply parses the XML from ophelp -X and stores it. Since ophelp can only be
 * queried for the event name, and returns the event id, this cache saves time
 * for the reverse lookup.
 */
public class EventIdCache {
	
	private static final String HELP_EVENTS = "help_events"; //$NON-NLS-1$
	private static final String OPHELP = "ophelp"; //$NON-NLS-1$
	private static final String EVENT = "event"; //$NON-NLS-1$
	private static final String EVENT_NAME = "event_name"; //$NON-NLS-1$

	private Document eventDoc; // the document to hold the xml from ophelp
	private Element eventRoot; // the root corresponding to the xml from ophelp
	// id - the id of the event
	// Object [] - an array with 0th being the string name and 1st being DOM node
	private HashMap<Integer, Object[]> idMap;
	private HashMap<String, Integer> nameMap;
	private static EventIdCache single;
	
	public static EventIdCache getInstance(){
		if (single == null){
			single = new EventIdCache ();
		}
		return single;
	}
	
	/**
	 * @param id the id corresponding to an event
	 * @return the name of the event corresponding to the id, or null if none
	 * could be found.
	 */
	public String getEventNameWithID (int id){
		if (single.idMap == null){
			readXML();
			buildCache();
		}
		return single.idMap.get(id) != null ? (String)single.idMap.get(id)[0] : null;
	}
	
	public int getEventIDWithName(String name) {
		if (single.idMap == null){
			readXML();
			buildCache();
		}
		return single.nameMap.get(name);
	}
	
	/**
	 * @param id the id corresponding to an event
	 * @return the DOM Element corresponding to the event tag
	 */
	public Element getElementWithID (int id){
		if (single.idMap == null){
			readXML();
			buildCache();
		}
		return single.idMap.get(id) != null ? (Element)single.idMap.get(id)[1] : null;
	}

	/**
	 * Build the cache
	 */
	private void buildCache() {
		single.idMap = new HashMap<Integer, Object []> ();
		single.nameMap = new HashMap<String, Integer> ();
		Process p;
		NodeList eventList = single.eventRoot.getElementsByTagName(EVENT);
		for (int i = 0; i < eventList.getLength(); i++){
			Element elem = (Element) eventList.item(i);
			String eventName = elem.getAttribute(EVENT_NAME);
			try {
				p = Runtime.getRuntime().exec(OPHELP + " " + eventName);
				BufferedReader bi = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = bi.readLine();
				int val = Integer.parseInt(line);
				single.idMap.put(val, new Object [] {eventName, elem});
				single.nameMap.put(eventName, val);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read the XML from ophelp
	 */
	private void readXML() {
		try {
			Process p = Runtime.getRuntime().exec(OPHELP + " " + "-X");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				try {
					single.eventDoc = builder.parse(p.getInputStream());
					Element elem = (Element) single.eventDoc.getElementsByTagName(HELP_EVENTS).item(0);
					single.eventRoot = elem;
				} catch (IOException e) {
				} catch (SAXException e) {
				}
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param name the name of the event
	 * @return the type of unit mask. This can be either mandatory, exclusive,
	 * bitmask, or null if none could be found.
	 */
	public String getUnitMaskType(String name) {
		String unitMaskType = null;
		File file = new File(InfoAdapter.CPUTYPE);

		try {
			BufferedReader bi = new BufferedReader(new FileReader(file));
			String cpuType = bi.readLine();
			File opArchEvents = new File(InfoAdapter.OP_SHARE + cpuType + "/" + InfoAdapter.EVENTS); //$NON-NLS-1$
			File opArchUnitMasks = new File(InfoAdapter.OP_SHARE + cpuType + "/" + InfoAdapter.UNIT_MASKS); //$NON-NLS-1$
			
			BufferedReader eventReader = new BufferedReader(new FileReader(opArchEvents));
			String line;
			while ((line = eventReader.readLine()) != null){
				// find the line with the event name
				if (line.contains("name:"+name+" ")){ //$NON-NLS-1$
					int start = line.indexOf("um:") + 3; //$NON-NLS-1$
					int end = line.indexOf(" ", start); //$NON-NLS-1$
					// grab the string that references the unit mask type
					String um = line.substring(start, end);
					BufferedReader unitMaskReader = new BufferedReader(new FileReader(opArchUnitMasks));
					while ((line = unitMaskReader.readLine()) != null){
						if (line.contains("name:"+um+" ")){ //$NON-NLS-1$
							start = line.indexOf("type:") + 5; //$NON-NLS-1$
							end = line.indexOf(" ", start); //$NON-NLS-1$
							unitMaskType = line.substring(start, end);
							return unitMaskType;
						}
					}
				}
			}
			eventReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return unitMaskType;
	}
}
