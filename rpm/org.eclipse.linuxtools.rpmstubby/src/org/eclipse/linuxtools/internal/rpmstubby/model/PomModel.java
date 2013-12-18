/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Gives easy access to data from the maven pom.xml file.
 *
 */
public class PomModel {

	private Document docroot;
	private XPath xpath;

	/**
	 * Instantiates the model with the given XML document and preparing the
	 * XPath evaluation environment.
	 *
	 * @param docroot
	 *            The document to query.
	 */
	public PomModel(Document docroot) {
		this.docroot = docroot;
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Returns the proposed package name.
	 *
	 * @return The proposed package name.
	 */
	public String getPackageName() {
		return xpathEval("/project/artifactId");
	}

	/**
	 * Returns the summary (xpath:/project/name). Maven project name is verbose
	 * and it corresponds to the RPM specfile Summary tag.
	 *
	 * @return The summary.
	 */
	public String getSummary() {
		return xpathEval("/project/name");
	}

	/**
	 * Returns the project version (xpath:/project/version) or the parent version if
	 * version is not present.
	 *
	 * @return The version.
	 */
	public String getVersion() {
		String version = xpathEval("/project/version");
		if (version.equals("")) {
			version = xpathEval("/project/parent/version");
		}
		return version;
	}

	/**
	 * Returns the license (xpath:/project/licenses/license/name).
	 *
	 * @return The license name.
	 */
	public String getLicense() {
		return xpathEval("/project/licenses/license/name");
	}

	/**
	 * Returns the URL (xpath:/project/url) or (xpath:/project/organization/url).
	 *
	 * @return The project url.
	 */
	public String getURL() {
		String url = xpathEval("/project/url");
		if (url.equals("")) {
			url = xpathEval("/project/organization/url");
		}
		return url;
	}

	/**
	 * Returns the project description (xpath:/project/description).
	 *
	 * @return The project description.
	 */
	public String getDescription() {
		return xpathEval("/project/description");
	}

	/**
	 * Returns the dependencies.
	 * @return All the dependencies.
	 */
	public Map<String, String> getDependencies() {
		Map<String, String> dependencies = new HashMap<>();
		NodeList nodes = xpathEvalNodes("/project/dependencies/dependency");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			try {
				dependencies.put(xpath.evaluate("groupId", node), xpath.evaluate("artifactId", node));
			} catch (XPathExpressionException e) {
				// just no deps generated
			}
		}
		return dependencies;
	}

	private String xpathEval(String path) {
		String result = "";
		try {
			result = xpath.evaluate(path, docroot);
		} catch (XPathExpressionException e) {
			// ignore, nothing that can be read
		}
		return result;
	}

	private NodeList xpathEvalNodes(String path) {
		NodeList result = null;
		try {
			result = (NodeList) xpath.evaluate(path, docroot,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// ignore, nothing that can be read
		}
		return result;
	}
}
