/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Test case providing all the objects needed for the rpm editor tests.
 * 
 */
public abstract class FileTestCase extends TestCase {

	protected SpecfileParser parser;
	protected Specfile specfile;
	protected IFile testFile;
	protected Document testDocument;
	SpecfileErrorHandler errorHandler;
	SpecfileTestProject testProject;
    FileEditorInput fei;
    SpecfileEditor editor;

	@Override
	protected void setUp() throws CoreException {
		testProject = new SpecfileTestProject();
		String fileName = "test" + this.getClass().getSimpleName() + ".spec";
		testFile = testProject.createFile(fileName);
		editor = new SpecfileEditor();
		parser = new SpecfileParser();
		specfile = new Specfile();
	}

	@Override
	protected void tearDown() throws CoreException {
		testProject.dispose();
	}

	@SuppressWarnings("rawtypes")
	protected IMarker[] getFailureMarkers() {
		ArrayList<IMarker> markers = new ArrayList<IMarker>();
		try {
			IAnnotationModel model = SpecfileEditor.getSpecfileDocumentProvider().getAnnotationModel(fei);
			for (Iterator i = model.getAnnotationIterator(); i.hasNext(); ) {
				Annotation annotation = (Annotation)i.next();
				if (annotation instanceof MarkerAnnotation) {
					MarkerAnnotation m = (MarkerAnnotation)annotation;
					markers.add(m.getMarker());
				}
			}
			return markers.toArray(new IMarker[markers.size()]);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected void newFile(String contents) {
		try {
			testFile.setContents(new ByteArrayInputStream(contents.getBytes()),
					false, false, null);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		testDocument = new Document(contents);
		fei = new FileEditorInput(testFile);
		try {
			SpecfileEditor.getSpecfileDocumentProvider().disconnect(fei);
			SpecfileEditor.getSpecfileDocumentProvider().connect(fei);
		} catch (CoreException e) {
			// let failures occur
		}
		errorHandler = new SpecfileErrorHandler(fei, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}

}
