/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.markers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParseException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class SpecfileErrorHandler extends SpecfileMarkerHandler{
	
	public static final String SPECFILE_ERROR_MARKER_ID = Activator.PLUGIN_ID
	+ ".specfileerror"; //$NON-NLS-1$
	
	private Map<Position, MarkerAnnotation> annotations = new HashMap<Position, MarkerAnnotation>();
	private AnnotationModel fAnnotationModel;
	private IEditorInput input;
	
	public SpecfileErrorHandler(IEditorInput input, IDocument document) {
		super(null, document);
		this.input = input;
	}
	
	private class SpecfileMarker implements IMarker {
		private Map<String, Object> attributes;
		private String type;
		private long id;
		
		public void delete() throws CoreException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
		public boolean exists() {
			// TODO Auto-generated method stub
			return true;
		}
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}
		public Object getAttribute(String attributeName) throws CoreException {
			return attributes.get(attributeName);
		}
		public boolean getAttribute(String attributeName, boolean defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof Boolean)
				return ((Boolean)o).booleanValue();
			return defaultValue;
		}
		public int getAttribute(String attributeName, int defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof Integer)
				return ((Integer)o).intValue();
			return defaultValue;
		}
		public String getAttribute(String attributeName, String defaultValue) {
			Object o = attributes.get(attributeName);
			if (o != null && o instanceof String)
				return (String)o;
			return defaultValue;
		}
		public Map<String, Object> getAttributes() throws CoreException {
			return attributes;
		}
		public Object[] getAttributes(String[] attributeNames)
				throws CoreException {
			Collection<Object> c = attributes.values();
			return c.toArray();
		}
		public long getCreationTime() throws CoreException {
			// TODO Auto-generated method stub
			return 0;
		}
		public long getId() {
			return id;
		}
		public IResource getResource() {
			return null;
		}
		public String getType() throws CoreException {
			return type;
		}
		public boolean isSubtypeOf(String superType) throws CoreException {
			if (superType.equals(type))
				return true;
			return false;
		}
		public void setAttribute(String attributeName, boolean value)
				throws CoreException {
			Boolean b = new Boolean(value);
			attributes.put(attributeName, b);
		}
		public void setAttribute(String attributeName, int value)
				throws CoreException {
			Integer i = new Integer(value);
			attributes.put(attributeName, i);	
		}
		public void setAttribute(String attributeName, Object value)
				throws CoreException {
			attributes.put(attributeName, value);
		}
		@SuppressWarnings("unchecked")
		public void setAttributes(Map map) throws CoreException {
			attributes.putAll(map);
		}
		public void setAttributes(String[] attributeNames, Object[] values)
				throws CoreException {
			for (int i = 0; i < attributeNames.length; ++i) {
				attributes.put(attributeNames[i], values[i]);
			}
		}
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
		public SpecfileMarker(String type, long id) {
			this.type = type;
			this.id = id;
			this.attributes = new HashMap<String, Object>();
		}
	}
	private IMarker createMarker(Map<String, Object> attributes, String markerType) throws CoreException {
		IMarker marker= new SpecfileMarker(markerType, -1);
		marker.setAttributes(attributes);
		return marker;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.cdt.autotools.ui.editors.IAutoconfErrorHandler#handleError(org.eclipse.linuxtools.cdt.autotools.core.ui.editors.parser.ParseException)
	 */
	public void handleError(SpecfileParseException e) {
		
		int lineNumber = e.getLineNumber();
		int lineOffset = 0;
		try {
			lineOffset = document.getLineOffset(lineNumber);
		} catch (BadLocationException e2) {
			// do nothing
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, e.getMessage());
		map.put(IMarker.MESSAGE, e.getMessage());
		map.put(IMarker.LOCATION, Integer.valueOf(lineNumber));

		Integer charStart = new Integer(lineOffset + e.getStartColumn());
		if (charStart != null) {
			map.put(IMarker.CHAR_START, charStart);
		}
		Integer charEnd = new Integer(lineOffset + e.getEndColumn());
		if (charEnd != null) {
			map.put(IMarker.CHAR_END, charEnd);
		}
		
		// FIXME:  add severity level
		map.put(IMarker.SEVERITY, new Integer(e.getSeverity()));
		
		try {
			IMarker marker = createMarker(map, SPECFILE_ERROR_MARKER_ID);

			MarkerAnnotation annotation = new MarkerAnnotation(marker);
			Position p = new Position(charStart.intValue(),charEnd.intValue() - charStart.intValue());
			fAnnotationModel.addAnnotation(annotation, p);
			annotations.put(p, annotation);
		} catch (CoreException ce) {
			// do nothing
		}
		return;
	}
	
	public void removeAllExistingMarkers()
	{
		fAnnotationModel.removeAllAnnotations();
		annotations.clear();
	}

	public void removeExistingMarkers() {
		removeExistingMarkers(0, document.getLength());
	}

	private AnnotationModel getAnnotationModel() {
		return (AnnotationModel)SpecfileEditor.getSpecfileDocumentProvider().getAnnotationModel(input);
	}
	@SuppressWarnings("unchecked")
	public void removeExistingMarkers(int offset, int length)
	{	
		fAnnotationModel = getAnnotationModel();
		Iterator i = fAnnotationModel.getAnnotationIterator();
		while (i.hasNext()) {
			Annotation annotation = (Annotation)i.next();
			Position p = fAnnotationModel.getPosition(annotation);
			int pStart = p.getOffset();
			if (pStart >= offset && pStart < (offset + length)) {
				// Remove directly from model instead of using
				// iterator so position will be removed from document.
				fAnnotationModel.removeAnnotation(annotation);
			}
		}
	}
	
	public SpecfileErrorHandler(IFile file, IDocument document)
	{
		this(new FileEditorInput(file), document);
	}

	@Override
	public void setFile(IFile file) {
		input = new FileEditorInput(file);
	}
	
//	public void handleError(SpecfileParseException e) {
//		int lineNumber = e.getLineNumber();
//		
//		if (file == null) {	return;	}
//		
//		Map<String, Object> map = new HashMap<String, Object>();
//		MarkerUtilities.setLineNumber(map, lineNumber);
//		MarkerUtilities.setMessage(map, e.getMessage());
//		map.put(IMarker.MESSAGE, e.getMessage());
//		map.put(IMarker.LOCATION, file.getFullPath().toString());
//
//		Integer charStart = getCharOffset(lineNumber, e.getStartColumn());
//		if (charStart != null) {
//			map.put(IMarker.CHAR_START, charStart);
//		}
//		Integer charEnd = getCharOffset(lineNumber, e.getEndColumn());
//		if (charEnd != null) {
//			map.put(IMarker.CHAR_END, charEnd);
//		}
//		
//		// FIXME:  add severity level
//		map.put(IMarker.SEVERITY, Integer.valueOf(e.getSeverity()));
//		
//		try {
//			MarkerUtilities.createMarker(file, map, SPECFILE_ERROR_MARKER_ID);
//		} catch (CoreException ee) {
//			SpecfileLog.logError(ee);
//		}
//	}
	
	@Override
	String getMarkerID() {
		return SPECFILE_ERROR_MARKER_ID;
	}
	
}
