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

import org.eclipse.linuxtools.valgrind.memcheck.ValgrindStackFrame;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public abstract class ValgrindTreeElement {
	protected static final Image ERROR_IMG = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
	
	protected ValgrindTreeElement parent;
	protected ArrayList<ValgrindTreeElement> children;

	public ValgrindTreeElement getParent() {
		return parent;
	}

	public ValgrindTreeElement[] getChildren() {
		return children.toArray(new ValgrindTreeElement[children.size()]);
	}

	public abstract String getText();
	
	public abstract Image getImage();

	protected void createStackElements(ArrayList<ValgrindStackFrame> frames) {
		if (frames.size() > 0) {
			children.add(new StackFrameTreeElement(this, frames.get(0), true));
			for (int i = 1; i < frames.size(); i++) {
				children.add(new StackFrameTreeElement(this, frames.get(i)));
			}
		}
	}
}
