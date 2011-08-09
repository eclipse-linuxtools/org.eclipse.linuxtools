/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpmstubby;

/**
 * Supported input types and file name patterns for the corresponding files.
 * 
 */
public enum InputType {

	/** Eclipse feature.xml file. */
	ECLIPSE_FEATURE("feature.xml"),
	/** Maven pom.xml file. */
	MAVEN_POM("pom.xml");

	private String fileNamePattern;

	private InputType(String fileName) {
		this.fileNamePattern = fileName;
	}

	/**
	 * Returns the file name pattern for the input type.
	 * 
	 * @return The pattern for the file name e.g. feature.xml or pom.xml.
	 */
	public String getFileNamePattern() {
		return fileNamePattern;
	}

}
