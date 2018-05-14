/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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
package org.eclipse.linuxtools.docker.core;

/**
 * Docker status values.
 * 
 * @author xcoulon
 *
 */
public enum EnumDockerStatus {

	STOPPED, RUNNING, PAUSED, EXITED, UNKNOWN;
	
	/**
	 * Finds the {@link EnumDockerStatus} from the given status message
	 * @param statusMessage the {@link IDockerContainer#status()} message
	 * @return the corresponding {@link EnumDockerStatus}
	 */
	public static EnumDockerStatus fromStatusMessage(final String statusMessage) {
		if (statusMessage.startsWith("Exited") //$NON-NLS-1$
				|| statusMessage.startsWith("Stopped") //$NON-NLS-1$
				|| statusMessage.startsWith("Created")) {//$NON-NLS-1$
			return STOPPED;
		} else if (statusMessage.startsWith("Running") && statusMessage.endsWith("(Paused)")) { //$NON-NLS-1$ //$NON-NLS-2$
			return PAUSED;
		} else if (statusMessage.startsWith("Running")) { //$NON-NLS-1$ //$NON-NLS-2$
			return RUNNING;
		} else if (statusMessage.startsWith("Up") && statusMessage.endsWith("(Paused)")) { //$NON-NLS-1$ //$NON-NLS-2$
			return PAUSED;
		} else if (statusMessage.startsWith("Up")) { //$NON-NLS-1$ //$NON-NLS-2$
			return RUNNING;
		}
		return UNKNOWN;
	}

}
