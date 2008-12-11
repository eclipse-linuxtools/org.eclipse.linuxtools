/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.configuration;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.oprofile.core.daemon.OpEvent;
import org.eclipse.swt.graphics.Image;

/**
 * This class is a label provider for OpEvents. Used by the launcher.
 */
public class OpEventLabelProvider implements ILabelProvider {

	public Image getImage(Object arg0) {
		return null;
	}

	public String getText(Object element) {
		OpEvent e = (OpEvent) element;
		return e.getText();
	}

	public void addListener(ILabelProviderListener arg0) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
	}

}
