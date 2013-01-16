/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - Copied from LoggedCommand and modified
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.linuxtools.systemtap.ui.structures.IPasswordPrompt;

public class LocalLoggedCommand extends LoggedCommand2 {
	public LocalLoggedCommand(String[] cmd, String[] envVars,
			IPasswordPrompt prompt, int monitorDelay) {
		super(cmd, envVars, prompt, monitorDelay);
	}
}
