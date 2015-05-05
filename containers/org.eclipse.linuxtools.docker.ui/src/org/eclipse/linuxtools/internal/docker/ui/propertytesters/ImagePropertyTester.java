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

package org.eclipse.linuxtools.internal.docker.ui.propertytesters;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * @author jjohnstn
 *
 */
public class ImagePropertyTester extends PropertyTester {

	/** Property name to check if a given {@link IDockerImage} can be tagged. */
	public static final String CAN_BE_TAGGED = "canBeTagged"; //$NON-NLS-1$
	public static final String HAS_MULTIPLE_TAGS = "hasMultipleTags"; //$NON-NLS-1$

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof IDockerImage) {
			final IDockerImage image = (IDockerImage) receiver;
			switch (property) {
			case CAN_BE_TAGGED:
				return !image.isDangling() && !image.isIntermediateImage();
			case HAS_MULTIPLE_TAGS:
				List<String> repoTags = image.repoTags();
				return repoTags != null && repoTags.size() > 1;
			}
		}
		return false;
	}

}
