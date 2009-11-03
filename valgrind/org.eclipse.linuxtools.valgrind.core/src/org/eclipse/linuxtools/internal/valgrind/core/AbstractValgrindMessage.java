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

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public class AbstractValgrindMessage implements IValgrindMessage {

	protected IValgrindMessage parent;
	protected ILaunch launch;
	protected ArrayList<IValgrindMessage> children;
	protected String text;

	public AbstractValgrindMessage(IValgrindMessage parent, String text, ILaunch launch) {
		children = new ArrayList<IValgrindMessage>();
		this.parent = parent;
		this.text = text;
		this.launch = launch;
		
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public void addChild(IValgrindMessage message) {
		children.add(message);
	}

	public ILaunch getLaunch() {
		return launch;
	}
	
	public IValgrindMessage getParent() {
		return parent;
	}

	public IValgrindMessage[] getChildren() {
		return children.toArray(new IValgrindMessage[children.size()]);
	}

	public String getText() {
		return text;
	}

}