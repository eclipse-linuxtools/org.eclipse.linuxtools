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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class ValgrindStubProcess implements IProcess {
	protected Map<String, String> attributes;
	protected ILaunch launch;
	protected String label;
	protected IStreamsProxy streamsProxy;
	
	public ValgrindStubProcess(ILaunch launch, String label) {
		attributes = new HashMap<String, String>();
		streamsProxy = new ValgrindStubStreamsProxy();
		this.launch = launch;
		this.label = label;
		
		launch.addProcess(this);
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public int getExitValue() {
		return 0;
	}

	public String getLabel() {
		return label;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public IStreamsProxy getStreamsProxy() {
		return streamsProxy;
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean canTerminate() {
		return true;
	}

	public boolean isTerminated() {
		return true;
	}

	public void terminate() {
	}

}
