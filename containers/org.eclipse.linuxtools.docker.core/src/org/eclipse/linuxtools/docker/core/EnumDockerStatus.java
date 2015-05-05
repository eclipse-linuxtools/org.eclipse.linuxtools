/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		if (statusMessage.startsWith("Exited") || statusMessage.startsWith("Stopped")) {//$NON-NLS-1$
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
