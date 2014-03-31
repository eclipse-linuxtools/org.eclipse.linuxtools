/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.launch;

import org.eclipse.osgi.util.NLS;

public class GprofLaunchMessages extends NLS {

	public static String GprofCompilerOptions_msg;
	public static String GprofCompileAgain_msg;
	public static String GprofGmonStale_msg;
	public static String GprofGmonStaleExplanation_msg;

	public static String GprofNoGmonDialog_Browse;
	public static String GprofNoGmonDialog_Cancel;
	public static String GprofNoGmonDialog_OpenGmon;
	public static String GprofNoGmonDialog_Workspace;

	static {
		NLS.initializeMessages(GprofLaunchMessages.class.getName(), GprofLaunchMessages.class);
	}

}
