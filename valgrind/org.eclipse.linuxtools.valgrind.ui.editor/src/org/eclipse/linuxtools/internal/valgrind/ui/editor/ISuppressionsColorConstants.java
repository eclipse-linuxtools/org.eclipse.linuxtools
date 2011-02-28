/*******************************************************************************
 * Copyright (c) 2008, 2009 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API.
 *    Red Hat - modifications for use with Valgrind plugins.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import org.eclipse.swt.graphics.RGB;

public interface ISuppressionsColorConstants {
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB TOOL = new RGB(153, 122, 0);
	public static final RGB SUPP_TYPE = new RGB(0, 153, 122);
	public static final RGB CONTEXT = new RGB(122, 0, 153);
	public static final RGB COMMENT = new RGB(0, 99, 166);
}
