/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class SpecfileTaskHandler {
	public static final String SPECFILE_TASK_MARKER_ID = Activator.PLUGIN_ID
			+ ".specfiletask";

	private IFile file;
	private IDocument document;

	public SpecfileTaskHandler(IFile file, IDocument document) {
		this.file = file;
		this.document = document;
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
		return;
	}

	public void removeExistingMarkers() {
		if (file == null) {
			return;
		}

		try {
			file.deleteMarkers(SPECFILE_TASK_MARKER_ID, true,
					IResource.DEPTH_ZERO);
		} catch (CoreException e1) {
			SpecfileLog.logError(e1);
		}
	}

	private Integer getCharOffset(int lineNumber, int columnNumber) {
		try {
			return Integer.valueOf(document.getLineOffset(lineNumber)
					+ columnNumber);
		} catch (BadLocationException e) {
			SpecfileLog.logError(e);
			return null;
		}
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public void setDocument(IDocument document) {
		this.document = document;
	}
}
