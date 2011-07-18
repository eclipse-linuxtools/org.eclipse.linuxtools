/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - modified for shared usage with remote tools
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.profiling.launch.remote;

public final class RemoteLaunchConstants {
	
	private static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.launch.remote"; //$NON-NLS-1$
	
	public static final String ATTR_REMOTE_HOSTID = PLUGIN_ID + ".REMOTE_HOSTID"; //$NON-NLS-1$

	public static final String DEFAULT_REMOTE_PEERID = null;
	
	public static final String DEFAULT_REMOTE_HOSTID = null;
	
}
