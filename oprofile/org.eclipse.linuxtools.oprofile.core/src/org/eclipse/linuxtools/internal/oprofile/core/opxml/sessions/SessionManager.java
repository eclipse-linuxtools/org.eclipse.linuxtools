/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class manipulates the XML data output from the command 'opxml sessions'
 */
public class SessionManager {
	public final static String PLUGIN_LOC = OprofileCorePlugin.getDefault()
            .getStateLocation().toOSString();
	public final static String SESSIONS = "sessions"; //$NON-NLS-1$
	public final static String EVENT = "event"; //$NON-NLS-1$
	public final static String SESSION = "session"; //$NON-NLS-1$
	public final static String NAME = "name"; //$NON-NLS-1$
	public final static String CURRENT = "current"; //$NON-NLS-1$
	public final static String OPXML_PREFIX = PLUGIN_LOC + "/opxml_"; //$NON-NLS-1$
	public final static String SESSION_LOCATION = OPXML_PREFIX + SESSIONS;
	public final static String MODEL_DATA = "model-data"; //$NON-NLS-1$

    public Document doc;
    public Element root;
    public String absfilePath;

    public SessionManager(String filePath) {
        this(new File(filePath));
        absfilePath = filePath;
        write();
    }

    /**
     * Session manager class constructor to manipulate the XML data
     *
     * @param file
     *            the xml file
     */
    public SessionManager(File file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            if (!file.exists()) {
                file.createNewFile();
                doc = builder.newDocument();
                root = doc.createElement(SESSIONS);
                doc.appendChild(root);
            } else {
                InputStream is = new FileInputStream(file);
                doc = builder.parse(is);
                Element elem = (Element) doc.getElementsByTagName(SESSIONS)
                        .item(0);
                root = elem;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a session to the specified event element if it does not exist.
     *
     * @param sessionName
     *            the name of the session
     * @param eventName
     *            the name of the event
     */
    public void addSession(String sessionName, String eventName) {
        Element event = null;
        Element session = find(root, SESSION, NAME, sessionName);
        if (session == null) {
            session = doc.createElement(SESSION);
            session.setAttribute(NAME, sessionName);
            root.appendChild(session);
        } else {
            event = find(session, EVENT, NAME, eventName);
        }

        if (event == null) {
            event = doc.createElement(EVENT);
            event.setAttribute(NAME, eventName);
            session.appendChild(event);
        }
    }

    /**
     * Check if a session exists
     *
     * @param sessionName
     *            the name of the session
     * @return true if the session exists, otherwise false
     */
    public boolean existsSession(String sessionName) {
        return find(root, SESSION, NAME, sessionName) != null ? true : false;
    }

    /**
     * Find an element in the XML
     *
     * @param elem
     *            the element to look under
     * @param tagName
     *            the name of the tag
     * @param attName
     *            the name of the attribute
     * @param attVal
     *            the name of the attribute value
     * @return the element that matches the search, or null if none is found.
     */
    private Element find(Element elem, String tagName, String attName,
            String attVal) {
        NodeList list = elem.getElementsByTagName(tagName);
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Element) {
                Element e = (Element) list.item(i);
                if (e.getAttribute(attName).equals(attVal)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Remove all sessions under any event that have the name 'current'
     */
    public void removeAllCurrentSessions() {
        NodeList sessionList = root.getElementsByTagName(SESSION);
        for (int i = 0; i < sessionList.getLength(); i++) {
            Element session = (Element) sessionList.item(i);
            String sessionName = session.getAttribute(NAME);
            if (CURRENT.equals(sessionName)) {
                NodeList eventList = session.getElementsByTagName(EVENT);
                for (int j = 0; j < eventList.getLength(); j++) {
                    Element event = (Element) eventList.item(j);
                    String eventName = event.getAttribute(NAME);
                    session.removeChild(event);
                    File file = new File(SessionManager.OPXML_PREFIX
                            + SessionManager.MODEL_DATA + eventName
                            + SessionManager.CURRENT);
                    file.delete();
                    if (sessionList.getLength() == 0) {
                        root.removeChild(session);
                    }
                }
            }
        }
    }

    /**
     * Remove a session named sessionName that is under eventName if it exists.
     *
     * @param sessionName
     * @param eventName
     */
    public void removeSession(String sessionName, String eventName) {
        NodeList list = root.getElementsByTagName(SESSION);
        for (int i = 0; i < list.getLength(); i++) {

            if (list.item(i) instanceof Element) {
                Element session = (Element) list.item(i);
                if (session.getAttribute(NAME).equals(sessionName)) {
                    Element event = find((Element) list.item(i), EVENT, NAME,
                            eventName);
                    if (event != null) {
                        removeSessionHelper(sessionName, event);
                    }
                }
            }
        }
    }

    /**
     * remove event elem for given session sessionName , also remove session if
     * there is no event under it.
     *
     * @param sessionName
     * @param elem
     *
     */
    private void removeSessionHelper(String sessionName, Element elem) {
        NodeList list = root.getElementsByTagName(SESSION);
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Element) {
                Element e = (Element) list.item(i);
                if (e.getAttribute(NAME).equals(sessionName)) {
                    NodeList events = e.getElementsByTagName(EVENT);
                    for (int j = 0; j < events.getLength(); j++) {
                        Element event = (Element) events.item(j);
                        if (event.getAttribute(NAME).equals(
                                elem.getAttribute(NAME))) {
                            e.removeChild(event);
                        }
                        if (events.getLength() == 0) {
                            root.removeChild(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Return a list of the events run with the given session
     *
     * @param sessionName
     *            the name of the session
     * @return A String ArrayList of event names that were run with the given
     *         session.
     */
    public ArrayList<String> getSessionEvents(String sessionName) {
        ArrayList<String> ret = new ArrayList<>();
        NodeList eventList = root.getElementsByTagName(SESSION);

        for (int i = 0; i < eventList.getLength(); i++) {
            if (eventList.item(i) instanceof Element) {
                Element event = ((Element) eventList.item(i));
                if (event.getAttribute(NAME).equals(sessionName)) {

                    NodeList sessionList = event.getElementsByTagName(EVENT);
                    for (int j = 0; j < sessionList.getLength(); j++) {
                        if (sessionList.item(j) instanceof Element) {
                            Element session = ((Element) sessionList.item(j));
                            ret.add(session.getAttribute(NAME));
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Write back to the same file, that the data was loaded from.
     */
    public void write() {
        writeToFile(absfilePath);
    }

    /**
     * Write the contents of the given Document to a file.
     */
    private void writeToFile(String filePath) {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(new File(filePath));
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer xformer;
        try {
            xformer = factory.newTransformer();
            xformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
