/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * @author jjohnstn
 *
 */
public class ImageInfoContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IDockerImage) {
			final IDockerImage image = (IDockerImage) inputElement;
			return new Object[] {
					new Object[] { "Id", image.id().substring(0, 12) }, //$NON-NLS-1$
					new Object[] { "ParentId", image.parentId() }, //$NON-NLS-1$
					new Object[] { "Created", image.createdDate() }, //$NON-NLS-1$
					new Object[] {
							"RepoTags", LabelUtils.reduce(image.repoTags()) }, //$NON-NLS-1$
					new Object[] { "Size", LabelUtils.toString(image.size()) }, //$NON-NLS-1$
					new Object[] {
							"VirtualSize", LabelUtils.toString(image.virtualSize()) }, //$NON-NLS-1$
					new Object[] {
							"IsIntermediateImage", LabelUtils.toString(image.isIntermediateImage()) }, //$NON-NLS-1$
					new Object[] {
							"IsDangling", LabelUtils.toString(image.isDangling()) }, //$NON-NLS-1$
			};
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		final Object propertyValue = ((Object[])parentElement)[1];
		final Object value = ((Object[])parentElement)[1];
		if(value instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Object> propertyValues = (List<Object>)propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			for (int i = 0; i < propertyValues.size(); i++) {
				result[i] = new Object[]{"", LabelUtils.toString(propertyValues.get(i))};
			}
			return result;
		} else if(value instanceof Object[]) {
			return (Object[])value;
		}
		return new Object[]{value};
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if(element instanceof Object[]) {
			return !(((Object[])element)[1] instanceof String);
		}
		return false;
	}

}
