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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;

/**
 * {@link Wizard} to push an {@link IDockerImage} to a registry
 */
public class ImagePush extends Wizard {

	private ImagePushPage mainPage;
	private final IDockerImage image;
	private final String defaultImageName;

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the image to push
	 * @param defautImageName
	 *            the default tag to use to push the image
	 */
	public ImagePush(final IDockerImage image, final String defautImageName) {
		super();
		setWindowTitle(WizardMessages.getString("ImagePush.name")); //$NON-NLS-1$
		this.image = image;
		this.defaultImageName = defautImageName;
	}

	/**
	 * @return the image name that was selected when opening this wizard.
	 */
	public IDockerImage getImage() {
		return this.image;
	}

	/**
	 * @return the image name that was initially selected when opening this
	 *         wizard.
	 */
	public String getDefaultImageName() {
		return this.defaultImageName;
	}

	/**
	 * <p>
	 * <strong>Note:</strong> If no tag was specified, then <code>latest</code>
	 * is set.
	 * </p>
	 * 
	 * @return the repository/tag to select/apply on the image.
	 */
	public String getSelectedImageTag() {
		final String selectedImageName = this.mainPage.getSelectedImageName();
		return DockerImage.setDefaultTagIfMissing(selectedImageName);
	}

	/**
	 * @return the target {@link IRegistry} on which to push the image
	 */
	public IRegistry getRegistry() {
		return this.mainPage.getSelectedRegistryAccount();
	}

	/**
	 * @return flag to indicate if the 'force' option should be used when
	 *         tagging the image.
	 */
	public boolean isForceTagging() {
		return this.mainPage.isForceTagging();
	}

	/**
	 * @return flag to indicate if the tagged image should be kept upon
	 *         completion.
	 */
	public boolean isKeepTaggedImage() {
		return this.mainPage.isKeepTaggedImage();
	}

	@Override
	public void addPages() {
		this.mainPage = new ImagePushPage(this.image, this.defaultImageName);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return this.mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}


}
