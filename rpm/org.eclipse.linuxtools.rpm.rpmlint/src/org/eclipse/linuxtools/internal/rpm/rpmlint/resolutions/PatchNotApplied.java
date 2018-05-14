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
 * Resolution for the "patch-not-applied" rpmlint warning. Resolves by removing
 * the patch definition.
 *
 */
public class PatchNotApplied extends ARemoveLineResolution {

	/**
	 * The rpmlint ID of the warning.
	 */
	public static final String ID = "patch-not-applied"; //$NON-NLS-1$

	@Override
	public String getDescription() {
		return Messages.PatchNotApplied_0;
	}

	@Override
	public String getLabel() {
		return ID;
	}

}
