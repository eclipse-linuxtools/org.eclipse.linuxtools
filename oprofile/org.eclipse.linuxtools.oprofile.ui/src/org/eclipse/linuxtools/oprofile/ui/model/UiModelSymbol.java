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
	
	/**
	 * Constructor to the UiModelSymbol class
	 * @param parent The parent element
	 * @param symbol The debugging symbol node object in the data model
	 * @param totalCount The total count of samples for the parent session
	 */
	public UiModelSymbol(IUiModelElement parent, OpModelSymbol symbol, int totalCount) {
		this.parent = parent;
		this.symbol = symbol;
		this.samples = null;
		this.totalCount = totalCount;
		refreshModel();
	}	
	
	/**
	 * Creates the ui samples from the data model
	 */
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
	/**
	 * Return the file path for the current debugging symbol
	 * @return the file path
	 */
	public String getFileName() {
		return symbol.getFilePath();
	}
	
	/**
	 * Return the debugging symbol function name
	 * @return the function name
	 */
	public String getFunctionName(){
		return symbol.getName();
	}

	/**
	 * Return the debugging symbol line number
	 * @return the line number
	 */
	public int getLineNumber(){
		return symbol.getLine();
	}

	/** IUiModelElement functions
	 * Returns the text to display in the tree viewer as required by the label provider.
	 * @return text describing this element
	 */
	public String getLabelText() {
		return toString();
	}

	/**
	 * Returns the children of this element.
	 * @return An array of child elements or null
	 */
	public IUiModelElement[] getChildren() {
		return samples;
	}

	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	public boolean hasChildren() {
		return (samples == null || samples.length == 0 ? false : true);
	}

	/**
	 * Returns the element's parent.
	 * @return parent The parent element
	 */
	public IUiModelElement getParent() {
		return parent;
	}

	/**
	 * Returns the Image to display next to the text in the tree viewer.
	 * @return an Image object of the icon
	 */
	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.SYMBOL_ICON).createImage();
	}
}
