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

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.valgrind.memcheck.ValgrindStackFrame;
import org.eclipse.swt.graphics.Image;

public class StackFrameTreeElement extends ValgrindTreeElement {

	protected ValgrindStackFrame frame;
	protected boolean top;

	public StackFrameTreeElement(ValgrindTreeElement parent, ValgrindStackFrame frame, boolean top) {
		this.parent = parent;		
		this.frame = frame;
		this.top = top;
		children = new ArrayList<ValgrindTreeElement>();
	}

	public StackFrameTreeElement(ValgrindTreeElement parent, ValgrindStackFrame frame) {
		this(parent, frame, false);
	}

	@Override
	public String getText() {
		StringBuffer line = new StringBuffer();
		line.append(top ? Messages.getString("StackFrameTreeElement.at") : Messages.getString("StackFrameTreeElement.by")); //$NON-NLS-1$ //$NON-NLS-2$
		line.append(frame.getPC() + ": "); //$NON-NLS-1$
		if (frame.getObj() != null) {
			if (frame.getFunc() != null) {
				line.append(frame.getFunc() + " "); //$NON-NLS-1$
			}
			line.append("("); //$NON-NLS-1$
			if (frame.getFile() != null) {
				line.append(frame.getFile() + ":" + String.valueOf(frame.getLine())); //$NON-NLS-1$
			}
			else {
				line.append(Messages.getString("StackFrameTreeElement.within") + frame.getObj()); //$NON-NLS-1$
			}
			line.append(")"); //$NON-NLS-1$
		}
		else {
			line.append("???"); //$NON-NLS-1$
		}
		return line.toString();
	}

	@Override
	public Image getImage() {
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
	}
	
	public ValgrindStackFrame getFrame() {
		return frame;
	}

}
