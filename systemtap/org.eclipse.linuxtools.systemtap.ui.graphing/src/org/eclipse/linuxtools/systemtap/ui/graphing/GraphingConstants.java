/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphing;

import java.io.File;

import org.eclipse.linuxtools.systemtap.ui.systemtapgui.SystemTapGUISettings;


public final class GraphingConstants {
	private static final String dataSetFileName = "/GraphSettings.xml"; //$NON-NLS-1$
	public static final File DataSetMetaData = new File(SystemTapGUISettings.settingsFolder.getAbsolutePath() + dataSetFileName);
}
