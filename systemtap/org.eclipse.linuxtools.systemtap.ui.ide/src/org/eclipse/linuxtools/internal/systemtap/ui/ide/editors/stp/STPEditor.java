/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API. 
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class STPEditor extends SimpleEditor {

	private ColorManager colorManager;
	
    private ProjectionSupport stpProjectionSupport;
	private Annotation[] stpOldAnnotations;
	private ProjectionAnnotationModel stpAnnotationModel;

	public static final String ID="org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor"; //$NON-NLS-1$

	public STPEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new STPConfiguration(colorManager,this));
		setDocumentProvider(new STPDocumentProvider());
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if(input instanceof FileStoreEditorInput)
			input= new PathEditorInput(new Path(((FileStoreEditorInput) input).getURI().getPath()));

		super.doSetInput(input);
	}

	@Override
	public void createPartControl(Composite parent)
	{
	    super.createPartControl(parent);
	   ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();
	   stpProjectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
	   stpProjectionSupport.install();
	   viewer.doOperation(ProjectionViewer.TOGGLE);
	   stpAnnotationModel = viewer.getProjectionAnnotationModel();
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

		
	public void updateFoldingStructure(ArrayList<Position> updatedPositions)
	{
		ProjectionAnnotation annotation;
		Annotation[] updatedAnnotations = new Annotation[updatedPositions.size()];
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<>();
		for(int i =0;i<updatedPositions.size();i++)
		{
			annotation = new ProjectionAnnotation();	
			newAnnotations.put(annotation,updatedPositions.get(i));
			updatedAnnotations[i]=annotation;
		}
		stpAnnotationModel.modifyAnnotations(stpOldAnnotations,newAnnotations,null);		
		stpOldAnnotations = updatedAnnotations;
	}
	
	public ISourceViewer getMySourceViewer() {
		return this.getSourceViewer();
	}
	
	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {

		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_LEFT);

	}
	
}