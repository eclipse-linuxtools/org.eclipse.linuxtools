/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/**
 * Gives easy access to data from the eclipse feature.xml file.
 *
 */
public class FeatureModel {
	
	private Document docroot;
	private XPath xpath;
	private Properties featureProperties;

	/**
	 * Instantiates the model with the given XML document and preparing the
	 * XPath evaluation environment.
	 *
	 * @param docroot
	 *            The document to query.
	 * @param featureProperties The properties as loaded by feature.properties.
	 */
	public FeatureModel(Document docroot, Properties featureProperties) {
		this.docroot = docroot;
		this.featureProperties = featureProperties;
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	private String xpathEval(String path) {
		String result = "";
		try {
			result = xpath.evaluate(path, docroot);
		} catch (XPathExpressionException e) {
			//ignore, empty string is fine on missing path
		}
		return result;
	}

	/**
	 * Get value for a given key from the feature.properties file, if the key
	 * don't start with '%' we just return the given key.
	 * 
	 * @param key
	 *            to find in feature.properties
	 * @return the value
	 */
	private String resolveFeatureProperties(String key) {
		if (key != null && key.trim().startsWith("%")) {
			return featureProperties.getProperty(key.trim().replaceAll("%", ""));
		} else {
			return key;
		}
	}
	
	/**
	 * Returns the package name. 
	 * It is calculated by using the feature id last segment unless the last segment
	 * is feature in which case the previous one is used.
	 * @return The package name.
	 */
	public String getSimplePackageName() {
		String packageName = xpathEval("/feature/@id");
		String[] packageItems = packageName.split("\\.");
		String name = packageItems[packageItems.length - 1];
		if (name.equalsIgnoreCase("feature")) {
			name = packageItems[packageItems.length - 2];
		}
		
		return name;
	}

	/**
	 * The simple package name with "eclipse-" prepended to make better RPM package name.
	 * @return The package
	 */
	public String getPackageName() {
		return "eclipse-"+getSimplePackageName();
	}

	/**
	 * Returns the feature id as retrieved with /feature/@id xpath.
	 * @return The feature id.
	 */
	public String getFeatureId() {
		return xpathEval("/feature/@id");
	}

	/**
	 * Returns the version as retrieved by /feature/@version xpath with .qualifier removed if any.
	 * 
	 * @return The version of the feature.
	 */
	public String getVersion() {
		String version = xpathEval("/feature/@version");
		version = version.replaceAll(".qualifier", "");
		return version;
	}

	/**
	 * Returns the summary as retrieved by /feature/@label xpath.
	 * 
	 * @return The package summary.
	 */
	public String getSummary() {
		return resolveFeatureProperties(xpathEval("/feature/@label"));
	}

	/**
	 * Returns the license. 
	 * The algorithm is to look at /feature/license and/or /feature/license/@url and look for epl or cpl.
	 * Otherwise #FIX ME comment is returned
	 * @return The license of the feature.
	 */
	public String getLicense() {
		String urlString =  resolveFeatureProperties(xpathEval("/feature/license/@url"));
		String urlAnotation = resolveFeatureProperties(xpathEval("/feature/license"));
		String license = "#FIXME";
		if ((urlString != null && urlAnotation != null)) {
				if ((urlString.indexOf("epl") > -1 || urlAnotation
						.indexOf("epl") > -1)) {
					license = "EPL";
				} else if ((urlString.indexOf("cpl") > -1 || urlAnotation
						.indexOf("cpl") > -1)) {
					license = "CPL";
				}
		}
		return license;
	}
	
	

	/**
	 * Returns the url as fetched from /feature/description/@url xpath if any.
	 * @return The url.
	 */
	public String getURL() {
		return xpathEval("/feature/description/@url");
	}

	/**
	 * Returns the description as fetched by /feature/description and resolved from feature.properties if needed.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return resolveFeatureProperties(xpathEval("/feature/description"));
	}

}
