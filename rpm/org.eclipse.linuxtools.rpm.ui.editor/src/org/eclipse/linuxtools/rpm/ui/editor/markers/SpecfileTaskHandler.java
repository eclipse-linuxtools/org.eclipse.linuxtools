/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class SpecfileTaskHandler extends SpecfileMarkerHandler{
	public static final String SPECFILE_TASK_MARKER_ID = Activator.PLUGIN_ID
			+ ".specfiletask"; //$NON-NLS-1$

	public SpecfileTaskHandler(IFile file, IDocument document) {
		super(file, document);
	}

	public void handleTask(int lineNumber, String line, String taskType) {
		if (file == null) {
			return;
		}

		String message = line.substring(line.indexOf(taskType));
		Map<String, Object> map = new HashMap<String, Object>();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, message);
		map.put(IMarker.MESSAGE, message);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		map.put(IMarker.USER_EDITABLE, false);
		Integer charStart = getCharOffset(lineNumber, 0);
		if (charStart != null) {
			map.put(IMarker.CHAR_START, charStart);
		}
		Integer charEnd = getCharOffset(lineNumber, line.length());
		if (charEnd != null) {
			map.put(IMarker.CHAR_END, charEnd);
		}

		try {
			MarkerUtilities.createMarker(file, map, SPECFILE_TASK_MARKER_ID);
		} catch (CoreException ee) {
			SpecfileLog.logError(ee);
		}
	}

	@Override
	String getMarkerID() {
		return SPECFILE_TASK_MARKER_ID;
	}

	
}
