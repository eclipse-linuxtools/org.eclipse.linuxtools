/*******************************************************************************
 * Copyright (c) 2008, 2013 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

/**
 * Resolution for the "rpm-buildroot-usage" rpmlint warning.
 * Resolves by removing the offending line.
 *
 */
public class RpmBuildrootUsage extends ARemoveLineResolution{
	/**
	 * The rpmlint ID of the warning.
	 */
	public static final String ID = "rpm-buildroot-usage"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	@Override
	public String getDescription() {
		return Messages.RpmBuildrootUsage_0;
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	@Override
	public String getLabel() {
		return ID;
	}
}
