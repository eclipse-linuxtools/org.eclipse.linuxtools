/*******************************************************************************
 * Copyright (c) 2008, 2009 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API. 
 *    Red Hat - modifications for use with Valgrind plugins.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;
import java.util.HashMap;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;


public class SuppressionsEditor extends TextEditor {
	
	private ColorManager colorManager;
	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	
	public SuppressionsEditor() {
		this.colorManager = new ColorManager();		
		setSourceViewerConfiguration(new SuppressionsConfiguration(colorManager, this));
		setDocumentProvider(new SuppressionsDocumentProvider());
		oldAnnotations = null;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();
	    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
	    projectionSupport.install();
	    viewer.doOperation(ProjectionViewer.TOGGLE);
	    annotationModel = viewer.getProjectionAnnotationModel();
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}
	
	public void updateFoldingStructure(Position[] updatedPositions)	{		
		Annotation[] updatedAnnotations = new Annotation[updatedPositions.length];
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<>();
		for (int i = 0; i < updatedPositions.length; i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();	
			newAnnotations.put(annotation, updatedPositions[i]);
			updatedAnnotations[i] = annotation;
		}
		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);		
		oldAnnotations = updatedAnnotations;
	}
	
	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
}