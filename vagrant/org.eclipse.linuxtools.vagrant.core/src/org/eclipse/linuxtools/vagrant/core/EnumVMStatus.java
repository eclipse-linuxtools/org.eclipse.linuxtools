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
package org.eclipse.linuxtools.vagrant.core;

public enum EnumVMStatus {

	SHUTOFF, RUNNING, PAUSED, UNKNOWN;

	public static EnumVMStatus fromStatusMessage(final String status) {
		if (status.equals("shutoff") //$NON-NLS-1$
				|| status.equals("poweroff")) { //$NON-NLS-1$
			return SHUTOFF;
		} else if (status.startsWith("pause")) { //$NON-NLS-1$
			return PAUSED;
		} else if (status.startsWith("running")) { //$NON-NLS-1$
			return RUNNING;
		}
		return UNKNOWN;
	}
}
