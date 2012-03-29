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
package org.eclipse.linuxtools.internal.oprofile.ui.model;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSymbol;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Children of sessions in the view -- the binary which was profiled. 
 * May or may not have child symbols. Note that although the dependent
 * images are children of OpModelImages in the data model, for usability's
 * sake they are children of the parent session in the tree.
 */
public class UiModelImage implements IUiModelElement {
	private IUiModelElement _parent;		//parent element, may be UiModelSession or UiModelDependent
	private OpModelImage _image;			//the node in the data model
	private UiModelSymbol _symbols[];		//this node's child (symbols)
	private int _totalCount;				//total number of samples 
	private int _depCount;					//number of samples from dependent images

	public UiModelImage(IUiModelElement parent, OpModelImage image, int totalCount, int depCount) {
		_parent = parent;
		_image = image;
		_symbols = null;
		_totalCount = totalCount;
		_depCount = depCount;
		refreshModel();
	}

	private void refreshModel() {
		OpModelSymbol[] dataModelSymbols = _image.getSymbols();
		
		//dependent images may not have symbols
		if (dataModelSymbols != null) {
			_symbols = new UiModelSymbol[dataModelSymbols.length];
	
			for (int i = 0; i < dataModelSymbols.length; i++) {
				_symbols[i] = new UiModelSymbol(this, dataModelSymbols[i], _totalCount);
			}
		}
	}
	
	@Override
	public String toString() {
		if (_image.getCount() == OpModelImage.IMAGE_PARSE_ERROR) {
			return OprofileUiMessages.getString("opxmlParse.error.multipleImages"); //$NON-NLS-1$
		} else {
			double countPercentage = (double)(_image.getCount() - _depCount) / (double)_totalCount;
			String percentage = OprofileUiPlugin.getPercentageString(countPercentage);
			
			return percentage + " " + OprofileUiMessages.getString("uimodel.percentage.in") + _image.getName(); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}

	public IUiModelElement[] getChildren() {
		IUiModelElement children[] = null;
		
		if (_symbols != null) {
			children = new IUiModelElement[_symbols.length];
			
			for (int i = 0; i < _symbols.length; i++) {
				children[i] = _symbols[i];
			}
		}
		
		return children;
	}

	public boolean hasChildren() {
		return (_symbols == null || _symbols.length == 0 ? false : true);
	}

	public IUiModelElement getParent() {
		return _parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.IMAGE_ICON).createImage();
	}
}
