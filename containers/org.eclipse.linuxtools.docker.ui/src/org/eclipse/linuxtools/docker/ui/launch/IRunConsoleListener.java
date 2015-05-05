/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.ui.launch;

public interface IRunConsoleListener {

	/**
	 * Listener receiver method called after output is written to Run Console.
	 * 
	 * @param output
	 *            string written to console
	 */
	public void newOutput(String output);

}
