/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 

package org.eclipse.linuxtools.internal.oprofile.core.model;

/**
 * This class represents a debugging symbol, the symbol output
 *  from opxml. If a symbol exists, it must have samples (which are
 *  OpModelSamples), although those samples may or may not have 
 *  complete debug info.
 */
public class OpModelSymbol {
	private String _name;
	private String _file;
	private int _line;
	private int _count;
	private OpModelSample[] _samples;
	private String _printTabs = ""; 	//for nice output //$NON-NLS-1$
	
	public OpModelSymbol() {
		_name = ""; //$NON-NLS-1$
		_file = ""; //$NON-NLS-1$
		_count = 0;
		_samples = null;
	}
	
	public void _setName(String _name) {
		this._name = _name;
	}

	public void _setFilePath(String _file) {
		this._file = _file;
	}

	public void setLine(int _line){
		this._line = _line;
	}

	public void _setCount(int _count) {
		this._count = _count;
	}

	public void _setSamples(OpModelSample[] _samples) {
		this._samples = _samples;
	}

	public String getName() {
		return _name;
	}

	public String getFilePath() {
		return _file;
	}

	public int getLine(){
		return _line;
	}

	public int getCount() {
		return _count;
	}

	public OpModelSample[] getSamples() {
		return _samples;
	}

	public String toString(String tabs) {
		_printTabs = tabs;
		String s = toString();
		_printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = _name + ", File: " + _file + ", Count: " + _count + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (_samples != null) {
			for (int i = 0; i < _samples.length; i++) {
				s += _printTabs + "Sample: "; //$NON-NLS-1$
				s += _samples[i].toString();
			}
		}
		return s;
	}
}
