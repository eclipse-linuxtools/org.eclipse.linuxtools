/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml;

import java.util.HashMap;
import java.util.Stack;

import org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent.CheckEventsProcessor;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.info.OpInfoProcessor;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata.ModelDataProcessor;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionsProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The SAX handler class that is used to parse the output of opxml.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class OprofileSAXHandler extends DefaultHandler {
    // The only allowed instance of this class
    private static OprofileSAXHandler instance = null;

    // A Map of all the XML processors for opxml
    private static HashMap<String,Class<?>> processors = new HashMap<>();

    // The current processor being used to parse the document
    private XMLProcessor processor = null;
    private Object callData;

    /* A stack of XML processors. This allows processors to invoke sub-processors
       for handling nested tags more efficiently. */
    private Stack<XMLProcessor> processorStack = new Stack<>();

    // Introduced for fix of Eclipse BZ#343025
    // As per SAX spec, SAX parsers are allowed to split character data into as many chunks as
    // they please, and they can split the text at whichever boundaries they want. In order to
    // handle this properly, it is needed to accumulate the text returned in each call
    // until it recieves a callback that isn't characters.
    private StringBuffer charactersBuffer;

    // A convenience class for specifying XMLProcessors
    private static class ProcessorItem {
        public String tagName;
        public Class<?> handlerClass;
        public ProcessorItem(String name, Class<?> cls) {
            tagName = name;
            handlerClass = cls;
        }
    }

    // The list of all "root" XML tags and their handler classes
    private static final ProcessorItem[] handlerList = {
        new ProcessorItem(OpxmlConstants.INFO_TAG, OpInfoProcessor.class),
        new ProcessorItem(OpxmlConstants.CHECKEVENTS_TAG, CheckEventsProcessor.class),
        new ProcessorItem(OpxmlConstants.MODELDATA_TAG, ModelDataProcessor.class),
        new ProcessorItem(OpxmlConstants.SESSIONS_TAG, SessionsProcessor.class)
    };

    /**
     * Returns an instance of the handler. This must be used to access the parser!
     * @return a handler instance
     */
    public static OprofileSAXHandler getInstance(Object callData) {
        if (instance == null) {
            instance = new OprofileSAXHandler();

            // Initialize processor map
            for (int i = 0; i < handlerList.length; ++i) {
                processors.put(handlerList[i].tagName, handlerList[i].handlerClass);
            }
        }

        // Set calldata into handler
        instance.setCallData (callData);
        return instance;
    }

    /**
     * Sets the calldata for the processor.
     * @param callData the calldata to pass to the processor
     */
    private void setCallData(Object callData) {
        this.callData = callData;
    }

    /**
     * Returns the processor for a given request type.
     * @param type the name of the processor
     * @return the requested processor or null
     */
    private static XMLProcessor getProcessor(String type) {
        XMLProcessor processor = null;

        Class<?> handlerClass = processors.get(type);
        if (handlerClass != null) {
            try {
                processor = (XMLProcessor) handlerClass.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
            }
        }

        return processor;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() {
        // Reset processor
        processor = null;
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() {
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String lName, String qName, Attributes attrs) {
        if (processor == null) {
            // Get processor for this event type
            processor = getProcessor(qName);
            processor.reset(callData);
        }

        // If we already have a processor, so let it deal with this new element.
        // Allow the processor to deal with it's own tag as well: this way it can
        // grab attributes from it.
        processor.startElement(qName, attrs, callData);

        // Clean up the characters buffer
        charactersBuffer = new StringBuffer();
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String name, String qName) {
        // Set the accumulated characters
        processor.characters(charactersBuffer.toString(), callData);
        processor.endElement(qName, callData);
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char ch[], int start, int length) {
        // Ignore characters which are only whitespace
        String str = new String(ch, start, length).trim();
        if (str.length() > 0 && processor != null)
             // Append the character to the buffer.
             charactersBuffer.append(str);
    }

    /**
     * Pushes the current XMLProcessor onto the stack and installs the given
     * processor as the document's parser/handler.
     * @param proc the processor to continue parsing the document
     */
    public void push(XMLProcessor proc) {
        processorStack.add(processor);
        processor = proc;
        processor.reset(callData);
    }

    /**
     * Removes the current XMLProcessor and installs the previous processor.
     * NOTE: This assumes that endElement caused the pop, so it calls endElement in
     * the parent processor.
     * @param tag the XML tag to pass to the parent processor
     */
    public void pop(String tag) {
        processor = processorStack.pop();
        processor.endElement(tag, callData);
    }
}
