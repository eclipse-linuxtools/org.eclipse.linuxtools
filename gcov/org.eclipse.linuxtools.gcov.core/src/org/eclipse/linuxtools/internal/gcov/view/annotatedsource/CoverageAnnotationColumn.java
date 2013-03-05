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


public class CoverageAnnotationColumn implements ISTAnnotationColumn {


	private final ArrayList<Line> lines;

	public CoverageAnnotationColumn(SourceFile sourceFile) {
		lines = sourceFile.getLines();
	}

	@Override
	public String getAnnotation(int index) {
		try {
			Line l = lines.get(index+1);
			if (l.exists()) {
				return Long.toString(l.getCount());
			}
		} catch (IndexOutOfBoundsException _) {
			//ignore as empty string will be returned anyway
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getTooltip(int index) {
		try {
			Line l = lines.get(index+1);
			if (!l.exists()) {
				return "non executable line";
			} else {
				long count = l.getCount();
				if (count == 0) return "line never executed";
				if (count == 1) return "line executed 1 time";
				return "line executed "
				+ Long.toString(count)
				+ " times";
			}
		} catch (IndexOutOfBoundsException _) {
			return "non executable line";
		}
	}

}
