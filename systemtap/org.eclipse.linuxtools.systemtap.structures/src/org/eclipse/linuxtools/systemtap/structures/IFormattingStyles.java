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
    int UNFORMATED    = 0;
    int STRING        = 1;
    int DATE        = 2;
    int DOUBLE        = 3;
    int HEX            = 4;
    int OCTAL        = 5;
    int BINARY        = 6;

    String[] FORMAT_TITLES = {Localization.getString("IFormattingStyles.Unformatted"), Localization.getString("IFormattingStyles.String"), Localization.getString("IFormattingStyles.Date"), Localization.getString("IFormattingStyles.Double"), Localization.getString("IFormattingStyles.Hex"), Localization.getString("IFormattingStyles.Octal"), Localization.getString("IFormattingStyles.Binary")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

    void setFormat(int format);
    String format(String s);
    int getFormat();
}
