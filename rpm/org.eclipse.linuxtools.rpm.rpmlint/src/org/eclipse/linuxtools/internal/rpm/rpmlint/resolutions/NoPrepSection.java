/*******************************************************************************
 * Copyright (c) 2008, 2013 Alexander Kurtakov.
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
 * Resolution for the "no-%prep-section".
 * Resolves by adding an empty %prep section.
 *
 */
public class NoPrepSection extends AInsertLineResolution{
    /**
     * The rpmlint ID of the warning.
     */
    public static final String ID = "no-%prep-section"; //$NON-NLS-1$

    @Override
    public String getDescription() {
        return Messages.NoPrepSection_0;
    }

    @Override
    public String getLabel() {
        return ID;
    }

    @Override
    public String getLineToInsert() {
        return "%prep\n\n"; //$NON-NLS-1$
    }

    @Override
    public int getLineNumberForInsert(SpecfileEditor editor) {
        List<SpecfileSection> sections = editor.getSpecfile().getSections();
        for (SpecfileSection section : sections) {
            if (section.getName().equals("build")) { //$NON-NLS-1$
                return section.getLineNumber();
            }
        }
        return 0;
    }
}
