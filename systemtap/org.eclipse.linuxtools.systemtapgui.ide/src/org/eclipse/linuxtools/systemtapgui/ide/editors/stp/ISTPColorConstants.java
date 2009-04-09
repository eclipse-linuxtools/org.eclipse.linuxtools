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

package org.eclipse.linuxtools.systemtapgui.ide.editors.stp;

import org.eclipse.swt.graphics.RGB;

/**
 * Color constants that define specific text styles in the STP Editor.
 */
public interface ISTPColorConstants {
	RGB COMMENT = new RGB(0, 128, 0);
	RGB DEFAULT= new RGB(0, 0, 0);
	RGB EMBEDDED = new RGB (0, 64, 64); 
	RGB EMBEDDEDC = new RGB (0, 64, 64); 
	RGB KEYWORD= new RGB(127, 0, 55);
	RGB STRING= new RGB(0, 0, 255);
	RGB TYPE= new RGB(0, 0, 128);
}
