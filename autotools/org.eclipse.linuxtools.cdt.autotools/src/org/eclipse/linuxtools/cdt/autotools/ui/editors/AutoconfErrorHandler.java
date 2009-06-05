/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.ui.editors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsPlugin;
import org.eclipse.linuxtools.cdt.autotools.ui.editors.parser.IAutoconfErrorHandler;
import org.eclipse.linuxtools.cdt.autotools.ui.editors.parser.ParseException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;


public class AutoconfErrorHandler implements IAutoconfErrorHandler {
	
	public static final String PARSE_ERROR_MARKER_ID = AutotoolsPlugin.PLUGIN_ID
	+ ".parsefileerror";
	
	private Map annotations = new HashMap();
	private AnnotationModel fAnnotationModel;
	
	public AutoconfErrorHandler(IEditorInput input) {
		this.fAnnotationModel = (AnnotationModel)AutoconfEditor.getAutoconfDocumentProvider().getAnnotationModel(input);
	}
	
	private class AutoconfMarker implements IMarker {
		private Map attributes;
		private String type;
		private long id;
		
		public void delete() throws CoreException {
			// TODO Auto-generated method stub
			
		}
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}
		public boolean exists() {
			// TODO Auto-generated method stub
			return true;
		}
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
		public Map getAttributes() throws CoreException {
			return attributes;
		}
		public Object[] getAttributes(String[] attributeNames)
				throws CoreException {
			Collection c = attributes.values();
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
		public AutoconfMarker(String type, long id) {
			this.type = type;
			this.id = id;
			this.attributes = new HashMap();
		}
	}
	private IMarker createMarker(Map attributes, String markerType) throws CoreException {
		IMarker marker= new AutoconfMarker(markerType, -1);
		marker.setAttributes(attributes);
		return marker;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.cdt.autotools.ui.editors.IAutoconfErrorHandler#handleError(org.eclipse.linuxtools.cdt.autotools.ui.editors.parser.ParseException)
	 */
	public void handleError(ParseException e) {
		
		int lineNumber = e.getLineNumber();
		
		Map map = new HashMap();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, e.getMessage());
		map.put(IMarker.MESSAGE, e.getMessage());
		map.put(IMarker.LOCATION, Integer.valueOf(lineNumber));

		Integer charStart = new Integer(e.getStartOffset());
		if (charStart != null) {
			map.put(IMarker.CHAR_START, charStart);
		}
		Integer charEnd = new Integer(e.getEndOffset());
		if (charEnd != null) {
			map.put(IMarker.CHAR_END, charEnd);
		}
		
		// FIXME:  add severity level
		map.put(IMarker.SEVERITY, new Integer(e.getSeverity()));
		
		try {
			IMarker marker = createMarker(map, ICModelMarker.C_MODEL_PROBLEM_MARKER);

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

	public void removeExistingMarkers(int offset, int length)
	{	
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
}
