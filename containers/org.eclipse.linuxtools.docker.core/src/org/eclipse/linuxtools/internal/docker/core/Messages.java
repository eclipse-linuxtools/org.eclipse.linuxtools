/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String Docker_Machine_Command_Not_Found;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}

}
