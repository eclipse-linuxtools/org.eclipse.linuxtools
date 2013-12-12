/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpmstubby;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class for RPMStubby to find the parent pom
 *
 * @since 1.2
 */
public class RPMStubbyUtils {

	private DocumentBuilder builder;
	private XPath xPath;
	private static final String PARENT_NODE = "/project/parent";

	/**
	 * Constructor.
	 *
	 */
	public RPMStubbyUtils() {
		DocumentBuilderFactory builderFactory =
		        DocumentBuilderFactory.newInstance();
		builder = null;
		try {
		    builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		    StubbyLog.logError(e);
		}
		xPath =  XPathFactory.newInstance().newXPath();
	}

	/**
	 * Find the parent pom.xml of a file by recursively checking
	 * the current directory for the parent pom.xml and moving up
	 * the directory structure if there is none.
	 *
	 * @param folderPath The container of the file to check.
	 * @return The path of the parent pom. Null otherwise.
	 */
	public boolean findPom(IPath folderPath) {
		IPath filePath = folderPath.append(new Path("pom.xml"));
		File file = filePath.toFile();
		boolean rc = false;
		if (file.exists()) {
			try {
				Document xmlDocument = builder.parse(new FileInputStream(file));
				String parent = xPath.compile(PARENT_NODE).evaluate(xmlDocument);
				if (!parent.equals("")) {
					rc = findPom(folderPath.removeLastSegments(1));
				} else {
					rc = true;
				}
			} catch (SAXException e) {
				StubbyLog.logError(e);
			} catch (IOException e) {
				StubbyLog.logError(e);
			} catch (XPathExpressionException e) {
				StubbyLog.logError(e);
			}
		}
		return rc;
	}
}
