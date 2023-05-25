/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.propertytesters;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyImageNode;

public class ImagePropertyTester extends PropertyTester {

	/** Property name to check if a given {@link IDockerImage} can be tagged. */
	public static final String CAN_BE_TAGGED = "canBeTagged"; //$NON-NLS-1$
	/**
	 * Property name to check if a given {@link IDockerImage} has multiple tags.
	 */
	public static final String HAS_MULTIPLE_TAGS = "hasMultipleTags"; //$NON-NLS-1$

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof IDockerImage image) {
			switch (property) {
			case CAN_BE_TAGGED:
				return !image.isDangling() && !image.isIntermediateImage();
			case HAS_MULTIPLE_TAGS:
				List<String> repoTags = image.repoTags();
				return repoTags != null && repoTags.size() > 1;
			}
		} else if (receiver instanceof IDockerImageHierarchyImageNode) {
			final IDockerImage image = ((IDockerImageHierarchyImageNode) receiver)
					.getElement();
			return test(image, property, args, expectedValue);
		}
		return false;
	}

}
