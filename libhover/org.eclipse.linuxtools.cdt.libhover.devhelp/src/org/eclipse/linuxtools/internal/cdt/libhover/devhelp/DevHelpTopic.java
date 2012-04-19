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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.libhover.devhelp.DevHelpPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences.PreferenceConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DevHelpTopic implements ITopic {

	private String name;
	private XPath xpath;
	private String label;

	DevHelpTopic(String name) {
		this.name = name;
		xpath = XPathFactory.newInstance().newXPath();
		init();
	}

	private void init() {
		IPreferenceStore ps = DevHelpPlugin.getDefault().getPreferenceStore();
		IPath devhelpLocation = new Path(
				ps.getString(PreferenceConstants.DEVHELP_DIRECTORY)).append(
				name).append(name + ".devhelp2");
		File devhelpFile = devhelpLocation.toFile();
		if (devhelpFile.exists()) {
			DocumentBuilderFactory docfactory = DocumentBuilderFactory
					.newInstance();
			docfactory.setValidating(false);
			try {
				docfactory.setFeature("http://xml.org/sax/features/namespaces",
						false);
				docfactory.setFeature("http://xml.org/sax/features/validation",
						false);
				docfactory
						.setFeature(
								"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
								false);
				docfactory
						.setFeature(
								"http://apache.org/xml/features/nonvalidating/load-external-dtd",
								false);

				DocumentBuilder docbuilder = docfactory.newDocumentBuilder();
				Document docroot = docbuilder.parse(devhelpLocation.toFile());
				
				label = xpathEval("/book/@title", docroot);
				if (label.equals("")) {
					label = name;
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			label = name;
		}

	}

	private String xpathEval(String path, Document docroot) {
		String result = "";
		try {
			result = xpath.evaluate(path, docroot);
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
		return new IUAElement[0];
	}

	@Override
	public String getHref() {
		return "/" + DevHelpPlugin.PLUGIN_ID + "/" + name + "/index.html"; // $NON-NLS-1$ //$NON-NLS-2$" //$NON-NLS-3$

	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ITopic[] getSubtopics() {
		return null;
	}
}