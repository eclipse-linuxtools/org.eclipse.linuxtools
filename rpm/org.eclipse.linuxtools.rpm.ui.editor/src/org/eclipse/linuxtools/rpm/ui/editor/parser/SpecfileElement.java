/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;

public class SpecfileElement {
	private Specfile specfile;
	private String name;
	private int lineNumber;
	private int lineStartPosition;
	private int lineEndPosition;

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public SpecfileElement() {
		// weird
	}

	public SpecfileElement(String name) {
		setName(name);
	}

	public String getName() {
		return resolve(name);
	}

	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getLineEndPosition() {
		return lineEndPosition;
	}

	public void setLineEndPosition(int lineEndPosition) {
		this.lineEndPosition = lineEndPosition;
	}

	public int getLineStartPosition() {
		return lineStartPosition;
	}

	public void setLineStartPosition(int lineStartPosition) {
		this.lineStartPosition = lineStartPosition;
	}

	public Specfile getSpecfile() {
		return specfile;
	}

	public void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
	}

	public String resolve(String toResolve) {
		if (specfile == null || toResolve.equals("")) {//$NON-NLS-1$
			if (toResolve.length()>2 && toResolve.substring(2, toResolve.length() - 1).equals(name)) { 
				return toResolve;
			}
		}
		return UiUtils.resolveDefines(specfile, toResolve);
	}

}
