/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility class used for writing the generated specfile to a file.
 *
 */
public class SpecfileWriter {
	
	/**
	 * Parse the feature.xml and write the generated specfile.
	 * @param featureFile The feature.xml file.
	 */
	public void write(IFile featureFile) {

		StubbyGenerator generator = new StubbyGenerator(featureFile);
		try {
			generator.writeContent(featureFile.getProject().getName());
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}
}
