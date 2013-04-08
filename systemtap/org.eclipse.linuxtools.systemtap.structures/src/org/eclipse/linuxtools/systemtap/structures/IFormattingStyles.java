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

package org.eclipse.linuxtools.systemtap.structures;

import org.eclipse.linuxtools.internal.systemtap.structures.Localization;

public interface IFormattingStyles {
	public static final int UNFORMATED	= 0;
	public static final int STRING		= 1;
	public static final int DATE		= 2;
	public static final int DOUBLE		= 3;
	public static final int HEX			= 4;
	public static final int OCTAL		= 5;
	public static final int BINARY		= 6;

	public static String[] FORMAT_TITLES = {Localization.getString("IFormattingStyles.Unformatted"), Localization.getString("IFormattingStyles.String"), Localization.getString("IFormattingStyles.Date"), Localization.getString("IFormattingStyles.Double"), Localization.getString("IFormattingStyles.Hex"), Localization.getString("IFormattingStyles.Octal"), Localization.getString("IFormattingStyles.Binary")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	public void setFormat(int format);
	public String format(String s);
	public int getFormat();
}
