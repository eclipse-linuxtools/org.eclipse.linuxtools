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
package org.eclipse.linuxtools.internal.valgrind.tests;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

public class ValgrindStubStreamsProxy implements IStreamsProxy {

	@Override
	public IStreamMonitor getErrorStreamMonitor() {
		return null;
	}

	@Override
	public IStreamMonitor getOutputStreamMonitor() {
		return null;
	}

	@Override
	public void write(String input) {
	}
}
