/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

public final class RemoteLaunchConstants {
	
	private static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch.remote"; //$NON-NLS-1$
	
	public static final String ATTR_REMOTE_OUTPUTDIR = PLUGIN_ID + ".REMOTE_OUTPUTDIR"; //$NON-NLS-1$

	public static final String DEFAULT_REMOTE_OUTPUTDIR = "/tmp"; //$NON-NLS-1$
}
