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
 * @since 1.1
 */
public class UiModelImage implements IUiModelElement {
	private IUiModelElement parent;		//parent element, may be UiModelSession or UiModelDependent
	private OpModelImage image;			//the node in the data model
	private UiModelSymbol symbols[];		//this node's child (symbols)
	private int totalCount;				//total number of samples
	private int depCount;					//number of samples from dependent images

	/**
	 * Constructor to the UiModelImage class
	 * @param parent The parent element
	 * @param image The image node object in the data model
	 * @param totalCount The total number of samples
	 * @param depCount The number of samples from dependent images
	 */
	public UiModelImage(IUiModelElement parent, OpModelImage image, int totalCount, int depCount) {
		this.parent = parent;
		this.image = image;
		this.symbols = null;
		this.totalCount = totalCount+depCount;//totalCount;
		this.depCount = depCount;
		refreshModel();
	}
	/**
	 * Create the ui symbols from the data model.
	 */
	private void refreshModel() {
		OpModelSymbol[] dataModelSymbols = image.getSymbols();

		//dependent images may not have symbols
		if (dataModelSymbols != null) {
			symbols = new UiModelSymbol[dataModelSymbols.length];

			for (int i = 0; i < dataModelSymbols.length; i++) {
				symbols[i] = new UiModelSymbol(this, dataModelSymbols[i], totalCount);
			}
		}
	}

	@Override
	public String toString() {
		if (image.getCount() == OpModelImage.IMAGE_PARSE_ERROR) {
			return OprofileUiMessages.getString("opxmlParse.error.multipleImages"); //$NON-NLS-1$
		} else {
			double countPercentage = (double)(image.getCount() ) / (double)totalCount;
			String percentage = OprofileUiPlugin.getPercentageString(countPercentage);

			return percentage + " " + OprofileUiMessages.getString("uimodel.percentage.in") + image.getName(); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
		IUiModelElement children[] = null;

		if (symbols != null) {
			children = new IUiModelElement[symbols.length];

			for (int i = 0; i < symbols.length; i++) {
				children[i] = symbols[i];
			}
		}

		return children;
	}
	/**
	 * Returns if the element has any children.
	 * @return true if the element has children, false otherwise
	 */
	@Override
	public boolean hasChildren() {
		return (symbols == null || symbols.length == 0 ? false : true);
	}

	/**
	 * Returns the element's parent.
	 * @return parent The parent element or null
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
		return OprofileUiPlugin.getImageDescriptor(OprofileUiPlugin.IMAGE_ICON).createImage();
	}
}
