/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import java.util.ArrayList;

import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.ISTAnnotationColumn;
import org.eclipse.linuxtools.internal.gcov.parser.Line;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.osgi.util.NLS;

public class CoverageAnnotationColumn implements ISTAnnotationColumn {

    private final ArrayList<Line> lines;

    public CoverageAnnotationColumn(SourceFile sourceFile) {
        lines = sourceFile.getLines();
    }

    @Override
    public String getAnnotation(int index) {
        try {
            Line l = lines.get(index + 1);
            if (l.exists()) {
                return Long.toString(l.getCount());
            }
        } catch (IndexOutOfBoundsException _) {
            // ignore as empty string will be returned anyway
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getTooltip(int index) {
        try {
            Line l = lines.get(index + 1);
            if (!l.exists()) {
                return Messages.CoverageAnnotationColumn_non_exec_line;
            } else {
                long count = l.getCount();
                if (count == 0) {
                    return Messages.CoverageAnnotationColumn_line_never_exec;
                } else if (count == 1) {
                    return Messages.CoverageAnnotationColumn_line_exec_once;
                }
                return NLS.bind(Messages.CoverageAnnotationColumn_line_mulitiple_exec, Long.toString(count));
            }
        } catch (IndexOutOfBoundsException _) {
            return Messages.CoverageAnnotationColumn_non_exec_line;
        }
    }

}
