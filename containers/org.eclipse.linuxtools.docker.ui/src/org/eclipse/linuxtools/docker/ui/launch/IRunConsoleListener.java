/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	void newOutput(String output);

}
