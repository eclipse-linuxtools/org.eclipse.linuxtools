/*******************************************************************************
 * Copyright (c) 2019, 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.docker.editor.ls.tests;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestLanguageServers {

	private IProject project;
	
	@BeforeEach
	public void setUpProject() throws Exception {
		ScopedPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e");
		prefs.putValue("org.eclipse.linuxtools.docker.editor.ls.server.logging.enabled", Boolean.toString(true));
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IViewReference ref : activePage.getViewReferences()) {
			activePage.hideView(ref);
		}
	}

	@AfterEach
	public void deleteProjectAndCloseEditors() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		this.project.delete(true, null);
	}
	
	@ParameterizedTest
	@ValueSource(strings={ "test.dockerfile", "Dockerfile" })
	void testDiagnostics(String fileName) throws Exception {
		final IFile file = project.getFile(fileName);
		file.create(new ByteArrayInputStream("".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE
				.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("MAINTAINER alex");
		Assertions.assertTrue(
				DisplayHelper.waitForCondition(PlatformUI.getWorkbench().getDisplay(), 5000, () -> {
					try {
						return file.findMarkers("org.eclipse.lsp4e.diagnostic", true, IResource.DEPTH_ZERO).length != 0;
					} catch (CoreException e) {
						return false;
					}
				}), "Diagnostic not published");
	}
}
