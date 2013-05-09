/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck;

import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

public class MemcheckPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.memcheck"; //$NON-NLS-1$

	public static final String TOOL_ID = "org.eclipse.linuxtools.valgrind.launch.memcheck"; //$NON-NLS-1$

	public static FontMetrics getFontMetrics(Control control) {
		 GC gc = new GC(control);
		 gc.setFont(control.getFont());
		 FontMetrics fontMetrics = gc.getFontMetrics();
		 gc.dispose();
		 return fontMetrics;
	}

}
