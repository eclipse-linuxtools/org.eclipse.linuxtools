/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core.model;

/**
 * Represents an OProfile sample.
 */
public class OpModelSample {
	private int _count;
	private int _line;

	public OpModelSample() {
		_count = 0;
		_line = 0;
	}
	
	public void _setCount(int _count) {
		this._count = _count;
	}

	public void _setLine(int _line) {
		this._line = _line;
	}

	public int getCount() {
		return _count;
	}

	public int getLine() {
		return _line;
	}

	@Override
	public String toString() {
		String s = "Line #: " + _line + ", Count: " + _count + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return s;
	}
}
