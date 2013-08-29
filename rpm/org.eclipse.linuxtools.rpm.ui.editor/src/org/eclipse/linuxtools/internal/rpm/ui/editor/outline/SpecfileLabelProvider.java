/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfilePreamble;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackageContainer;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.graphics.Image;

public class SpecfileLabelProvider implements ILabelProvider {

	private static final String PREAMBLE_ICON="icons/preamble_obj.gif"; //$NON-NLS-1$
	private static final String SECTION_ICON="icons/section_obj.gif"; //$NON-NLS-1$
	private static final String PACKAGES_ICON="icons/packages_obj.gif"; //$NON-NLS-1$
	private static final String PACKAGE_ICON="icons/package_obj.gif"; //$NON-NLS-1$
	private IProject project;

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Image getImage(Object element) {
		if (element instanceof SpecfilePackage) {
			return Activator.getDefault().getImage(PACKAGE_ICON);
		} else if (element instanceof SpecfileSection) {
			return Activator.getDefault().getImage(SECTION_ICON);
		} else if (element instanceof SpecfilePackageContainer) {
			return Activator.getDefault().getImage(PACKAGES_ICON);
		}else if (element instanceof SpecfilePreamble) {
			return Activator.getDefault().getImage(PREAMBLE_ICON);
		}
		return null;
	}

	public String getText(Object element) {
		String str = ""; //$NON-NLS-1$
		try {
			if (element instanceof SpecfileSection) {
				SpecfileSection specfileSection = (SpecfileSection) element;
				str = specfileSection.toString();
			} else if (element instanceof Specfile) {
				str = ((Specfile) element).getName();
			} else if (element instanceof SpecfilePackageContainer) {
				str = Messages.SpecfileLabelProvider_0;
			} else if (element instanceof SpecfilePreamble){
				str = Messages.SpecfileLabelProvider_1;
			} else if (element instanceof SpecfileElement) {
				SpecfileElement specfileElement = (SpecfileElement) element;
				str = specfileElement.getName();
			} else if (element instanceof String) {
				str = (String) element;
			} else if (element instanceof SpecfilePackage) {
				str = ((SpecfilePackage) element).getName();
			}
			str = RPMQuery.eval(project, str).trim();
		} catch (CoreException e) {
			SpecfileLog.logError("Within Project Outline, unable to evaluate " + str, e); //$NON-NLS-1$
		}
		return str;
	}

	/**
	 * Sets IProject
	 * @since 2.1
	 */
	protected void setProject(IProject project) {
		this.project=project;
	}
}
