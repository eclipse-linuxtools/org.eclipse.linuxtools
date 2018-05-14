/*******************************************************************************
 * Copyright (c) 2008, 2018 Alexander Kurtakov.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

/**
 * Resolution for the "rpm-buildroot-usage" rpmlint warning. Resolves by
 * removing the offending line.
 *
 */
public class RpmBuildrootUsage extends ARemoveLineResolution {
	/**
	 * The rpmlint ID of the warning.
	 */
	public static final String ID = "rpm-buildroot-usage"; //$NON-NLS-1$

	@Override
	public String getDescription() {
		return Messages.RpmBuildrootUsage_0;
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
