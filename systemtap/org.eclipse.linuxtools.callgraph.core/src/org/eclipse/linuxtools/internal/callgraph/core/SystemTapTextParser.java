/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SystemTapTextParser extends SystemTapParser{

	protected String contents;

	@Override
	public IStatus nonRealTimeParsing() {
		contents = Helper.readFile(sourcePath);
		System.out.println(contents);
		return Status.OK_STATUS;
	}

	@Override
	protected void initialize() {
		// Empty
	}

	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader)) {
			return Status.CANCEL_STATUS;
		}

		BufferedReader buff = (BufferedReader) internalData;
		StringBuffer text = new StringBuffer();

		String line;
		try {
			while ((line = buff.readLine()) != null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				text.append(line + "\n"); //$NON-NLS-1$
			}
			setData(text.toString());
			if (text.length() > 0) {
				System.out.println(text.toString());
			}
			view.update();
		} catch (IOException|InterruptedException e) {
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

}
