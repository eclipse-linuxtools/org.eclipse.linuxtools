/*******************************************************************************
 * Copyright (c) 2008, 2018 Alexander Kurtakov.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import java.util.List;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;

/**
 * Resolution for the no-%build-section rpmlint warning. Fix is to put an empty
 * %build section.
 *
 */
public class NoBuildSection extends AInsertLineResolution {
	/**
	 * The string ID of the rpmlint warning.
	 */
	public static final String ID = "no-%build-section"; //$NON-NLS-1$

	@Override
	public String getDescription() {
		return Messages.NoBuildSection_0;
	}

	@Override
	public String getLabel() {
		return ID;
	}

	@Override
	public String getLineToInsert() {
		return "%build\n\n"; //$NON-NLS-1$
	}

	@Override
	public int getLineNumberForInsert(Specfile specfile) {
		List<SpecfileSection> sections = specfile.getSections();
		for (SpecfileSection section : sections) {
			if (section.getName().equals("install")) { //$NON-NLS-1$
				return section.getLineNumber();
			}
		}
		return 0;
	}
}
