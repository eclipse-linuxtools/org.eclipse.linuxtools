/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
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
 * Resolution for "macro-in-%changelog" warning. 
 * The resolution is to escape the macro.
 *
 */
public class MacroInChangelog extends AReplaceTextResolution {

	/**
	 * The string ID of the rpmlint warning.
	 */
	public static final String ID = "macro-in-%changelog"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions.AReplaceTextResolution#getOriginalString()
	 */
	@Override
	public String getOriginalString() {
		return "%"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions.AReplaceTextResolution#getReplaceString()
	 */
	@Override
	public String getReplaceString() {
		return "%%"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	public String getDescription() {
		return Messages.MacroInChangelog_0
				+ Messages.MacroInChangelog_1;
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return ID;
	}
}
