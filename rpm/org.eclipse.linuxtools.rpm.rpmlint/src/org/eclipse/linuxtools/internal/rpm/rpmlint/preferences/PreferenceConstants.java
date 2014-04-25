/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.rpmlint.preferences;

/**
 * Constant definitions for rpmlint plug-in preferences
 */
public class PreferenceConstants {
    /** Path to rpmlint executable.     */
    public static final String P_RPMLINT_PATH = "RpmlintPath"; //$NON-NLS-1$
    /** Preference to show warnings about using tabs. */
    public static final String P_RPMLINT_TABS = "RpmlintTab"; //$NON-NLS-1$
    /** Preference to show warnings about using spaces. */
    public static final String P_RPMLINT_SPACES = "RpmlintSpaces"; //$NON-NLS-1$
    /** Preference to show warnings about using tabs or spaces. */
    public static final String P_RPMLINT_TABS_AND_SPACES = P_RPMLINT_SPACES;

    /**
     * Default path to rpmlint executable (/usr/bin/rpmlint).
     */
    public static final String DP_RPMLINT_PATH = "/usr/bin/rpmlint"; //$NON-NLS-1$
}
