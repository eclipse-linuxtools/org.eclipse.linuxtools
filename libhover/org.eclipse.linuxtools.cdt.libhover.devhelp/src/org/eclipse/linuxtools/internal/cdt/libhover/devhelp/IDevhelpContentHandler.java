/*******************************************************************************
 * Copyright (c) 2025 Aleksandar Kurtakov and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Aleksandar Kurtakov - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

public interface IDevhelpContentHandler extends ContentHandler{

	void setHtmlsaxParser(DevHelpSAXParser htmlsaxParser);
	
	@Override
	public default void setDocumentLocator(Locator locator) {
	}

	@Override
	public default void startDocument() {
	}

	@Override
	public default void endDocument() {
	}

	@Override
	public default void startPrefixMapping(String prefix, String uri) {
	}

	@Override
	public default void endPrefixMapping(String prefix) {
	}
	
	@Override
	public default void ignorableWhitespace(char[] ch, int start, int length) {
	}

	@Override
	public default void processingInstruction(String target, String data) {
	}

	@Override
	public default void skippedEntity(String name) {
	}


}