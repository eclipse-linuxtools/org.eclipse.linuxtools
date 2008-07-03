/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.IEditorPart;

import junit.framework.TestCase;

abstract class AScannerTest extends TestCase {
	
	private SpecfileTestProject testProject;

	private IFile testFile;

	private Document testDocument;

	protected RuleBasedScanner rulesBasedScanner;

	protected abstract String getContents();
	
	protected abstract RuleBasedScanner getScanner();
	
	public SpecfileEditor editor;
	
	public AScannerTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("testspecfile.spec");
		newFile(getContents());
		IEditorPart openEditor = org.eclipse.ui.ide.IDE.openEditor(Activator
				.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage(), testFile,
		"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");
		editor = (SpecfileEditor) openEditor;
		rulesBasedScanner = getScanner();
		rulesBasedScanner.setRange(testDocument, 0, getContents().length());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		editor.dispose();
		testProject.dispose();
	}

	protected void newFile(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()),
				false, false, null);
		testDocument = new Document(contents);
	}

	protected IToken getNextToken() {
		return rulesBasedScanner.nextToken();
	}

	protected IToken getToken(int nbrOfToken) {
		for (int i = 0; i < nbrOfToken - 1; i++) {
			rulesBasedScanner.nextToken();
		}
		return rulesBasedScanner.nextToken();
	}

	
}
