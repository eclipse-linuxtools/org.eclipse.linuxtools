/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import org.xml.sax.Attributes;

/**
 * A class which (minimally) parses XML documents. This class provides only basic
 * support for collecting information from XML documents. It is intended to be subclassed,
 * providing only common functionality for all parser classes.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class XMLProcessor {
	// The characters in the current tag
	protected String _characters;
	
	/**
	 * This method is called whenever the SAXHandler is about to invoke the
	 * processor for the first time on a given document.
	 * @param callData call data for the processor (usually the result is stored here)
	 */
	public void reset(Object callData) {
	};
	
	/**
	 * This method is called whenever a new tag is seen in the document. By default,
	 * this process will clear the characters collected for the tag. Processors will typically
	 * call this baseclass method to setup for collecting new character information for a
	 * tag.
	 * @param name the name of the tag
	 * @param attrs the tag's attributes
	 * @param callData call data for the processor (usually the result is stored here)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		_characters = new String();
	}
	
	/**
	 * This method is called whenever the closing tag for an element is seen in the
	 * document.
	 * @param name the element which is ending
	 * @param callData call data for the processor (usually the result is stored here)
	 */
	public void endElement(String name, Object callData) {
	}
	
	/**
	 * This method is called whenever characters are seen in the document that are not in
	 *  a markup tag.
	 * @param chars the characters read
	 * @param callData call data for the processor (usually the result is stored here)
	 */
	public void characters(String chars, Object callData) {
		_characters += chars;
	}
}
