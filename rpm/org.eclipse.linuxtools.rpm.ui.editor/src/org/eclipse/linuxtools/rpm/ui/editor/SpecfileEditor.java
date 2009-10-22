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
 *    Alexander Kurtakov - adapt to 3.5 API.
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileTaskHandler;
import org.eclipse.linuxtools.rpm.ui.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SpecfileEditor extends TextEditor {

	private ColorManager colorManager;
	private SpecfileContentOutlinePage outlinePage;
	private IEditorInput input;
	private Specfile specfile;
	private ProjectionSupport projectionSupport;
	private SpecfileParser parser;
	private SpecfileTabConverter tabConverter;
	private RpmMacroOccurrencesUpdater fOccurrencesUpdater;

	public SpecfileEditor() {
		super();
		colorManager = new ColorManager();
		parser = getParser();
		setSourceViewerConfiguration(new SpecfileConfiguration(colorManager,
				this));
		setDocumentProvider(new SpecfileDocumentProvider());
		setKeyBindingScopes(new String[] { "org.eclipse.linuxtools.rpm.ui.specEditorScope" }); //$NON-NLS-1$
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	public void dispose() {
		colorManager.dispose();
		// Set specfile field to null here is useful for test cases because
		// whether
		// the specfile in null SpecfileReconcilingStrategy#reconcile don't
		// update anything and thus it don't give false stacktraces.
		specfile = null;
		super.dispose();
	}

	@Override
	protected void doSetInput(IEditorInput newInput) throws CoreException {
		super.doSetInput(newInput);
		this.input = newInput;

		if (outlinePage != null)
			outlinePage.setInput(input);

		configureTabConverter();
		validateAndMark();
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();

		// we validate and mark document here
		validateAndMark();

		if (outlinePage != null)
			outlinePage.update();
	}

	protected void validateAndMark() {
		try {
			IDocument document = getInputDocument();
			SpecfileErrorHandler specfileErrorHandler = new SpecfileErrorHandler(
					getInputFile(), document);
			specfileErrorHandler.removeExistingMarkers();
			SpecfileTaskHandler specfileTaskHandler = new SpecfileTaskHandler(
					getInputFile(), document);
			specfileTaskHandler.removeExistingMarkers();
			this.parser.setErrorHandler(specfileErrorHandler);
			this.parser.setTaskHandler(specfileTaskHandler);
			specfile = parser.parse(document);
		} catch (Exception e) {
			SpecfileLog.logError(e);
		}
	}

	/**
	 * Get a {@link IFile}, this implementation return <code>null</code> if the
	 * <code>IEditorInput</code> instance is not of type
	 * {@link IFileEditorInput}.
	 * 
	 * @return a <code>IFile</code> or <code>null</code>.
	 */
	protected IFile getInputFile() {
		if (input instanceof IFileEditorInput) {
			IFileEditorInput ife = (IFileEditorInput) input;
			IFile file = ife.getFile();
			return file;
		}
		return null;
	}

	public IDocument getInputDocument() {
		IDocument document = getDocumentProvider().getDocument(input);
		return document;
	}

	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		if (projectionSupport != null) {
			Object adapter = projectionSupport.getAdapter(getSourceViewer(),
					required);
			if (adapter != null)
				return adapter;
		}
		return super.getAdapter(required);
	}

	public SpecfileContentOutlinePage getOutlinePage() {
		if (outlinePage == null) {
			outlinePage = new SpecfileContentOutlinePage(this);
			if (getEditorInput() != null)
				outlinePage.setInput(getEditorInput());
		}
		return outlinePage;
	}

	public Specfile getSpecfile() {
		return specfile;
	}

	/*
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer = new SpecfileProjectionViewer(parent, ruler,
				fOverviewRuler, true, styles, getSourceViewerConfiguration());
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	/*
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionSupport = new ProjectionSupport(projectionViewer,
				getAnnotationAccess(), getSharedColors());
		projectionSupport.install();
		projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		if (isTabConversionEnabled()) {
			startTabConversion();
		}
		fOccurrencesUpdater = new RpmMacroOccurrencesUpdater(this);
	}

	protected void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
		if (fOccurrencesUpdater != null) {
			Shell shell = getSite().getShell();
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
	 * @return the specfile source viewer
	 */
	public SourceViewer getSpecfileSourceViewer() {
		return (SourceViewer) getSourceViewer();
	}

	private void configureTabConverter() {
		if (tabConverter != null) {
			tabConverter.setLineTracker(new DefaultLineTracker());
		}
	}

	private int getTabSize() {
		return Activator.getDefault().getPreferenceStore().getInt(
				PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB);
	}

	private boolean isTabConversionEnabled() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.P_SPACES_FOR_TABS);
	}

	private void startTabConversion() {
		if (tabConverter == null) {
			tabConverter = new SpecfileTabConverter();
			tabConverter.setLineTracker(new DefaultLineTracker());
			tabConverter.setNumberOfSpacesPerTab(getTabSize());
			SpecfileProjectionViewer asv = (SpecfileProjectionViewer) getSourceViewer();
			asv.addTextConverter(tabConverter);
			asv.updateIndentationPrefixes();
		}
	}

	private void stopTabConversion() {
		if (tabConverter != null) {
			SpecfileProjectionViewer asv = (SpecfileProjectionViewer) getSourceViewer();
			asv.removeTextConverter(tabConverter);
			asv.updateIndentationPrefixes();
			tabConverter = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.texteditor.AbstractDecoratedTextEditor#
	 * handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {

		if (getSourceViewer() == null
				|| getSourceViewer().getTextWidget() == null)
			return;
		try {
			SpecfileProjectionViewer viewer = (SpecfileProjectionViewer) getSourceViewer();
			if (viewer != null) {
				String p = event.getProperty();
				if (PreferenceConstants.P_SPACES_FOR_TABS.equals(p)) {
					if (isTabConversionEnabled())
						startTabConversion();
					else
						stopTabConversion();
					return;
				}
				if (PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB.equals(p)) {
					viewer.updateIndentationPrefixes();
					if (tabConverter != null)
						tabConverter.setNumberOfSpacesPerTab(getTabSize());
					Object value = event.getNewValue();
					if (value instanceof Integer) {
						viewer.getTextWidget().setTabs(
								((Integer) value).intValue());
					} else if (value instanceof String) {
						viewer.getTextWidget().setTabs(
								Integer.parseInt((String) value));
					}
					return;
				}
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

}
