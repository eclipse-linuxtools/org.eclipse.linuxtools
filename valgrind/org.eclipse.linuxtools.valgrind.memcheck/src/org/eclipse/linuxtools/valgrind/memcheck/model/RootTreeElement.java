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

import org.eclipse.linuxtools.valgrind.memcheck.ValgrindError;
import org.eclipse.swt.graphics.Image;

public class RootTreeElement extends ValgrindTreeElement {

	public RootTreeElement(ValgrindError[] errors) {
		parent = null;
		children = new ArrayList<ValgrindTreeElement>(errors.length);
		for (ValgrindError error : errors) {
			children.add(new ErrorTreeElement(this, error));
		}
	}
	
	@Override
	public String getText() {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

}
