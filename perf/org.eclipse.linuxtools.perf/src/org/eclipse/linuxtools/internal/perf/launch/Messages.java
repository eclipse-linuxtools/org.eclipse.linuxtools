/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.perf.launch.messages"; //$NON-NLS-1$
    public static String MsgProxyError;
    public static String PerfEventsTab_Add;
	public static String PerfEventsTab_Events;
	public static String PerfEventsTab_ForExample;
	public static String PerfEventsTab_ForExampleR1A8;
	public static String PerfEventsTab_HardwareBreakpoint;
	public static String PerfEventsTab_Note;
	public static String PerfEventsTab_RawRegisterEncoding;
	public static String PerfEventsTab_RemoveSelectedEvents;
	public static String PerfLaunchConfigDelegate_perf_not_found;
    public static String PerfLaunchConfigDelegate_analyzing;
    public static String PerfLaunchConfigDelegate_stat_title;
    public static String PerfOptionsTab_Browse;
    public static String PerfOptionsTab_File_DNE;
    public static String PerfOptionsTab_Kernel_Prompt;
    public static String PerfOptionsTab_Loc_DNE;
    public static String PerfOptionsTab_Options;
    public static String PerfOptionsTab_Requires_LTE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
