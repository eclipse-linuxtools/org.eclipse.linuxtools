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

package org.eclipse.linuxtools.systemtap.ui.systemtapgui;

import java.io.File;

/**
 * @since 2.0
 */
public interface SystemTapGUISettings {
	File settingsFolder = new File(System.getenv("HOME") + "/.systemtapgui/");
	String installDirectory = System.getProperty("user.dir");
	String tempDirectory = "/tmp/systemtapgui/";
}
