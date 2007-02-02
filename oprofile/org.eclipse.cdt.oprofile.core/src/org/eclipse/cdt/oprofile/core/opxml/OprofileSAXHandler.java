/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The SAX handler class that is used to parse the output of opxml.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class OprofileSAXHandler extends DefaultHandler {
	// The only allowed instance of this class
	private static OprofileSAXHandler _instance = null;
	
	// A Map of all the XML processors for opxml
	private static HashMap _processors = new HashMap();
	
	// The current processor being used to parse the document
	private XMLProcessor _processor = null;
	private Object _callData;
	
	/* A stack of XML processors. This allows processors to invoke sub-processors
	   for handling nested tags more efficiently. */
	private Stack _processorStack = new Stack();
	
	// A convenience class for specifying XMLProcessors
	private static class ProcessorItem {
		public String tagName;
		public Class handlerClass;
		public ProcessorItem(String name, Class cls) {
			tagName = name;
			handlerClass = cls;
		}
	}
	
	// The list of all "root" XML tags and their handler classes 
	private static final ProcessorItem[] _handlerList = {
		new ProcessorItem(OpxmlConstants.INFO_TAG, OpInfoProcessor.class),
		new ProcessorItem(OpxmlConstants.CHECKEVENTS_TAG, CheckEventsProcessor.class),
		new ProcessorItem(OpxmlConstants.DEBUGINFO_TAG, DebugInfoProcessor.class),
		new ProcessorItem(OpxmlConstants.SAMPLES_TAG, SamplesProcessor.class),
		new ProcessorItem(OpxmlConstants.SESSIONS_TAG, SessionsProcessor.class)
	};
	
	/**
	 * Returns an instance of the handler. This must be used to access the parser!
	 * @return a handler instance
	 */
	public static OprofileSAXHandler getInstance(Object callData) {
		if (_instance == null) {
			_instance = new OprofileSAXHandler();
			
			// Initialize processor map
			for (int i = 0; i < _handlerList.length; ++i) {
				_processors.put(_handlerList[i].tagName, _handlerList[i].handlerClass);
			}
		}
		
		// Set calldata into handler
		_instance.setCallData (callData);
		return _instance;
	}
	
	/**
	 * Sets the calldata for the processor.
	 * @param callData the calldata to pass to the processor
	 */
	public void setCallData(Object callData)
	{
		_callData = callData;
	}
	
	/**
	 * Returns the processor for a given request type.
	 * @param type the name of the processor
	 * @return the requested processor or null
	 */
	public static XMLProcessor getProcessor(String type) {
		XMLProcessor processor = null;
		
		Class handlerClass = (Class) _processors.get(type);
		if (handlerClass != null) {
			try {
				processor = (XMLProcessor) handlerClass.newInstance();
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		
		return processor;
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() {
		// Reset processor
		_processor = null;
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() {
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String lName, String qName, Attributes attrs) {
		if (_processor == null) {
			// Get processor for this event type
			_processor = getProcessor(qName);
			_processor.reset(_callData);
		}
		
		// If we already have a processor, so let it deal with this new element.
		// Allow the processor to deal with it's own tag as well: this way it can
		// grab attributes from it.
		_processor.startElement(qName, attrs, _callData);
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String name, String qName) {
		_processor.endElement(qName, _callData);
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char ch[], int start, int length) {
		// Ignore characters which are only whitespace
		String str = new String(ch, start, length).trim();
		if (str.length() > 0 && _processor != null)
			_processor.characters(new String(ch, start, length), _callData);
	}
	
	/**
	 * Returns the processor used to parse the document.
	 * @return the XMLProcessor
	 */
	public XMLProcessor getProcessor() {
		return _processor;
	}
	
	/**
	 * Pushes the current XMLProcessor onto the stack and installs the given
	 * processor as the document's parser/handler.
	 * @param proc the processor to continue parsing the document
	 */
	public void push(XMLProcessor proc) {
		_processorStack.add(_processor);
		_processor = proc;
		_processor.reset(_callData);
	}
	
	/**
	 * Removes the current XMLProcessor and installs the previous processor.
	 * NOTE: This assumes that endElement caused the pop, so it calls endElement in
	 * the parent processor.
	 * @param tag the XML tag to pass to the parent processor
	 */
	public void pop(String tag) {
		_processor = (XMLProcessor) _processorStack.pop();
		_processor.endElement(tag, _callData);
	}
}
