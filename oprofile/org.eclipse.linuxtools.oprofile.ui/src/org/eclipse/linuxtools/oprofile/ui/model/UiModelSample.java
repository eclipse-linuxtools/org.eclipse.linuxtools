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

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * @since 1.1
 */
public class UiModelSample implements IUiModelElement {
	private IUiModelElement parent;		//parent element
	private OpModelSample sample;			//the node in the data model
	private int totalCount;				//total sample count for the parent session
	
	/**
	 * Constructor to the UiModelSample class
	 * @param parent The parent element
	 * @param sample Oprofile sample node in the data model
	 * @param totalCount The total sample count for the parent session
	 */
	public UiModelSample(IUiModelElement parent, OpModelSample sample, int totalCount) {
		this.parent = parent;
		this.sample = sample;
		this.totalCount = totalCount;
	}
	
	@Override
	public String toString() {
		double countPercentage = (double)sample.getCount() / (double)totalCount;
		String percentage = OprofileUiPlugin.getPercentageString(countPercentage);
		
		return percentage + " " + OprofileUiMessages.getString("uimodel.sample.on.line") + Integer.toString(sample.getLine()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Return the sample line
	 * @return the sample line
	 */
	public int getLine() {
		return sample.getLine();
	}
	
	/**
	 * Return the path to the sample node
	 * @return node path
	 */
	public String getFile(){
		return sample.getFilePath();
	}

	/**
	 * Returns the count percentage for the sample node
	 * @return count percentage for the sample node
	 */
	public double getCountPercentage() {
		return (double)sample.getCount() / (double)totalCount;
	}
	
	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}

	/**
	 * Returns the children of this element.
	 * @return An array of child elements or null
	 */
	public IUiModelElement[] getChildren() {
		return null;
	}
	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	public boolean hasChildren() {
		return false;		//bottom level element
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
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.SAMPLE_ICON).createImage();
	}
}
