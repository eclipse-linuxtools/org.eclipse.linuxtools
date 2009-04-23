/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.memcheck;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ValgrindXMLParser {
	protected static final String END_TAG = "</valgrindoutput>"; //$NON-NLS-1$

	protected DocumentBuilder builder;
	protected ArrayList<ValgrindError> errors;

	public ValgrindXMLParser(InputStream in) throws ParserConfigurationException, IOException, CoreException, SAXException {
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			errors = new ArrayList<ValgrindError>();

			Document doc = builder.parse(in);

			NodeList nodes = doc.getElementsByTagName("error"); //$NON-NLS-1$
			for (int i = 0; i < nodes.getLength(); i++) {
				errors.add(new ValgrindError(nodes.item(i)));
			}
		} finally {
			in.close();
		}
	}

	public ArrayList<ValgrindError> getErrors() {
		return errors;
	}
}
