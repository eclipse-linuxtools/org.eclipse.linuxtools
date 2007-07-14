/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SpecfileEditor extends TextEditor {

	private ColorManager colorManager;
	private SpecfileContentOutlinePage outlinePage;
	private IEditorInput input;
	private Specfile specfile;
	private ProjectionSupport projectionSupport;
	private SpecfileParser parser;
	private RpmMacroOccurrencesUpdater fOccurrencesUpdater;

	public SpecfileEditor() {
		super();
		colorManager = new ColorManager();
		parser = getParser();
		setSourceViewerConfiguration(new SpecfileConfiguration(colorManager, this));
		setDocumentProvider(new SpecfileDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		// Set specfile field to null here is useful for test cases because whether 
		// the specfile in null SpecfileReconcilingStrategy#reconcile don't 
		// update anything and thus it don't give false stacktraces.
		specfile = null;
		super.dispose();
	}

	protected void doSetInput(IEditorInput newInput) throws CoreException
	{
		super.doSetInput(newInput);
		this.input = newInput;

		if (outlinePage != null)
			outlinePage.setInput(input);

		validateAndMark();
	}

	protected void editorSaved()
	{
		super.editorSaved();
		
		//we validate and mark document here
		validateAndMark();

		if (outlinePage != null)
			outlinePage.update();	
	}

	protected void validateAndMark()
	{
		try
		{
			IDocument document = getInputDocument();
			SpecfileErrorHandler specfileErrorHandler = new SpecfileErrorHandler(getInputFile(), document);
			specfileErrorHandler.removeExistingMarkers();

			this.parser.setErrorHandler(specfileErrorHandler);
			specfile = parser.parse(document);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected IFile getInputFile()
	{
		IFileEditorInput ife = (IFileEditorInput) input;
		IFile file = ife.getFile();
		return file;
	}

	protected IDocument getInputDocument()
	{
		IDocument document = getDocumentProvider().getDocument(input);
		return document;
	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		if (projectionSupport != null) {
			Object adapter= projectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}
		return super.getAdapter(required);
	}
	
	public SpecfileContentOutlinePage getOutlinePage() {
		if (outlinePage == null) {
			outlinePage= new SpecfileContentOutlinePage(this);
			if (getEditorInput() != null)
				outlinePage.setInput(getEditorInput());
		}
		return outlinePage;
	}
	
	public Specfile getSpecfile() {
		return specfile;
	}

	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer= new ProjectionViewer(parent, ruler, fOverviewRuler, true, styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();
		projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		fOccurrencesUpdater = new RpmMacroOccurrencesUpdater(this);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		IAction action= new ContentAssistAction(
				getResourceBundle(),
				"ContentAssistProposal.",
				this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssist", action);
		markAsStateDependentAction("ContentAssist", true);
	}
	
	
	// ContentAssistAction take a ResourceBundle but Resource bundles are not yet implemented on  
	// a plugin level, so we have add this method here as a quick fix.
	public ResourceBundle getResourceBundle() {
		ResourceBundle resourceBundle;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditorMessages");
		} catch (MissingResourceException x) {
			resourceBundle= null;
		}
		return resourceBundle;
	}
	
	protected void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
		if (fOccurrencesUpdater != null) {
			Shell shell= getSite().getShell();
			if (!(shell == null || shell.isDisposed())) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						fOccurrencesUpdater.update(getSourceViewer());
					}
				});
			}
		}

	}
	
	public SpecfileParser getParser() {
		if (parser == null) {
			parser = new SpecfileParser();
		}
		return parser;
	}
	
	/**
	 * Get the spefile source viewer, this method is useful for test cases.
	 * 
	 * @return
	 * 		the specfile source viewer
	 */
	public SourceViewer getSpecfileSourceViewer() {
		return (SourceViewer) getSourceViewer();
	}

}
