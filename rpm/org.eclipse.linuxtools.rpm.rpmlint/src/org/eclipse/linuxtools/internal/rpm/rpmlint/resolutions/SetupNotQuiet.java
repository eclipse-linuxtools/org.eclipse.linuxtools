/*******************************************************************************
 * Copyright (c) 2007, 2013 Alphonse Van Assche and others.
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
 * Resolution for the "setup-not-quied" rpmlint warning.
 * Resolves by adding <b>-q</b> parameter to the %setup call.
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
