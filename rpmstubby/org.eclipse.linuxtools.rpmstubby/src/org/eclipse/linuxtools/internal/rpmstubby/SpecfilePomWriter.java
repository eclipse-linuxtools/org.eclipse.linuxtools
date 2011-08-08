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
package org.eclipse.linuxtools.internal.rpmstubby;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility class used for writing the generated specfile to a file.
 *
 */
public class SpecfilePomWriter {

	/**
	 * Parse the pom.xml and write the generated specfile.
	 * @param pomFile The pom.xml file.
	 */
	public void write(IFile pomFile) {

		StubbyPomGenerator generator = new StubbyPomGenerator(pomFile);
		try {
			generator.writeContent(pomFile.getProject().getName());
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}

}
