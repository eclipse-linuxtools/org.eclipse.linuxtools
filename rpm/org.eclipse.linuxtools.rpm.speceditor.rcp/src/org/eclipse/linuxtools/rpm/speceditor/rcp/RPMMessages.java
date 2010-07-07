/*******************************************************************************
 * Copyright (c) 20109 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.speceditor.rcp;

import org.eclipse.osgi.util.NLS;

public final class RPMMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.rpm.speceditor.rcp.RPMMessages"; //$NON-NLS-1$
	public static String CannotOpen;
	public static String InitialOpen;
	public static String OpenFile;
	public static String NullFile;
	public static String Problem;
	public static String EditorTitle;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, RPMMessages.class);
	}

	private RPMMessages() {
		super();
	}
}
