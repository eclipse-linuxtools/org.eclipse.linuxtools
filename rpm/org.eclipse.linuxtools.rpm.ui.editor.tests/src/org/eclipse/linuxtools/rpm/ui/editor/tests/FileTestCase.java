/*******************************************************************************
 * Copyright (c) 2008, 2009, 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.After;
import org.junit.Before;

/**
 * Test case providing all the objects needed for the rpm editor tests.
 * 
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

	@Before
	public void setUp() throws CoreException {
		testProject = new SpecfileTestProject();
		String fileName = "test" + this.getClass().getSimpleName() + ".spec";
		testFile = testProject.createFile(fileName);
		editor = new SpecfileEditor();
		parser = new SpecfileParser();
		specfile = new Specfile();
	}

	@After
	public void tearDown() throws CoreException {
		testProject.dispose();
	}

	public static void closeEditor(final IEditorPart editor) {
		if (editor.getSite().getWorkbenchWindow().getActivePage() != null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					// close editor
					editor.getSite().getWorkbenchWindow().getActivePage()
							.closeEditor(editor, false);
				}
			});
		}
	}

	protected SpecfileTestFailure[] getFailures() {
		ArrayList<SpecfileTestFailure> failures = new ArrayList<SpecfileTestFailure>();
		IAnnotationModel model = SpecfileEditor.getSpecfileDocumentProvider()
				.getAnnotationModel(fei);
		for (Iterator<Annotation> i = model.getAnnotationIterator(); i
				.hasNext();) {
			Annotation annotation = i.next();
			Position p = model.getPosition(annotation);
			SpecfileTestFailure t = new SpecfileTestFailure(annotation, p);
			failures.add(t);
		}
		return failures.toArray(new SpecfileTestFailure[failures.size()]);
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

	/**
	 * Set the potential rpm package list to the given list. Useful for
	 * testing package proposals.
	 * @param packages 
	 */
	protected void setPackageList(String[] packages) {
		ScopedPreferenceStore prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		prefStore.setValue(PreferenceConstants.P_RPM_LIST_FILEPATH,
						"/tmp/pkglist1");
		prefStore.setValue(PreferenceConstants.P_RPM_LIST_BACKGROUND_BUILD,
						false);

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"/tmp/pkglist1"));
			
			for (String packageName : packages){
				out.write(packageName + "\n");
			}

			out.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		Activator.packagesList = null;
	}
}
