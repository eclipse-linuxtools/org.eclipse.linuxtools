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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSymbol;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Children of images in the view -- a function name in the profiled 
 *  image's source code. May or may not have child samples.
 */
public class UiModelSymbol implements IUiModelElement {
	private IUiModelElement parent;	//parent element
	private OpModelSymbol symbol;		//the node in the data model
	private UiModelSample samples[];	//this node's children
	private int totalCount;			//total count of samples for the parent session
	
	public UiModelSymbol(IUiModelElement parent, OpModelSymbol symbol, int totalCount) {
		this.parent = parent;
		this.symbol = symbol;
		this.samples = null;
		this.totalCount = totalCount;
		refreshModel();
	}	
	
	private void refreshModel() {
		ArrayList<UiModelSample> sampleList = new ArrayList<UiModelSample>();
		OpModelSample dataModelSamples []= symbol.getSamples();
		
		for (int i = 0; i < dataModelSamples.length; i++) {
			//dont display samples with line number of 0, meaning no line number
			// was correlated, more likely that no source file exists
			if (dataModelSamples[i].getLine() != 0) {
				sampleList.add(new UiModelSample(this, dataModelSamples[i], totalCount));
			}
		}
		
		samples = new UiModelSample[sampleList.size()];
		sampleList.toArray(samples);
	}
	
	@Override
	public String toString() {
		double countPercentage = (double)symbol.getCount() / (double)totalCount;
		String percentage = OprofileUiPlugin.getPercentageString(countPercentage);
		
		//a hack to get `basename` type functionality
		String fileName = (new File(symbol.getFilePath())).getName();

		return percentage + " " + OprofileUiMessages.getString("uimodel.percentage.in") + symbol.getName() + (fileName.length() == 0 ? "" : " [" + fileName + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	public String getFileName() {
		return symbol.getFilePath();
	}
	
	public String getFunctionName(){
		return symbol.getName();
	}

	public int getLineNumber(){
		return symbol.getLine();
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}

	public IUiModelElement[] getChildren() {
		return samples;
	}

	public boolean hasChildren() {
		return (samples == null || samples.length == 0 ? false : true);
	}

	public IUiModelElement getParent() {
		return parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.SYMBOL_ICON).createImage();
	}
}
