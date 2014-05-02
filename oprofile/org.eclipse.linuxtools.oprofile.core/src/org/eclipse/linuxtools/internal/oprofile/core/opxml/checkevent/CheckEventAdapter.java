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
package org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent;

import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.AbstractDataAdapter;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.EventIdCache;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.InfoAdapter;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class takes the XML that is output from various checks for and uses
 * that data to modify it into the format expected by the SAX parser.
 */
public class CheckEventAdapter extends AbstractDataAdapter {

    public static final String CHECK_EVENTS = "check-events"; //$NON-NLS-1$
    public static final String RESULT = "result"; //$NON-NLS-1$
    public static final String UNIT_MASKS = "unit_masks"; //$NON-NLS-1$
    public static final String UNIT_MASK = "unit_mask"; //$NON-NLS-1$
    public static final String MASK = "mask"; //$NON-NLS-1$

    private Element event; // the element corresponding to the event id
    private String eventName; // the id corresponding to the event
    private String unitMask; // the unit mask for the event
    private String cpuCounter; // the cpu counter used for profiling
    private Document resultDoc; // the document to hold the generated xml
    private String returnCode; // the return code to be used in the generated xml

    public CheckEventAdapter(String ctr, String event, String umask) {
        cpuCounter = ctr;
        eventName = event;
        unitMask = umask;

        this.event = EventIdCache.getInstance().getElementWithName(eventName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            resultDoc = builder.newDocument();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void process() {
        setReturnCode();
        createXML();
    }

    private void setReturnCode() {
        if (! isValidCounter()){
            returnCode = "invalid-counter"; //$NON-NLS-1$
            return;
        }

        if (! isValidUnitMask()){
            returnCode = "invalid-um"; //$NON-NLS-1$
            return;
        }

        returnCode = "ok"; //$NON-NLS-1
    }

    /**
     * Check if the unit mask being used is acceptable for this event
     * @return true if the unit mask is recognized by the event and false
     * otherwise.
     */
    private boolean isValidUnitMask() {
        TreeSet<Integer> bitMaskSet = new TreeSet<> ();
        Element unitMasksTag = (Element) event.getElementsByTagName(UNIT_MASKS).item(0);

        if (unitMasksTag == null){
            return true;
        }

        NodeList unitMasksList = unitMasksTag.getElementsByTagName(UNIT_MASK);

        // type:exclusive unit mask support
        for (int i = 0; i < unitMasksList.getLength(); i++){
            Element unitMaskElem = (Element) unitMasksList.item(i);
            String val = unitMaskElem.getAttribute(MASK);
            if (val.equals(unitMask)){
                return true;
            }
            bitMaskSet.add(Integer.parseInt(val));
        }

        // type:bitmask unit mask support
        String unitMaskType = EventIdCache.getInstance().getUnitMaskType(eventName);
        if ("bitmask".equals(unitMaskType)){ //$NON-NLS-1$
            int tmpVal = Integer.parseInt(unitMask);
            int count = 0;
            while (tmpVal != 0){
                if (tmpVal % 2 != 0 && ! bitMaskSet.contains((int)Math.pow(2, count))){
                    return false;
                }
                tmpVal = tmpVal / 2;
                count ++;
            }
            return true;
        }

        return false;
    }

    /**
     * Check if /dev/oprofile/N exists where N is the counter selected
     * @return true if /dev/oprofile/N exists, and false otherwise.
     */
    private boolean isValidCounter() {
        /*
         * TODO: Originally the number of counters for a given arch were
         * hard-coded in a list. This method may not be entirely correct,
         * although much simpler.
         */
        if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPCONTROL_BINARY)) {
            IRemoteFileProxy proxy = null;
            try {
                proxy = RemoteProxyManager.getInstance().getFileProxy(Oprofile.OprofileProject.getProject());
                IFileStore fileStore = proxy.getResource(InfoAdapter.DEV_OPROFILE + cpuCounter);
                if (! fileStore.fetchInfo().exists()){
                    return false;
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void createXML() {
        Element resultRoot = resultDoc.createElement(CHECK_EVENTS);
        Element resultTag = resultDoc.createElement(RESULT);
        resultTag.setTextContent(returnCode);
        resultRoot.appendChild(resultTag);
        resultDoc.appendChild(resultRoot);
    }

    @Override
    public Document getDocument() {
        return resultDoc;
    }

}
