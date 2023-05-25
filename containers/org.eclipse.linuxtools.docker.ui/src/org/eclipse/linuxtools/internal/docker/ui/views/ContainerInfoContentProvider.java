/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.internal.docker.core.DockerPortMapping;

public class ContainerInfoContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if(inputElement instanceof IDockerContainer container) {
			return new Object[] {
					new Object[]{"Id", container.id().substring(0,  12)}, //$NON-NLS-1$
					new Object[]{"Image", container.image()}, //$NON-NLS-1$
					new Object[]{"Command", container.command()}, //$NON-NLS-1$
					new Object[]{"Created", LabelProviderUtils.toCreatedDate(container.created())}, //$NON-NLS-1$
					new Object[]{"Status", container.status()}, //$NON-NLS-1$
					new Object[]{"Ports", getPorts(container)}, //$NON-NLS-1$
					new Object[]{"Names", getNames(container)}, //$NON-NLS-1$
			};
		}
		return EMPTY;
	}

	/**
	 * @return the first {@link DockerPortMapping} if the given {@link IDockerContainer} has only one {@link DockerPortMapping}, an empty {@link String} if the given list is empty, otherwise the given list itself.  
	 * @param container the {@link IDockerContainer} of elements to analyze
	 */
	private Object getPorts(final IDockerContainer container) {
		if(container.ports().isEmpty()) {
			return "";
		} else if(container.ports().size() == 1) {
			return LabelProviderUtils.containerPortMappingToString(container.ports().get(0));
		} else {
			final List<String> ports = new ArrayList<>();
			for (IDockerPortMapping portMapping : container.ports()) {
				ports.add(LabelProviderUtils.containerPortMappingToString(portMapping));
			}
			return ports;
		}
	}

	/**
	 * @return the first name if the given {@link IDockerContainer} has only one, an empty {@link String} if the given list is empty, otherwise the given list itself.  
	 * @param container the {@link IDockerContainer} of elements to analyze
	 */
	private Object getNames(final IDockerContainer container) {
		if(container.names().isEmpty()) {
			return "";
		} else if(container.names().size() == 1) {
			return container.name();
		} else {
			return container.names();
		}
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
				result[i] = new Object[]{"", LabelProviderUtils.toString(propertyValues.get(i))};
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
