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
