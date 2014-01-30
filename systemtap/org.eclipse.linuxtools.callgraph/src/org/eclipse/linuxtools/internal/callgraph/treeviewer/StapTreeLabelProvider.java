/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.treeviewer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.StapData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class StapTreeLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		Image im = null;
		if ( ((StapData) element).isMarked()) {
			im = AbstractUIPlugin.imageDescriptorFromPlugin(CallGraphConstants.PLUGIN_ID, "/icons/public_co.gif").createImage(); //$NON-NLS-1$
		} else {
			im = AbstractUIPlugin.imageDescriptorFromPlugin(CallGraphConstants.PLUGIN_ID, "/icons/compare_method.gif").createImage(); //$NON-NLS-1$
		}
		return im;
	}

	@Override
	public String getText(Object element) {
		return ((StapData) element).timesCalled + ": " + ((StapData) element).name; //$NON-NLS-1$
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// Empty
	}


	@Override
	public void dispose() {
		// Empty
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// Empty
	}

}
