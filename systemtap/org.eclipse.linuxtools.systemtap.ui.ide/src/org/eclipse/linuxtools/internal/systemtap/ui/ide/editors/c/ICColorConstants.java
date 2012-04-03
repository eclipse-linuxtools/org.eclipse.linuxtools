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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c;

import org.eclipse.swt.graphics.RGB;

/**
 * This interface defines the constant colors that are used for syntax highlighting inside
 * <code>CEditor</code>
 * @author Ryan Morse
 */
public interface ICColorConstants {
	RGB COMMENT= new RGB(0, 128, 0);
	RGB PREPROCESSOR = new RGB(180,56,231);
	RGB KEYWORD= new RGB(127, 0, 85);
	RGB TYPE= new RGB(0, 0, 128);
	RGB STRING= new RGB(0, 0, 255);
	RGB DEFAULT= new RGB(0, 0, 0);
}
