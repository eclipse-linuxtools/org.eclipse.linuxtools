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

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyImageNode;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;
import org.eclipse.swt.graphics.Image;

/**
 * the {@link ILabelProvider} implementation fo the
 * {@link DockerImageHierarchyView}.
 */
public class DockerImageHierarchyLabelProvider
		extends DockerExplorerLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof IDockerImageHierarchyNode) {
			return super.getImage(
					((IDockerImageHierarchyNode) element).getElement());
		}
		return super.getImage(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IDockerImageHierarchyImageNode) {
			final IDockerImage image = ((IDockerImageHierarchyImageNode) element)
					.getElement();
			// we display all repo/tags in a single line
			return getStyledText(image);
		}
		if (element instanceof IDockerImageHierarchyNode) {
			return super.getStyledText(
					((IDockerImageHierarchyNode) element).getElement());
		}
		return super.getStyledText(element);
	}

	/**
	 * @param image
	 *            the {@link IDockerImage} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(final IDockerImage image) {
		final Map<String, List<String>> imageTagsByRepo = DockerImage
				.extractTagsByRepo(image.repoTags());
		final List<String> imageRepos = new ArrayList<>(
				imageTagsByRepo.keySet());
		Collections.sort(imageRepos);
		final StyledString result = new StyledString();
		imageRepos.forEach(repo -> {
			result.append(repo);
			final List<String> tags = imageTagsByRepo.get(repo);
			final String joinedTags = tags.stream()
					.collect(Collectors.joining(", ")); //$NON-NLS-1$
			result.append(':');
			result.append(joinedTags, StyledString.COUNTER_STYLER); // $NON-NLS-1$
			result.append(' ');
		});
		// TODO: remove the cast to 'DockerImage' once the 'shortId()'
		// method is in the public API
		result.append('(', StyledString.QUALIFIER_STYLER) // $NON-NLS-1$
				.append(((DockerImage) image).shortId(), StyledString.QUALIFIER_STYLER).append(')', StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
		return result;
	}

}
