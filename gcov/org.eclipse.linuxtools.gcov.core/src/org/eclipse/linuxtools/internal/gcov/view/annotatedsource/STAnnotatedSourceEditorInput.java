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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.AbstractSTAnnotatedSourceEditorInput;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.ISTAnnotationColumn;
import org.eclipse.linuxtools.internal.gcov.parser.Line;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;



public class STAnnotatedSourceEditorInput extends
AbstractSTAnnotatedSourceEditorInput {

	private final SourceFile sourceFile;
	private final int lineCount;
	private final ArrayList<ISTAnnotationColumn> columns = new ArrayList<ISTAnnotationColumn>();

	public static final Color GREEN = new Color(PlatformUI.getWorkbench().getDisplay(), 0 ,128, 0);

	// FIXME: dispose colors ?
	private static final Color[] greenColors = new Color[129];
	
	
	public STAnnotatedSourceEditorInput(IFileStore fileStore, SourceFile sourceFile){
		super(fileStore);
		this.sourceFile = sourceFile; 
		lineCount = sourceFile.getLines().size();
		this.columns.add(new CoverageAnnotationColumn(sourceFile));
	}

	@Override
	public Color getColor(int ln) {
		final int index = ln + 1;
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (index < lineCount){
			ArrayList<Line> lines = sourceFile.getLines();
			Line line = lines.get(index);
			if (line.exists()) {
				long count = line.getCount();
				if (count == 0) return display.getSystemColor(SWT.COLOR_RED);
				if (count == sourceFile.getmaxLineCount()) return GREEN;
				int colorIndex = 128 - (int) ((128*count)/sourceFile.getmaxLineCount());
				if (greenColors[colorIndex] == null) {
					greenColors[colorIndex] = new Color(display, colorIndex,127+colorIndex,colorIndex);
				}
				return greenColors[colorIndex];
			}
		}
		return display.getSystemColor(SWT.COLOR_WHITE);
	}	

	@Override
	public ArrayList<ISTAnnotationColumn> getColumns() {
		return columns;
	}

}
