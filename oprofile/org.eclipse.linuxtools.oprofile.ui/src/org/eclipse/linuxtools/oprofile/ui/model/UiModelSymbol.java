/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.linuxtools.oprofile.core.model.OpModelSymbol;
import org.eclipse.swt.graphics.Image;

/**
 * Children of images in the view -- a function name in the profiled 
 *  image's source code. May or may not have child samples.
 */
public class UiModelSymbol implements IUiModelElement {
	private IUiModelElement _parent;	//parent element
	private OpModelSymbol _symbol;		//the node in the data model
	private UiModelSample _samples[];	//this node's children
	private int _totalCount;			//total count of samples for the parent session
	private double _countPercentage;	//percentage of samples under this symbol
	
	public UiModelSymbol(IUiModelElement parent, OpModelSymbol symbol, int totalCount) {
		_parent = parent;
		_symbol = symbol;
		_samples = null;
		_totalCount = totalCount;
		refreshModel();
	}	
	
	private void refreshModel() {
		_countPercentage = (double)_symbol.getCount() / (double)_totalCount;
		//TODO
	}
	
	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getPercentInstance();
		if (nf instanceof DecimalFormat) {
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
		}
		
		String percentage;
		if (_countPercentage < 0.0001) {
			percentage = "<" + nf.format(0.0001);
		} else {
			percentage = nf.format(_countPercentage);
		}

		return percentage + " in " + _symbol.getName() + ", from file " + _symbol.getFile();
	}

	/** IUiModelElement functions **/
	@Override
	public String getLabelText() {
		return toString();
	}

	@Override
	public IUiModelElement[] getChildren() {
		return _samples;
	}

	@Override
	public boolean hasChildren() {
		return (_samples == null || _samples.length == 0 ? false : true);
	}

	@Override
	public IUiModelElement getParent() {
		return _parent;
	}

	@Override
	public Image getLabelImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
