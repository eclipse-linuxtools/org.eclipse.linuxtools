/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;

public class ThreadProfilerParser extends SystemTapParser {
	
	@Override
	protected void initialize() {
	}

	@Override
	public IStatus nonRealTimeParsing() {
		return realTimeParsing();
	}
	int counter = 0;
	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;
		
		if (!(view instanceof ThreadProfilerView))
			return Status.CANCEL_STATUS;
		
		BufferedReader buff = (BufferedReader) internalData;

		String line;
		
		try {
			while ((line = buff.readLine()) != null) {
				if (line.equals("--"))
					((ThreadProfilerView) view).tick();
				if (line.contains(", ")) {
					String[] blargh = line.split(", ");
					((ThreadProfilerView) view).addDataPoints(counter++, blargh);
				} else {
					String[] data = line.split(":");
					//TODO: Log error
					try {
						int tid = Integer.parseInt(data[0]);
						((ThreadProfilerView) view).addThread(tid, data[1]);
					} catch (NumberFormatException e) {
						//Do nothing
					}
				}
			}
			view.update();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 

		return Status.OK_STATUS;
	}

}
