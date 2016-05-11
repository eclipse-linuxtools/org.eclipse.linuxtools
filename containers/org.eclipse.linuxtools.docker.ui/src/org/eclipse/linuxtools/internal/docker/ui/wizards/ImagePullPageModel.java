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
	public static final String REGISTRY = "registry";

	private String imageName;
	private String registry;

	public String getImageName() {
		return imageName;
	}

	public String getRegistry() {
		return registry;
	}

	public void setImageName(final String imageName) {
		firePropertyChange(IMAGE_NAME, this.imageName, this.imageName = imageName);
	}

	public void setRegistry(final String registry) {
		firePropertyChange(REGISTRY, this.registry, this.registry = registry);
	}
}
