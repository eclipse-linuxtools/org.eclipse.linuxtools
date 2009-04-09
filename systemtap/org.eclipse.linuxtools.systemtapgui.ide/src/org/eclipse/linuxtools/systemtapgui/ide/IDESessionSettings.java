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

package org.eclipse.linuxtools.systemtapgui.ide;

import org.eclipse.linuxtools.systemtapgui.ide.editors.stp.STPEditor;

/**
 * A simple class that contains information about the current session of the IDE, such as
 * the path to the tapset libraries, the active SystemTap Script Editor, and if the user
 * chooses, the user's account password.
 * @author Ryan Morse
 */
public class IDESessionSettings {
	public static String tapsetLocation = "";
	public static STPEditor activeSTPEditor = null;
	public static String password = null;
}
