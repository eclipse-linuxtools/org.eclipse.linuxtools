/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified to use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

public interface IOSIORestConstants {
	public static final String EDITOR_TYPE_ASSIGNEES = "osio.editor.assignees"; //$NON-NLS-1$
	
	public static final String EDITOR_TYPE_LABELS = "osio.editor.labels"; //$NON-NLS-1$

	public static final String EDITOR_TYPE_KEYWORD = "osio.editor.keyword"; //$NON-NLS-1$

	public static final String WORKITEM_STATUS = "system.state"; //$NON-NLS-1$

	public static final String REPOSITORY_AUTH_TOKEN = "osio.rest.authtoken"; //$NON-NLS-1$
	
	public static final String REPOSITORY_AUTH_ID = "osio.rest.userid"; //$NON-NLS-1$

	public static final String KIND_FLAG = "task.common.kind.flag"; //$NON-NLS-1$

	public static final String KIND_FLAG_TYPE = "task.common.kind.flag_type"; //$NON-NLS-1$

	public static final String EDITOR_TYPE_FLAG = "osio.editor.flag"; //$NON-NLS-1$
	
	public static final String RESOLVED = "resolved"; //$NON-NLS-1$
	
	public static final String CLOSED = "closed"; //$NON-NLS-1$
}
