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

package org.eclipse.linuxtools.internal.rpm.ui.editor.parser;

import java.text.MessageFormat;

public class SpecfilePatchMacro extends SpecfileMacro {
	private int patchNumber;
	private int patchLevel;
	// TODO:  add patchLevel functionality
	public SpecfilePatchMacro(int patchNumber) {
		super();
		this.patchNumber = patchNumber;
//		this.patchLevel = patchLevel;
	}
	public int getPatchLevel() {
		return patchLevel;
	}
	public void setPatchLevel(int patchLevel) {
		this.patchLevel = patchLevel;
	}
	public int getPatchNumber() {
		return patchNumber;
	}
	public void setPatchNumber(int patchNumber) {
		this.patchNumber = patchNumber;
	}
	@Override
	public String toString() {
		return MessageFormat.format("patch #{0} at level {1}", patchNumber, patchLevel); //$NON-NLS-1$
	}
}
