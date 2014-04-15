/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

/**
 * Resolution for the "patch-not-applied" rpmlint warning.
 * Resolves by removing the patch definition.
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
