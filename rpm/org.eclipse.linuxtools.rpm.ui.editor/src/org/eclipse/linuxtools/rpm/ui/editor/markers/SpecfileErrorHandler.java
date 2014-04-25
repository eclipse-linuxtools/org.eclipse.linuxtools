/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileParseException;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class SpecfileErrorHandler extends SpecfileMarkerHandler {

    public static final String SPECFILE_ERROR_MARKER_ID = Activator.PLUGIN_ID
    + ".specfileerror"; //$NON-NLS-1$

    public static final String ANNOTATION_ERROR = "org.eclipse.ui.workbench.texteditor.error"; //$NON-NLS-1$
    public static final String ANNOTATION_WARNING = "org.eclipse.ui.workbench.texteditor.warning"; //$NON-NLS-1$
    public static final String ANNOTATION_INFO = "org.eclipse.ui.workbench.texteditor.info"; //$NON-NLS-1$

    private Map<Position, Annotation> annotations = new HashMap<>();
    private AnnotationModel fAnnotationModel;
    private IEditorInput input;

    public SpecfileErrorHandler(IEditorInput input, IDocument document) {
        super(null, document);
        this.input = input;
        fAnnotationModel = getAnnotationModel();
    }

    private static class SpecfileAnnotation extends Annotation implements IQuickFixableAnnotation {
        public SpecfileAnnotation(String annotationType, boolean persist, String message) {
            super(annotationType, persist, message);
        }

        /**
         * Tells whether this annotation is quick fixable.
         */
        private boolean fIsQuickFixable;
        /**
         * Tells whether the quick fixable state (<code>fIsQuickFixable</code> has been computed.
         */
        private boolean fIsQuickFixableStateSet;

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public void setQuickFixable(boolean state) {
            fIsQuickFixable= state;
            fIsQuickFixableStateSet= true;
        }

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public boolean isQuickFixableStateSet() {
            return fIsQuickFixableStateSet;
        }

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public boolean isQuickFixable() {
            Assert.isTrue(isQuickFixableStateSet());
            return fIsQuickFixable;
        }

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

        Integer charStart = Integer.valueOf(lineOffset + e.getStartColumn());
        Integer charEnd = Integer.valueOf(lineOffset + e.getEndColumn());
        String annotationType = ANNOTATION_INFO;
        if (e.getSeverity() == IMarker.SEVERITY_ERROR) {
            annotationType = ANNOTATION_ERROR;
        } else if (e.getSeverity() == IMarker.SEVERITY_WARNING) {
            annotationType = ANNOTATION_WARNING;
        }
        Annotation annotation = new SpecfileAnnotation(annotationType, true, e.getLocalizedMessage());
        Position p = new Position(charStart.intValue(),charEnd.intValue() - charStart.intValue());
        if (fAnnotationModel != null) {
            fAnnotationModel.addAnnotation(annotation, p);
        }
        annotations.put(p, annotation);
        return;
    }

    public void removeAllExistingMarkers()
    {
        fAnnotationModel.removeAllAnnotations();
        annotations.clear();
    }

    @Override
    public void removeExistingMarkers() {
        removeExistingMarkers(0, document.getLength());
    }

    private AnnotationModel getAnnotationModel() {
        return (AnnotationModel)SpecfileEditor.getSpecfileDocumentProvider().getAnnotationModel(input);
    }

    public void removeExistingMarkers(int offset, int length)
    {
        if (fAnnotationModel != null) {
            Iterator<Annotation> i = fAnnotationModel.getAnnotationIterator();
            while (i.hasNext()) {
                Annotation annotation = i.next();
                Position p = fAnnotationModel.getPosition(annotation);
                if (p != null) {
                    int pStart = p.getOffset();
                    if (pStart >= offset && pStart < (offset + length)) {
                        // Remove directly from model instead of using
                        // iterator so position will be removed from document.
                        fAnnotationModel.removeAnnotation(annotation);
                    }
                }
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

    @Override
    String getMarkerID() {
        return SPECFILE_ERROR_MARKER_ID;
    }

}
