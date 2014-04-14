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

import java.util.Arrays;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * @since 1.1
 */
public class UiModelDependent implements IUiModelElement {
	private IUiModelElement parent;
	private OpModelImage dataModelDependents[];
	private UiModelImage dependents[];
	private int totalCount;
	private int depCount;

	/**
	 * Constructor to this UiModelDependent class
	 * @param parent The parent element
	 * @param dependents The dependent images
	 * @param totalCount The total count of samples for the parent session
	 * @param depCount The count for all dependent images
	 */
	public UiModelDependent(IUiModelElement parent, OpModelImage dependents[], int totalCount, int depCount) {
		this.parent = parent;
		this.dataModelDependents = dependents;
		this.dependents = null;
		this.totalCount = totalCount+depCount;
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

		return percentage + " " + OprofileUiMessages.getString("uimodel.percentage.in")+" " + OprofileUiMessages.getString("uimodel.dependent.dependent.images"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/** IUiModelElement functions **/
	@Override
	public String getLabelText() {
		return toString();
	}

	/**
	 * Returns the children of this element.
	 * @return An array of child elements or null
	 */
	@Override
	public IUiModelElement[] getChildren() {

		if (UiModelRoot.SortType.LIB == UiModelRoot.getSortingType()) {
			Arrays.sort(dependents, UiModelSorting.getInstance());
			return dependents;
		}

		return dependents;
	}

	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	@Override
	public boolean hasChildren() {
		return true;	//must have children, or this object wouldn't be created
	}

	/**
	 * Returns the parent element.
	 * @return the parent element or null
	 */
	@Override
	public IUiModelElement getParent() {
		return parent;
	}

	/**
	 * Returns the Image to display next to the text in the tree viewer.
	 * @return an Image object of the icon
	 */
	@Override
	public Image getLabelImage() {
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.DEPENDENT_ICON).createImage();
	}
}
