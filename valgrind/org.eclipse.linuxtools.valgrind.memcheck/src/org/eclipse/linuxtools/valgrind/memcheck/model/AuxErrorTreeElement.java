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
import org.eclipse.linuxtools.valgrind.memcheck.ValgrindStackFrame;
import org.eclipse.swt.graphics.Image;

public class AuxErrorTreeElement extends ValgrindTreeElement {
	protected ValgrindError error;
	
	public AuxErrorTreeElement(ValgrindTreeElement parent, ValgrindError error) {
		this.parent = parent;
		this.error = error;
		children = new ArrayList<ValgrindTreeElement>();
		
		ArrayList<ValgrindStackFrame> frames = error.getAuxFrames();
		if (frames != null) {
			createStackElements(frames);
		}
	}

	@Override
	public Image getImage() {
		return ERROR_IMG;
	}

	@Override
	public String getText() {
		return error.getAuxWhat();
	}
}
