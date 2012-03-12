/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import java.util.List;

import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;

/**
 * Quick fix for the "no-buildroot-tag" error.
 * TODO: Provide UI for defining the BuildRoot format.
 *
 */
public class NoBuildrootTag extends AInsertLineResolution {
	public static final String ID = "no-buildroot-tag"; //$NON-NLS-1$

	public String getDescription() {
		return Messages.NoBuildrootTag_0;
	}

	public String getLabel() {
		return ID;
	}

	@Override
	public String getLineToInsert() {
		return "BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root\n\n"; //$NON-NLS-1$
	}

	@Override
	public int getLineNumberForInsert(SpecfileEditor editor) {
		List<SpecfileSection> sections = editor.getSpecfile()
				.getComplexSections();
		for (SpecfileSection section : sections) {
			if (section.getName().equals("description") //$NON-NLS-1$
					&& section.getPackage() == null) {
				return section.getLineNumber();
			}
		}
		return 0;
	}
}
