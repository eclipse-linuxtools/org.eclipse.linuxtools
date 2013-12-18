/*******************************************************************************
 * Copyright (c) 2008, 2013 Red Hat, Inc.
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
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * A class designated to handle the tasks of a specfile.
 *
 * @since 1.2
 */
public class SpecfileTaskHandler extends SpecfileMarkerHandler{
	public static final String SPECFILE_TASK_MARKER_ID = Activator.PLUGIN_ID
			+ ".specfiletask"; //$NON-NLS-1$

	/**
	 * Construct a specfile task handler given a IFile.
	 *
	 * @param file The specfile file.
	 * @param document The specfile document.
	 */
	public SpecfileTaskHandler(IFile file, IDocument document) {
		super(file, document);
	}

	/**
	 * Construct a specfile task handler given a FileEditorInput.
	 *
	 * @param file The FileEditorInput to get the file from.
	 * @param document The specfile document.
	 *
	 * @since 2.0
	 */
	public SpecfileTaskHandler(FileEditorInput file, IDocument document) {
		this(file.getFile(), document);
	}

	/**
	 * Handle the task of the specfile.
	 *
	 * @param lineNumber The line number of the task.
	 * @param line The line contents.
	 * @param taskType The task type.
	 */
	public void handleTask(int lineNumber, String line, String taskType) {
		if (file == null) {
			return;
		}

		String message = line.substring(line.indexOf(taskType));
		Map<String, Object> map = new HashMap<>();
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileMarkerHandler#getMarkerID()
	 */
	@Override
	String getMarkerID() {
		return SPECFILE_TASK_MARKER_ID;
	}
}
