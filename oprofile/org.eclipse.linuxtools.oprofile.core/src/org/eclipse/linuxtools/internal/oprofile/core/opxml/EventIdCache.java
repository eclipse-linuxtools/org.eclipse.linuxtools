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
package org.eclipse.linuxtools.internal.oprofile.core.opxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
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

    private static final String LOCAL = "local"; //$NON-NLS-1$

    private Document eventDoc; // the document to hold the xml from ophelp
    private Element eventRoot; // the root corresponding to the xml from ophelp
    // name - the name of the event
    // Element - the DOM node
    private HashMap<String, Element> nameMap;

    // Map containing the caches for remote machines
    private static HashMap<String, EventIdCache> cacheMap;

    public static EventIdCache getInstance(){

        if (cacheMap == null) {
            cacheMap = new HashMap<>();
        }

        IProject project = Oprofile.OprofileProject.getProject();
        EventIdCache newCache = new EventIdCache();
        if (project != null) {
            EventIdCache eventIdCache = cacheMap.get(project.getLocationURI().getHost());
            if (eventIdCache == null) {
                cacheMap.put(project.getLocationURI().getHost(), newCache);
            } else {
                return eventIdCache;
            }
        } else {
            // If no project associated we should launch locally
            EventIdCache eventIdCache = cacheMap.get(LOCAL);
            if (eventIdCache == null) {
                cacheMap.put(LOCAL, newCache);
            } else {
                return eventIdCache;
            }
        }

        return newCache;
    }

    /**
     * @param id the id corresponding to an event
     * @return the DOM Element corresponding to the event tag
     */
    public Element getElementWithName (String name) {
        IProject project = Oprofile.OprofileProject.getProject();
        EventIdCache eventIdCache;
        if (project != null) {
            eventIdCache = cacheMap.get(project.getLocationURI().getHost());
        } else {
            eventIdCache = cacheMap.get(LOCAL);
        }

        if (eventIdCache.nameMap == null){
            readXML(eventIdCache);
            buildCache(eventIdCache);
        }
        return eventIdCache.nameMap.get(name) != null ? (Element)eventIdCache.nameMap.get(name) : null;

    }

    /**
     * Build the cache
     */
    private void buildCache(EventIdCache eventId) {
        eventId.nameMap = new HashMap<> ();
        NodeList eventList = eventId.eventRoot.getElementsByTagName(EVENT);
        for (int i = 0; i < eventList.getLength(); i++){
            Element elem = (Element) eventList.item(i);
            String eventName = elem.getAttribute(EVENT_NAME);
            eventId.nameMap.put(eventName, elem);
        }
    }

    /**
     * Read the XML from ophelp
     */
    private void readXML(EventIdCache eventId) {
        if (eventId.eventRoot != null) {
            return;
        }

        try {
            Process p = RuntimeProcessFactory.getFactory().exec(OPHELP + ' ' + "-X", Oprofile.OprofileProject.getProject()); //$NON-NLS-1$

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                try {
                    eventId.eventDoc = builder.parse(p.getInputStream());
                    Element elem = (Element) eventId.eventDoc.getElementsByTagName(HELP_EVENTS).item(0);
                    eventId.eventRoot = elem;
                } catch (IOException|SAXException e) {
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
        IProject project = Oprofile.OprofileProject.getProject();
        EventIdCache eventIdCache;
        if (project != null) {
            eventIdCache = cacheMap.get(project.getLocationURI().getHost());
        } else {
            eventIdCache = cacheMap.get(LOCAL);
        }

        if (eventIdCache.eventRoot == null){
            readXML(eventIdCache);
            buildCache(eventIdCache);
        }

        Element header = (Element)eventIdCache.eventRoot.getElementsByTagName(HEADER).item(0);

        double schemaVersion = 0;

        if (!eventIdCache.eventRoot.getAttribute(SCHEMA).isEmpty()){
            schemaVersion = Double.parseDouble(eventIdCache.eventRoot.getAttribute(SCHEMA));
        } else {
            schemaVersion = Double.parseDouble(header.getAttribute(SCHEMA));
        }

        String unitMaskType = null;
        IRemoteFileProxy proxy = null;
        try {
            proxy = RemoteProxyManager.getInstance().getFileProxy(Oprofile.OprofileProject.getProject());
        } catch (CoreException e) {
            e.printStackTrace();
        }

        // Schema Version > 1.0 has the unit mask type within the XML
        if (schemaVersion > 1.0){
            Element event = getElementWithName(name);
            Element unitMaskTag = (Element) event.getElementsByTagName(InfoAdapter.UNIT_MASKS).item(0);
            return unitMaskTag.getAttribute(CATEGORY);
        } else {
            IFileStore fileStore = proxy.getResource(InfoAdapter.CPUTYPE);
            try (InputStream fileInputStream = fileStore.openInputStream(
                    EFS.NONE, new NullProgressMonitor());
                    BufferedReader bi = new BufferedReader(
                            new InputStreamReader(fileInputStream))) {

                String cpuType = bi.readLine();
                IFileStore opArchEvents = proxy.getResource(InfoAdapter.OP_SHARE + cpuType + "/" + InfoAdapter.EVENTS); //$NON-NLS-1$
                IFileStore opArchUnitMasks = proxy.getResource(InfoAdapter.OP_SHARE + cpuType + "/" + InfoAdapter.UNIT_MASKS); //$NON-NLS-1$

                try (InputStream inputStreamEvents = opArchEvents.openInputStream(EFS.NONE, new NullProgressMonitor());
                        BufferedReader eventReader = new BufferedReader(new InputStreamReader(inputStreamEvents))) {
                    String line;
                    while ((line = eventReader.readLine()) != null){
                        // find the line with the event name
                        if (line.contains("name:"+name+' ')){ //$NON-NLS-1$
                            int start = line.indexOf("um:") + 3; //$NON-NLS-1$
                            int end = line.indexOf(' ', start);
                            // grab the string that references the unit mask type
                            String um = line.substring(start, end);
                            try (InputStream inputStreamMasks = opArchUnitMasks
                                    .openInputStream(EFS.NONE,
                                            new NullProgressMonitor());
                                    BufferedReader unitMaskReader = new BufferedReader(
                                            new InputStreamReader(inputStreamMasks))) {
                                while ((line = unitMaskReader.readLine()) != null) {
                                    if (line.contains("name:" + um + ' ')) { //$NON-NLS-1$
                                        start = line.indexOf("type:") + 5; //$NON-NLS-1$
                                        end = line.indexOf(' ', start);
                                        unitMaskType = line.substring(start, end);
                                        return unitMaskType;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException|CoreException e) {
            }
        }
        return unitMaskType;
    }

    /**
     * @since 3.0
     */
    public void setCacheDoc(Element oldRoot) {
        eventRoot = oldRoot;
    }
}
