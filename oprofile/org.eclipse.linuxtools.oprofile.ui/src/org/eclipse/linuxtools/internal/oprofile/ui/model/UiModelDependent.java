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
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

public class UiModelDependent implements IUiModelElement {
	private IUiModelElement parent;
	private OpModelImage dataModelDependents[];
	private UiModelImage dependents[];
	private int totalCount;
	private int depCount;
	
	public UiModelDependent(IUiModelElement parent, OpModelImage dependents[], int totalCount, int depCount) {
		this.parent = parent;
		this.dataModelDependents = dependents;
		this.dependents = null;
		this.totalCount = totalCount;
		this.depCount = depCount;
		refreshModel();
	}

	private void refreshModel() {
		dependents = new UiModelImage[dataModelDependents.length];
		
		for (int i = 0; i < dataModelDependents.length; i++) {
			dependents[i] = new UiModelImage(this, dataModelDependents[i], totalCount, 0);
		}
	}
	
	@Override
	public String toString() {
		double countPercentage = (double)depCount / (double)totalCount;
		String percentage = OprofileUiPlugin.getPercentageString(countPercentage);

		return percentage + " " + OprofileUiMessages.getString("uimodel.percentage.in") + OprofileUiMessages.getString("uimodel.dependent.dependent.images"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	}

	/** IUiModelElement functions **/
	public String getLabelText() {
		return toString();
	}

	public IUiModelElement[] getChildren() {
		return dependents;
	}

	public boolean hasChildren() {
		return true;	//must have children, or this object wouldn't be created
	}

	public IUiModelElement getParent() {
		return parent;
	}

	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.DEPENDENT_ICON).createImage();
	}
}
