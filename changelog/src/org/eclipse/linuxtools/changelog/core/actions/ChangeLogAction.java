/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.changelog.core.ChangeLogExtensionManager;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.changelog.core.Messages;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;


public abstract class ChangeLogAction extends Action {

	protected ChangeLogExtensionManager extensionManager = null;

	// Preference variables
	protected String pref_AuthorName;

	protected String pref_AuthorEmail;

	protected String pref_ChangeLogName = "ChangeLog";

	protected String pref_Formatter;

	// set selection here
	// selection can be either IEditorPart / IProject / IStructuredSelection
	// IEditorPart and IProject will be adapted to IStructuredSelection.

	public ChangeLogAction() {

		extensionManager = ChangeLogExtensionManager.getExtensionManager();
	}

	public ChangeLogAction(String name) {
		super(name);
		extensionManager = ChangeLogExtensionManager.getExtensionManager();
	}

	protected void reportErr(String msg, Exception e) {
		ChangelogPlugin.getDefault().getLog().log(
				new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, msg, e));
	}

	protected IWorkbench getWorkbench() {
		return ChangelogPlugin.getDefault().getWorkbench();
	}

	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	protected IEditorPart openEditor(IFile diskresource) {
		IWorkbench ws = getWorkbench();

		try {
			return org.eclipse.ui.ide.IDE.openEditor(ws
					.getActiveWorkbenchWindow().getActivePage(), diskresource,
					true);
		} catch (PartInitException e) {
			e.printStackTrace();

			return null;
		}
	}

	protected String returnQualifedEditor(Class ClassName) {
		return ClassName.toString().substring(
				ClassName.getPackage().toString().length() - 1,
				ClassName.toString().length());
	}

	protected IFile createChangeLog(IPath changelog) {
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		IWorkbench ws = getWorkbench();

		final IFile changelog_File = myWorkspaceRoot.getFile(changelog);
		final InputStream initialContents = new ByteArrayInputStream(
				new byte[0]);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws CoreException {
				try {
					monitor.beginTask("Adding ChangeLog", 2000); //$NON-NLS-1$
					changelog_File.create(initialContents, false, monitor);

					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}

				} finally {
					monitor.done();
				}
			}
		};

		try {
			new ProgressMonitorDialog(ws.getActiveWorkbenchWindow().getShell())
					.run(true, true, operation);
		} catch (InterruptedException e) {
			reportErr("Interruped while creating changelog", e);
			return null;
		} catch (InvocationTargetException e) {
			reportErr("Couldn't create changelog process", e);
			return null;
		}

		try {
			myWorkspaceRoot.refreshLocal(2, null);
		} catch (CoreException e) {
			reportErr("Couldn't refresh local resources", e);
			return null;
		}

		return changelog_File;
	}

	protected IEditorPart askChangeLogLocation(String editorLoc) {
		IWorkbench ws = getWorkbench();
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();

		IResource given_resource = myWorkspaceRoot.findMember(editorLoc);

		ContainerSelectionDialog dialog = new ContainerSelectionDialog(ws
				.getActiveWorkbenchWindow().getShell(), given_resource
				.getParent(), false, Messages
				.getString("AddAction.str_ChangeLog_Location")); //$NON-NLS-1$
		dialog.showClosedProjects(false);

		dialog.open();

		Object[] result = dialog.getResult();
		if (result == null)
			return null;
		final IPath result_path = new Path(result[0]
				+ System.getProperty("file.separator") + pref_ChangeLogName); //$NON-NLS-1$ //$NON-NLS-2$
		IFile newChangeLog = createChangeLog(result_path);

		return openEditor(newChangeLog);

	}

	protected IEditorPart getChangelog(String currentEditorloc) {
		// Scenario 1: The Changelog is in in the current project file
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		IResource given_resource = myWorkspaceRoot.findMember(currentEditorloc);

		if (given_resource != null) {
			IResource parent_dec = given_resource;

			while (parent_dec != null) {
				String parent_node = parent_dec.getFullPath()
						.removeLastSegments(1).toOSString();
				parent_node = parent_node
						+ System.getProperty("file.separator") + pref_ChangeLogName; //$NON-NLS-1$

				IResource change_log_res = myWorkspaceRoot
						.findMember(parent_node);

				if (change_log_res != null) {
					IProject proj_loc = given_resource.getProject();
					IPath modified_changelog_path = change_log_res
							.getFullPath().removeFirstSegments(1);
					IFile change_log_file = proj_loc
							.getFile(modified_changelog_path);

					return openEditor(change_log_file);
				}

				parent_dec = (IResource) parent_dec.getParent();

				if (parent_dec == null) {
					break;
				}
			}
		}

		return null;
	}

	protected IFile getDocumentIFile(IEditorPart currentEditor) {
		IEditorInput cc = currentEditor.getEditorInput();

		if (cc instanceof IFileEditorInput)
			return ((IFileEditorInput) cc).getFile();
		return null;
	}

	protected String getDocumentLocation(IEditorPart currentEditor,
			boolean appendRoot) {
		
		IEditorInput cc;
		String WorkspaceRoot;
		try {
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		WorkspaceRoot = myWorkspaceRoot.getLocation().toOSString();
		cc = currentEditor.getEditorInput();
		} catch(Exception e) {
			return "";
		}
		
		if (cc == null)
			return "";
		
		if ((cc instanceof SyncInfoCompareInput)
				|| (cc instanceof CompareEditorInput)) {

			CompareEditorInput test = (CompareEditorInput) cc;
			if (test.getCompareResult() == null)
				return "";
			if (appendRoot)
				return WorkspaceRoot + test.getCompareResult().toString();
			else
				return test.getCompareResult().toString();

		}

		IFile loc = getDocumentIFile(currentEditor);
		if (appendRoot) {
			return WorkspaceRoot + loc.getFullPath().toOSString();
		} else {
			return loc.getFullPath().toOSString();
		}
	}

	protected void loadPreferences() {
		IPreferenceStore store = ChangelogPlugin.getDefault()
				.getPreferenceStore();

		pref_AuthorName = store.getString("IChangeLogConstants.AUTHOR_NAME"); //$NON-NLS-1$
		pref_AuthorEmail = store.getString("IChangeLogConstants.AUTHOR_EMAIL"); //$NON-NLS-1$

		pref_Formatter = store
				.getString("IChangeLogConstants.DEFAULT_FORMATTER");
	}

}
