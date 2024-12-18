/*******************************************************************************
 * Copyright (c) 2008, 2024 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test case providing all the objects needed for the rpm editor tests.
 */
public abstract class FileTestCase {

	protected SpecfileParser parser;
	protected Specfile specfile;
	protected IFile testFile;
	protected Document testDocument;
	SpecfileErrorHandler errorHandler;
	SpecfileTestProject testProject;
	FileEditorInput fei;
	protected SpecfileEditor editor;

	@BeforeEach
	public void setUp() throws CoreException {
		testProject = new SpecfileTestProject();
		String fileName = "test" + this.getClass().getSimpleName() + ".spec";
		testFile = testProject.createFile(fileName);
		editor = new SpecfileEditor();
		parser = new SpecfileParser();
		specfile = new Specfile();
	}

	@AfterEach
	public void tearDown() throws CoreException {
		testProject.dispose();
	}

	public static void closeEditor(final IEditorPart editor) {
		if (editor.getSite().getWorkbenchWindow().getActivePage() != null) {
			PlatformUI.getWorkbench().getDisplay()
					.syncExec(() -> editor.getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false));
		}
	}

	protected ArrayList<SpecfileTestFailure> getFailures() {
		ArrayList<SpecfileTestFailure> failures = new ArrayList<>();
		IAnnotationModel model = SpecfileEditor.getSpecfileDocumentProvider().getAnnotationModel(fei);
		for (Iterator<Annotation> i = model.getAnnotationIterator(); i.hasNext();) {
			Annotation annotation = i.next();
			Position p = model.getPosition(annotation);
			SpecfileTestFailure t = new SpecfileTestFailure(annotation, p);
			failures.add(t);
		}
		return failures;
	}

	protected void newFile(String contents) {
		try {
			testFile.setContents(contents.getBytes(), false, false, null);
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
