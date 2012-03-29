/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core.model;


/**
 * A class which represents an image (executables,
 * libraries, modules) profile by OProfile.
 */
public class OpModelImage {
	public static final int IMAGE_PARSE_ERROR = -1;
	
	//The count of all samples from this image
	private int _count;
	
	//the count for all dependent images -- needed?
	private int _depcount;
	
	//The name of this image (the full path, where applicable)
	private String _name;
	
	//The symbols profiled in this image
	private OpModelSymbol[] _symbols;
	
	//Any dependent images on this image (usually shared libs, kernel modules)
	private OpModelImage[] _dependents;
	
	private String _printTabs = "";		//for nice output //$NON-NLS-1$
	
	public OpModelImage() {
		_name = ""; //$NON-NLS-1$
		_count = 0;
		_depcount = 0;
		_symbols = null;
		_dependents = null;
	}
	
	public int getCount() {
		return _count;
	}
	
	public int getDepCount() {
		return _depcount;
	}

	public String getName() {
		return _name;
	}

	public OpModelSymbol[] getSymbols() {
		return _symbols;
	}

	public OpModelImage[] getDependents() {
		return _dependents;
	}

	public boolean hasDependents() {
		return (_dependents == null || _dependents.length == 0 ? false : true);
	}
	
	/**
	 * This method is not meant to be called publicly, used only 
	 * from the XML processors	
	 * @param _count
	 */
	public void _setCount(int _count) {
		this._count = _count;
	}
	
	/**
	 * This method is not meant to be called publicly, used only 
	 * from the XML processors	
	 * @param _depcount
	 */
	public void _setDepCount(int _depcount) {
		this._depcount = _depcount;
	}

	/**
	 * This method is not meant to be called publicly, used only 
	 * from the XML processors	
	 * @param _name
	 */
	public void _setName(String _name) {
		this._name = _name;
	}

	/**
	 * This method is not meant to be called publicly, used only 
	 * from the XML processors	
	 * @param _symbols
	 */
	public void _setSymbols(OpModelSymbol[] _symbols) {
		this._symbols = _symbols;
	}

	/**
	 * This method is not meant to be called publicly, used only 
	 * from the XML processors	
	 * @param _dependents
	 */
	public void _setDependents(OpModelImage[] _dependents) {
		this._dependents = _dependents;
	}

	public String toString(String tabs) {
		_printTabs = tabs;
		String s = toString();
		_printTabs = ""; //$NON-NLS-1$
		return s;
	}

	@Override
	public String toString() {
		String s = _name + ", Count: " + _count + (_depcount !=0 ? ", Dependent Count: " + _depcount + "\n" : "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (_symbols != null) {
			for (int i = 0; i < _symbols.length; i++) {
				s += _printTabs + "Symbols: "; //$NON-NLS-1$
				s += _symbols[i].toString(_printTabs + "\t"); //$NON-NLS-1$
			}
		}
		if (_dependents != null) {
			for (int i = 0; i < _dependents.length; i++) {
				s += _printTabs + "Dependent Image: "; //$NON-NLS-1$
				s += _dependents[i].toString(_printTabs + "\t"); //$NON-NLS-1$
			}			
		}
		return s;
	}
}
