/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.gcov.Activator;

/**
 * This action changes the content provider
 */

public class SwitchContentProviderAction extends Action {

	private final ColumnViewer viewer;
	private final IContentProvider provider;

	public SwitchContentProviderAction(String name, String iconPath, ColumnViewer viewer, IContentProvider provider) {
		super(name, AS_RADIO_BUTTON);
		this.setImageDescriptor(ResourceLocator.imageDescriptorFromBundle(Activator.PLUGIN_ID, iconPath).get());
		this.setToolTipText(name);
		this.viewer = viewer;
		this.provider = provider;
	}

	@Override
	public void run() {
		viewer.getControl().setRedraw(false);
		viewer.setContentProvider(provider);
		((TreeViewer) viewer).expandToLevel(2);
		viewer.getControl().setRedraw(true);
	}
}