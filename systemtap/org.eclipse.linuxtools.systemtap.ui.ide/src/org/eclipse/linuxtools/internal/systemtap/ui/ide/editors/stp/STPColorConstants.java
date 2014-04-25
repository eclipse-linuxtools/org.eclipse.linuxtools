/* Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.swt.graphics.RGB;

public interface STPColorConstants {
    RGB KEYWORD = new RGB(127, 0, 85);
    RGB COMMENT = new RGB(63, 127, 95);
    RGB STP_STRING = new RGB(0, 0, 255);
    RGB DEFAULT = new RGB(0, 0, 0);
    RGB EMBEDDED = new RGB (0, 64, 64);
    RGB EMBEDDEDC = new RGB (0, 64, 64);
    RGB TYPE= new RGB(0, 0, 128);

}
