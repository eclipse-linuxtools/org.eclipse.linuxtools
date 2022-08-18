/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfilePreamble;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackageContainer;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.graphics.Image;

public class SpecfileLabelProvider extends LabelProvider {

	private static final String PREAMBLE_ICON = "icons/preamble_obj.gif"; //$NON-NLS-1$
	private static final String SECTION_ICON = "icons/section_obj.gif"; //$NON-NLS-1$
	private static final String PACKAGES_ICON = "icons/packages_obj.gif"; //$NON-NLS-1$
	private static final String PACKAGE_ICON = "icons/package_obj.gif"; //$NON-NLS-1$

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof SpecfilePackage) {
			return Activator.getDefault().getImage(PACKAGE_ICON);
		} else if (element instanceof SpecfileSection) {
			return Activator.getDefault().getImage(SECTION_ICON);
		} else if (element instanceof SpecfilePackageContainer) {
			return Activator.getDefault().getImage(PACKAGES_ICON);
		} else if (element instanceof SpecfilePreamble) {
			return Activator.getDefault().getImage(PREAMBLE_ICON);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		String str = ""; //$NON-NLS-1$
		if (element instanceof SpecfileSection specfileSection) {
			str = specfileSection.toString();
		} else if (element instanceof Specfile s) {
			str = s.getName();
		} else if (element instanceof SpecfilePackageContainer) {
			str = Messages.SpecfileLabelProvider_0;
		} else if (element instanceof SpecfilePreamble) {
			str = Messages.SpecfileLabelProvider_1;
		} else if (element instanceof SpecfileElement specfileElement) {
			str = specfileElement.getName();
		} else if (element instanceof String s) {
			str = s;
		} else if (element instanceof SpecfilePackage sp) {
			str = sp.getName();
		}
		return filterMacros(str.trim());
	}

	/**
	 * Remove any unresolved macros from the string. These are macros that follow
	 * the format %{?...} (e.g. %{?scl_prefix}).
	 *
	 * @param text The text to filter macros out from.
	 * @return A string without unresolved macros.
	 *
	 * @since 2.1
	 */
	private String filterMacros(String text) {
		Pattern variablePattern = Pattern.compile("%\\{\\?\\w+\\}"); //$NON-NLS-1$
		Matcher variableMatcher = variablePattern.matcher(text);
		while (variableMatcher.find()) {
			text = text.replace(variableMatcher.group(0), ""); //$NON-NLS-1$
		}
		return text;
	}

}
