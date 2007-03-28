package org.eclipse.cdt.rpm.editor;

import org.eclipse.cdt.rpm.editor.outline.SpecfileContentOutlinePage;
import org.eclipse.cdt.rpm.editor.parser.Specfile;
import org.eclipse.cdt.rpm.editor.parser.SpecfileParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SpecfileEditor extends TextEditor {

	private ColorManager colorManager;
	private SpecfileContentOutlinePage outlinePage;
	private IEditorInput input;
	private Specfile specfile;
	private SpecfileParser parser;

	public SpecfileEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new SpecfileConfiguration(colorManager, this));
		setDocumentProvider(new SpecfileDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
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

			parser = getParser();
			parser.setErrorHandler(specfileErrorHandler);
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
	
	protected void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
	}
	
	public SpecfileParser getParser() {
		if (parser == null) {
			parser = new SpecfileParser();
		}
		return parser;
	}

}
