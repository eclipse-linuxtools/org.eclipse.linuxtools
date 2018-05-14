/*******************************************************************************
 * Copyright (c) 2015, 2018 EZChip Semiconductor and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Slava Risenberg <slava@ezchip.com> - adding support for Doxygen XML files as input
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.w3c.dom.Document;

public abstract class LibhoverInfoGenerator {

	protected Document document;

	public void generate(String outputFile){
		LibHoverInfo hoverInfo = doGenerate();
		save(hoverInfo, outputFile);
	}

	protected abstract LibHoverInfo doGenerate();

	protected void save(LibHoverInfo hoverInfo, String fileName){
		try (FileOutputStream f = new FileOutputStream(fileName);
				ObjectOutputStream out = new ObjectOutputStream(f)) {
			out.writeObject(hoverInfo);
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
