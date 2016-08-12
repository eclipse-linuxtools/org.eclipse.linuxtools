/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.regex.Pattern;

import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Base {@link BaseDatabindingModel} for Push and Pull models.
 */
public abstract class ImagePullPushPageModel extends BaseDatabindingModel {

	public static final String SELECTED_IMAGE_NAME = "selectedImageName"; //$NON-NLS-1$

	public static final String SELECTED_REGISTRY = "selectedRegistry"; //$NON-NLS-1$

	public static final String TAG = "tag"; //$NON-NLS-1$

	private static final Pattern PATTERN = Pattern.compile("(.*)@(.*)"); //$NON-NLS-1$

	private IRegistry selectedRegistry;

	private String selectedImageName;

	public ImagePullPushPageModel() {

	}

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the selected {@link IDockerImage}
	 * @param selectedImageName
	 *            the selected repo/name/tag of the given image
	 */
	public ImagePullPushPageModel(final String selectedImageName) {
		setSelectedImageName(selectedImageName);
	}

	public IRegistry getSelectedRegistry() {
		return this.selectedRegistry;
	}

	public void setSelectedRegistry(final IRegistry selectedRegistry) {
		firePropertyChange(SELECTED_REGISTRY, this.selectedRegistry,
				this.selectedRegistry = selectedRegistry);
	}

	public String getSelectedImageName() {
		return selectedImageName;
	}

	public void setSelectedImageName(final String selectedImageName) {
		firePropertyChange(SELECTED_IMAGE_NAME, this.selectedImageName,
				this.selectedImageName = selectedImageName);
	}

}
