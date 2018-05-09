/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
