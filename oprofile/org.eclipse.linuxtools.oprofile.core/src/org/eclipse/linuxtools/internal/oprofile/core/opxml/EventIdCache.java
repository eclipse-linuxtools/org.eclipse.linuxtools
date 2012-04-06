package org.eclipse.linuxtools.internal.oprofile.core.opxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
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
 * simply parses the XML from ophelp -X and stores it.
 */
public class EventIdCache {
	
	private static final String HELP_EVENTS = "help_events"; //$NON-NLS-1$
	private static final String HEADER = "header"; //$NON-NLS-1$
	private static final String SCHEMA = "schemaversion"; //$NON-NLS-1$
	private static final String CATEGORY = "category"; //$NON-NLS-1$
	private static final String OPHELP = "ophelp"; //$NON-NLS-1$
	private static final String EVENT = "event"; //$NON-NLS-1$
	private static final String EVENT_NAME = "event_name"; //$NON-NLS-1$

	private Document eventDoc; // the document to hold the xml from ophelp
	private Element eventRoot; // the root corresponding to the xml from ophelp
	// name - the name of the event
	// Element - the DOM node
	private HashMap<String, Element> nameMap;
	private static EventIdCache single;
	
	public static EventIdCache getInstance(){
		if (single == null){
			single = new EventIdCache ();
		}
		return single;
	}
	
	/**
	 * @param id the id corresponding to an event
	 * @return the DOM Element corresponding to the event tag
	 */
	public Element getElementWithName (String name) {
		if (single.nameMap == null){
			readXML();
			buildCache();
		}
		return single.nameMap.get(name) != null ? (Element)single.nameMap.get(name) : null;
	}

	/**
	 * Build the cache
	 */
	private void buildCache() {
		single.nameMap = new HashMap<String, Element> ();
		NodeList eventList = single.eventRoot.getElementsByTagName(EVENT);
		for (int i = 0; i < eventList.getLength(); i++){
			Element elem = (Element) eventList.item(i);
			String eventName = elem.getAttribute(EVENT_NAME);
			single.nameMap.put(eventName, elem);
		}
	}

	/**
	 * Read the XML from ophelp
	 */
	private void readXML() {
		try {
			Process p = RuntimeProcessFactory.getFactory().exec(OPHELP + " " + "-X", Oprofile.getCurrentProject());
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
	 * Get the unit mask type. Schema Version 1.1 and newer of ophelp XML
	 * will list the unit mask type as an attribute. Older version will not
	 * so we default to file lookups.
	 *
	 * @param name the name of the event
	 * @return the type of unit mask. This can be either mandatory, exclusive,
	 * bitmask, or null if none could be found.
	 */
	public String getUnitMaskType(String name) {
		if (single.eventRoot == null){
			readXML();
			buildCache();
		}

		Element header = (Element)single.eventRoot.getElementsByTagName(HEADER).item(0);

		double schemaVersion = 0;
		if (!single.eventRoot.getAttribute(SCHEMA).equals("")){
			schemaVersion = Double.parseDouble(single.eventRoot.getAttribute(SCHEMA));
		}else{
			schemaVersion = Double.parseDouble(header.getAttribute(SCHEMA));
		}

		String unitMaskType = null;

		// Schema Version > 1.0 has the unit mask type within the XML
		if (schemaVersion > 1.0){
			Element event = getElementWithName(name);
			Element unitMaskTag = (Element) event.getElementsByTagName(InfoAdapter.UNIT_MASKS).item(0);
			return unitMaskTag.getAttribute(CATEGORY);
		}else{
			File file = new File(InfoAdapter.CPUTYPE);
			BufferedReader bi = null;
			try {
				bi = new BufferedReader(new FileReader(file));
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
						BufferedReader unitMaskReader = null;
						try {
							unitMaskReader = new BufferedReader(new FileReader(
									opArchUnitMasks));
							while ((line = unitMaskReader.readLine()) != null) {
								if (line.contains("name:" + um + " ")) { //$NON-NLS-1$
									start = line.indexOf("type:") + 5; //$NON-NLS-1$
									end = line.indexOf(" ", start); //$NON-NLS-1$
									unitMaskType = line.substring(start, end);
									return unitMaskType;
								}
							}
						} finally {
							if (unitMaskReader != null) {
								unitMaskReader.close();
							}
						}
					}
				}
				eventReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bi != null) {
					try {
						bi.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return unitMaskType;
	}
}
