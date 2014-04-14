/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;


public class ValgrindError extends AbstractValgrindMessage {
	protected int pid;
	
	public ValgrindError(IValgrindMessage message, String text, ILaunch launch, int pid) {
		super(message, text, launch);
		this.pid = pid;
	}
	
	@Override
	public String getText() {
		return super.getText() + " [PID: " + pid + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
