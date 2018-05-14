/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

/**
 * Resolution for the "setup-not-quied" rpmlint warning. Resolves by adding
 * <b>-q</b> parameter to the %setup call.
 *
 */
public class SetupNotQuiet extends AReplaceTextResolution {

	/**
	 * The rpmlint ID of the warning.
	 */
	public static final String ID = "setup-not-quiet"; //$NON-NLS-1$

	@Override
	public String getOriginalString() {
		return "%setup"; //$NON-NLS-1$
	}

	@Override
	public String getReplaceString() {
		return "%setup -q"; //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return Messages.SetupNotQuiet_0;
	}

	@Override
	public String getLabel() {
		return ID;
	}

}
