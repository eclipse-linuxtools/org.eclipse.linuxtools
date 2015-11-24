package org.eclipse.linuxtools.internal.docker.ui.validators;

import java.util.regex.Matcher;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;

/**
 * Validates that the image name matches
 * [REGISTRY_HOST[:REGISTRY_PORT]/]IMAGE_NAME[:TAG]
 */
public class ImageNameValidator implements IValidator {

	@Override
	public IStatus validate(final Object value) {
		final String imageName = (String) value;
		if (imageName.isEmpty()) {
			return ValidationStatus
					.cancel(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		}
		final Matcher matcher = DockerImage.imageNamePattern
				.matcher(imageName);
		if (!matcher.matches()) {
			return ValidationStatus.warning(WizardMessages
					.getString("ImagePull.name.invalidformat.msg")); //$NON-NLS-1$
		} else if (matcher.group("tag") == null) { //$NON-NLS-1$
			return ValidationStatus.warning(
					WizardMessages.getString("ImagePull.assumeLatest.msg")); //$NON-NLS-1$

		}
		return Status.OK_STATUS;
	}

}