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

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

public class UiModelSample implements IUiModelElement {
	private IUiModelElement parent;		//parent element
	private OpModelSample sample;			//the node in the data model
	private int totalCount;				//total sample count for the parent session
	
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
	
	public int getLine() {
		return sample.getLine();
	}
	
	public String getFile(){
		return sample.getFilePath();
	}

	public double getCountPercentage() {
		return (double)sample.getCount() / (double)totalCount;
	}
	
	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}

	public IUiModelElement[] getChildren() {
		return null;
	}

	public boolean hasChildren() {
		return false;		//bottom level element
	}

	public IUiModelElement getParent() {
		return parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.SAMPLE_ICON).createImage();
	}
}
