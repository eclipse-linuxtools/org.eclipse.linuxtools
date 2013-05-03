/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.runnable;

import java.io.InputStream;

/**
 * A {@link StreamGobbler} that reads the stream into a {@link StringBuilder}
 * instead of notifying observers.
 *
 */
public class StringStreamGobbler extends StreamGobbler {
	StringBuilder output;

	public StringStreamGobbler(InputStream is) {
		super(is);
		this.output = new StringBuilder();
	}

	@Override
	public void fireNewDataEvent(String line) {
		output.append(line);
	}

	public StringBuilder getOutput() {
		return output;
	}
}
