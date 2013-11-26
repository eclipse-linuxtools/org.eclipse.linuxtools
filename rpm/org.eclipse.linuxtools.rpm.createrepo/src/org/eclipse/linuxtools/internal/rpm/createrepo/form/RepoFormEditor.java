/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.listener.CreaterepoResourceChangeListener;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * The repo file form editor will allow the user to edit the .repo
 * file, import/remove RPMs, and customize the execution and/or
 * the repo metadata that is generated.
 */
public class RepoFormEditor extends FormEditor {

	public static final String EDITOR_ID = "org.eclipse.linuxtools.rpm.createrepo.repoEditor"; //$NON-NLS-1$

	private CreaterepoProject project;
	private TextEditor editor;
	private IResourceChangeListener resourceChangeListener;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		IFile file = ResourceUtil.getFile(input);
		setPartName(file.getName());
		try {
			project = new CreaterepoProject(file.getProject(), file);
		} catch (CoreException e) {
			Activator.logError(Messages.RepoFormEditor_errorInitializingProject, e);
		}
		resourceChangeListener = new CreaterepoResourceChangeListener(project);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		editor.doSaveAs();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		createPage0();
		createPage1();
		createPage2();
	}

	/**
	 * Page 0 allows the user to import RPMs from the workspace or
	 * the file system.
	 */
	void createPage0() {
		try {
			FormPage composite = new ImportRPMsPage(this, project);
			addPage(composite);
		} catch (PartInitException e) {
			Activator.logError(Messages.RepoFormEditor_errorInitializingForm, e);
		}
	}

	/**
	 * Page 1 allows the user to modify some of the data in the
	 * repomd.xml as well as some options when customizing the
	 * execution of the createrepo command. The default execution
	 * would satisfy most users.
	 */
	void createPage1() {
		try {
			FormPage composite = new MetadataPage(this);
			addPage(composite);
		} catch (PartInitException e) {
			Activator.logError(Messages.RepoFormEditor_errorInitializingForm, e);
		}
	}

	/**
	 * Page 2 allows the user to view the current .repo file.
	 */
	void createPage2() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			Activator.logError(Messages.RepoFormEditor_errorInitializingForm, e);
		}
	}

}
