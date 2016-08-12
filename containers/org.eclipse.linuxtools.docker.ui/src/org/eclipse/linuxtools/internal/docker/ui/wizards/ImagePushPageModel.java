/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * {@link BaseDatabindingModel} for the {@link ImagePushPage}
 */
public class ImagePushPageModel extends ImagePullPushPageModel {

	public static final String FORCE_TAGGING = "forceTagging"; //$NON-NLS-1$

	public static final String KEEP_TAGGED_IMAGE = "keepTaggedImage"; //$NON-NLS-1$

	private final IDockerImage image;

	private boolean forceTagging;

	private boolean keepTaggedImage;

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the selected {@link IDockerImage}
	 * @param selectedImageName
	 *            the selected repo/name/tag of the given image
	 */
	public ImagePushPageModel(final IDockerImage image,
			final String selectedImageName) {
		super(selectedImageName);
		this.image = image;
	}

	public IDockerImage getImage() {
		return this.image;
	}

	public boolean isForceTagging() {
		return this.forceTagging;
	}

	public void setForceTagging(final boolean forceTagging) {
		firePropertyChange(FORCE_TAGGING, this.forceTagging,
				this.forceTagging = forceTagging);
	}

	public boolean isKeepTaggedImage() {
		return this.keepTaggedImage;
	}

	public void setKeepTaggedImage(final boolean keepTaggedImage) {
		firePropertyChange(KEEP_TAGGED_IMAGE, this.keepTaggedImage,
				this.keepTaggedImage = keepTaggedImage);
	}

	class ImagePushValidator extends MultiValidator {

		private final IObservableValue<String> imageNameObservable;

		private final IObservableValue<IRegistry> registryAccountObservable;

		public ImagePushValidator(
				final IObservableValue<String> imageNameObservable,
				final IObservableValue<IRegistry> registryAccountObservable) {
			this.imageNameObservable = imageNameObservable;
			this.registryAccountObservable = registryAccountObservable;
		}

		@Override
		protected IStatus validate() {
			final String selectedImageName = imageNameObservable.getValue();
			final IRegistry selectedRegistryAcccount = registryAccountObservable
					.getValue();
			if (selectedImageName == null || selectedImageName.isEmpty()) {
				return ValidationStatus
						.error(WizardMessages.getString("ImagePush.desc")); //$NON-NLS-1$
			} else if (selectedRegistryAcccount == null) {
				return ValidationStatus.error(WizardMessages
						.getString("ImagePushPage.empty.registry.account")); //$NON-NLS-1$
			} else if (ImagePushPageModel.this.image.repoTags() == null
					|| !ImagePushPageModel.this.image.repoTags()
							.contains(selectedImageName)) {
				return ValidationStatus.warning(WizardMessages
						.getString("ImagePushPage.repotag.unknown.msg")); //$NON-NLS-1$
			}
			return ValidationStatus.info(WizardMessages
					.getString("ImagePushPage.repotag.default.msg")); //$NON-NLS-1$
		}

		@Override
		public IObservableList<?> getTargets() {
			final WritableList<IObservableValue<?>> targets = new WritableList<>();
			targets.add(this.imageNameObservable);
			targets.add(this.registryAccountObservable);
			return targets;
		}
	}

}
