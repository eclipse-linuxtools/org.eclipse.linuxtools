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
 * Resolution for "macro-in-%changelog" warning. The resolution is to escape the
 * macro.
 *
 */
public class MacroInChangelog extends AReplaceTextResolution {

	/**
	 * The string ID of the rpmlint warning.
	 */
	public static final String ID = "macro-in-%changelog"; //$NON-NLS-1$

	@Override
	public String getOriginalString() {
		return "%"; //$NON-NLS-1$
	}

	@Override
	public String getReplaceString() {
		return "%%"; //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return Messages.MacroInChangelog_0 + Messages.MacroInChangelog_1;
	}

	@Override
	public String getLabel() {
		return ID;
	}
}
