/*******************************************************************************
 * Copyright (c) 2008, 2017 Phil Muldoon and others.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class STPEditor extends TextEditor {

    private Annotation[] stpOldAnnotations;
    private ProjectionAnnotationModel stpAnnotationModel;

    public static final String ID="org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor"; //$NON-NLS-1$

    public STPEditor() {
        super();
        setKeyBindingScopes(new String[] { "org.eclipse.linuxtools.systemtap.ui.ide.context" }); //$NON-NLS-1$
        configureInsertMode(SMART_INSERT, false);
        setSourceViewerConfiguration(new STPConfiguration(this));
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        if(input instanceof FileStoreEditorInput) {
            input= new PathEditorInput(new Path(((FileStoreEditorInput) input).getURI().getPath()));
        }
        super.doSetInput(input);
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
       ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();
       ProjectionSupport stpProjectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
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


	public void updateFoldingStructure(ArrayList<Position> updatedPositions) {
		ProjectionAnnotation annotation;
		Annotation[] updatedAnnotations = new Annotation[updatedPositions.size()];
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<>();
		for (int i = 0; i < updatedPositions.size(); i++) {
			annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, updatedPositions.get(i));
			updatedAnnotations[i] = annotation;
		}
		stpAnnotationModel.modifyAnnotations(stpOldAnnotations, newAnnotations, null);
		stpOldAnnotations = updatedAnnotations;
	}

	public ISourceViewer getMySourceViewer() {
		return this.getSourceViewer();
	}
	
	 /**
     * Inserts text into the IDocument.
     * @param text string to insert
     */
    public void insertText(String text) {
        IDocument doc = getSourceViewer().getDocument();
        String s = doc.get();
        int offset = s.length();
        s += text;
        doc.set(s);
        this.setHighlightRange(offset, 0, true);
    }

    /**
     * Inserts text at the current location.
     * @param text string to insert
     */
    public void insertTextAtCurrent(String text) {
        ISelection selection = this.getSelectionProvider().getSelection();
        IDocument doc = getSourceViewer().getDocument();

        if (selection instanceof ITextSelection) {
            ITextSelection s = (ITextSelection) selection;
            StringBuffer sb = new StringBuffer(doc.get().substring(0, s.getOffset()));
            sb.append(text.trim());
            sb.append(doc.get().substring(s.getOffset() + s.getLength(), doc.get().length()));
            doc.set(sb.toString());
            this.setHighlightRange(s.getOffset() + text.trim().length(), 0, true);
        }
    }
    
    /**
     * Jumps to the location in the IDocument.
     * @param line The line you wish to jump to.
     * @param character The character you wish to jump to.
     */
    public void jumpToLocation(int line, int character) {
        IDocument doc = getSourceViewer().getDocument();

        try {
            int offset = doc.getLineOffset(line-1) + character;
            this.getSelectionProvider().setSelection(new TextSelection(doc, offset, 0));
        } catch (BadLocationException boe) {
            // Pass
        }
    }

    /**
     * Selects a line in the IDocument.
     * @param line the line you wish to select
     */
    public void selectLine(int line) {
        IDocument doc = getSourceViewer().getDocument();

        try {
            this.getSelectionProvider().setSelection(new TextSelection(doc, doc.getLineOffset(line-1), doc.getLineLength(line-1)-1));
        } catch (BadLocationException boe) {
            // Pass
        }
    }
    
    /**
     * Performs a SaveAs on the IDocument.
     */
    @Override
    public void doSaveAs() {
        File file = queryFile();
        if (file == null) {
            return;
        }

        IEditorInput inputFile = createEditorInput(file);

        IDocument doc = getSourceViewer().getDocument();
        String s = doc.get();

        try (FileOutputStream fos = new FileOutputStream(file);
                PrintStream ps = new PrintStream(fos)){
            ps.print(s);
            ps.close();
        } catch (IOException fnfe) {
            // Pass
        }

        setInput(inputFile);
        setPartName(inputFile.getName());
    }

    /**
     * Sets up an editor input based on the specified file.
     * @param file the location of the file you wish to set.
     * @return input object created.
     */
    private static IEditorInput createEditorInput(File file) {
        IPath location= new Path(file.getAbsolutePath());
        return new PathEditorInput(location);
    }

    private static File queryFile() {
        FileDialog dialog= new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
        dialog.setText(Messages.NewFileHandler_NewFile);
        String path= dialog.open();
        if (path != null && !path.isEmpty()) {
            return new File(path);
        }
        return null;
    }

    @Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);

	}

}