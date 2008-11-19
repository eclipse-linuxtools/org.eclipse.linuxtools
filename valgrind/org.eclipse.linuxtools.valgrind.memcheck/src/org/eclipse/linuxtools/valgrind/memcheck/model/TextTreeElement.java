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
package org.eclipse.linuxtools.valgrind.memcheck.model;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

public class TextTreeElement extends ValgrindTreeElement {
	
	protected String text;
	
	public TextTreeElement(ValgrindTreeElement parent, String text) {
		this.parent = parent;
		this.text = text;
		children = new ArrayList<ValgrindTreeElement>();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

}
