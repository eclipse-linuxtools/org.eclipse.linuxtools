/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - change samples to model-data
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml;

/**
 * A class of constants for communications with the opxml wrapper
 * program.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler
 */
public class OpxmlConstants {
    /**
     * Request static oprofile information (num counters, defaults, event lists)
     */
    public static final String OPXML_INFO = "info"; //$NON-NLS-1$
    public static final String INFO_TAG = OPXML_INFO;

    /**
     * Request model data for a session
     */
    public static final String OPXML_MODELDATA = "model-data"; //$NON-NLS-1$
    public static final String MODELDATA_TAG = OPXML_MODELDATA;

    /**
     * Request event validity check
     */
    public static final String OPXML_CHECKEVENTS = "check-events"; //$NON-NLS-1$
    public static final String CHECKEVENTS_TAG = OPXML_CHECKEVENTS;

    /**
     * Request session list
     */
    public static final String OPXML_SESSIONS  = "sessions"; //$NON-NLS-1$
    public static final String SESSIONS_TAG = OPXML_SESSIONS;
}
