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

import org.xml.sax.Attributes;

/**
 * A class which (minimally) parses XML documents. This class provides only basic
 * support for collecting information from XML documents. It is intended to be subclassed,
 * providing only common functionality for all parser classes.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class XMLProcessor {
	// The characters in the current tag
	protected String characters;
	
	/**
	 * This method is called whenever the SAXHandler is about to invoke the
	 * processor for the first time on a given document.
	 * @param callData call data for the processor (usually the result is stored here)
	 */
	public void reset(Object callData) {
	}
	
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
		characters = "";
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
		characters = chars;
	}
	
	/**
	 * This method is called on attribute strings and does the reverse of valid_string in
	 * xmlfmt.cc in opxml. 
	 * @param source source attribute string 
	 * @return the source string with escaped characters translated back to their single character counterpart
	 */
	public String validString(String source) {
		final String chars_long[] = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		final String chars[] = {"&", "\"", "'", "<", ">"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		for (int i = 0; i < chars_long.length; i++) {
			source.replaceAll(chars_long[i], chars[i]);
		}
		return source;
	}
}
