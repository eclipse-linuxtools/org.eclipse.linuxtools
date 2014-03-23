/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DevHelpTopic implements ITopic {

	private String name;
	private XPath xpath;
	private String label;
	private String link;
	private List<ITopic> subTopics;

	DevHelpTopic(String name) {
		this.name = name;
		xpath = XPathFactory.newInstance().newXPath();
		subTopics = new ArrayList<>();
		label = name;
		init();
	}

	private void init() {
		IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
		IPath devhelpLocation = new Path(
				ps.getString(PreferenceConstants.DEVHELP_DIRECTORY)).append(
				name).append(name + ".devhelp2"); //$NON-NLS-1$
		File devhelpFile = devhelpLocation.toFile();
		if (devhelpFile.exists()) {
			DocumentBuilderFactory docfactory = DocumentBuilderFactory
					.newInstance();
			docfactory.setValidating(false);
			try {
				docfactory.setFeature("http://xml.org/sax/features/namespaces", //$NON-NLS-1$
						false);
				docfactory.setFeature("http://xml.org/sax/features/validation", //$NON-NLS-1$
						false);
				docfactory
						.setFeature(
								"http://apache.org/xml/features/nonvalidating/load-dtd-grammar", //$NON-NLS-1$
								false);
				docfactory
						.setFeature(
								"http://apache.org/xml/features/nonvalidating/load-external-dtd", //$NON-NLS-1$
								false);

				DocumentBuilder docbuilder = docfactory.newDocumentBuilder();
				Document docroot = docbuilder.parse(devhelpLocation.toFile());

				// set label
				label = xpathEval("/book/@title", docroot); //$NON-NLS-1$
				if (label.isEmpty()) {
					label = name;
				}
				link = xpathEval("/book/@link", docroot); //$NON-NLS-1$

				// set subtopics
				NodeList nodes = xpathEvalNodes("/book/chapters/sub", docroot); //$NON-NLS-1$
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					subTopics.add(new SimpleTopic(name, node));
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String xpathEval(String path, Document docroot) {
		String result = ""; //$NON-NLS-1$
		try {
			result = xpath.evaluate(path, docroot);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private NodeList xpathEvalNodes(String path, Document docroot) {
		NodeList result = null;
		try {
			result = (NodeList) xpath.evaluate(path, docroot,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	@Override
	public IUAElement[] getChildren() {
		return getSubtopics();
	}

	@Override
	public String getHref() {
		return "/" + DevHelpPlugin.PLUGIN_ID + "/" + name + "/"+link; //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ITopic[] getSubtopics() {
		return subTopics.toArray(new ITopic[subTopics.size()]);
	}
}