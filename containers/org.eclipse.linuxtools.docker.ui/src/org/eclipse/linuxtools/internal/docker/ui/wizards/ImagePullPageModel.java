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

import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * 
 */
public class ImagePullPageModel extends BaseDatabindingModel {

	public static final String IMAGE_NAME = "imageName";

	private String imageName;

	public String getImageName() {
		return imageName;
	}

	public void setImageName(final String imageName) {
		firePropertyChange(IMAGE_NAME, this.imageName, this.imageName = imageName);
	}
}
