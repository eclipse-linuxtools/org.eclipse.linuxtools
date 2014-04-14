/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *    Jeff Johnston <jjohnstn@redhat.com> - add removed files support
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.linuxtools.internal.changelog.core.ChangeLogExtensionManager;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.internal.changelog.core.Messages;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;


public abstract class ChangeLogAction extends Action {

	protected ChangeLogExtensionManager extensionManager = null;

	// Preference variables
	protected String pref_AuthorName;

	protected String pref_AuthorEmail;

	protected String pref_ChangeLogName = "ChangeLog"; // $NON-NLS-1$

	protected String pref_Formatter;

	// set selection here
	// selection can be either IEditorPart / IProject / IStructuredSelection
	// IEditorPart and IProject will be adapted to IStructuredSelection.

	public ChangeLogAction() {
		extensionManager = ChangeLogExtensionManager.getExtensionManager();
	}

	protected void reportErr(String msg, Exception e) {
		ChangelogPlugin.getDefault().getLog().log(
				new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID, IStatus.ERROR, msg, e));
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	protected IEditorPart openEditor(IFile diskresource) {
		IWorkbench ws = PlatformUI.getWorkbench();

		try {
			return org.eclipse.ui.ide.IDE.openEditor(ws
					.getActiveWorkbenchWindow().getActivePage(), diskresource,
					true);
		} catch (PartInitException e) {
			e.printStackTrace();

			return null;
		}
	}

	protected IFile createChangeLog(IPath changelog) {
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		IWorkbench ws = PlatformUI.getWorkbench();

		final IFile changelog_File = myWorkspaceRoot.getFile(changelog);
		final InputStream initialContents = new ByteArrayInputStream(
				new byte[0]);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			@Override
			public void execute(IProgressMonitor monitor) throws CoreException {
				try {
					monitor.beginTask(Messages.getString("ChangeLog.AddingChangeLog"), 2000); //$NON-NLS-1$
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
			reportErr(Messages.getString("ChangeLog.ErrInterrupted"), e); // $NON-NLS-1$
			return null;
		} catch (InvocationTargetException e) {
			reportErr(Messages.getString("ChangeLog.ErrInvocation"), e); // $NON-NLS-1$
			return null;
		}

		// FIXME:  we should put this refreshLocal call into a thread (filed as bug #256180)
		try {
			IContainer changelogContainer = myWorkspaceRoot.getContainerForLocation(changelog);
			if (changelogContainer != null)
				changelogContainer.refreshLocal(2, null);
		} catch (CoreException e) {
			reportErr(Messages.getString("ChangeLog.ErrRefresh"), e); // $NON-NLS-1$
			return null;
		}

		return changelog_File;
	}

	protected IEditorPart askChangeLogLocation(String editorLoc) {
		IWorkbench ws = PlatformUI.getWorkbench();
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();

		IResource given_resource = myWorkspaceRoot.findMember(editorLoc);

		if (given_resource == null)
			return null;

		ChangeLogContainerSelectionDialog dialog = new ChangeLogContainerSelectionDialog(ws
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

				parent_dec = parent_dec.getParent();

				if (parent_dec == null) {
					break;
				}
			}
		}

		return null;
	}

	/**
	 * Find the ChangeLog for a file that is being removed.  It can't be found and
	 * it is possible that the directory it is in has also been removed.
	 *
	 * @param path Path of removed file
	 * @return ChangeLog editor part that must be used to report removed file
	 */
	protected IEditorPart getChangelogForRemovePath(IPath path) {
		IResource parent_resource = null;
		IPath loc_path = path;
		// Look from current loc up to find first folder that is still existing
		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		while (loc_path.segmentCount() > 0) {
			parent_resource = myWorkspaceRoot.findMember(loc_path);
			if (parent_resource != null)
				break;
			loc_path = loc_path.removeLastSegments(1);
		}

		if (parent_resource != null) {
			IResource parent_dec = parent_resource;

			while (parent_dec != null) {
				String parent_node = parent_dec.getFullPath().toOSString();
				parent_node = parent_node
						+ System.getProperty("file.separator") + pref_ChangeLogName; //$NON-NLS-1$

				IResource change_log_res = myWorkspaceRoot
						.findMember(parent_node);

				if (change_log_res != null) {
					IProject proj_loc = parent_resource.getProject();
					IPath modified_changelog_path = change_log_res
							.getFullPath().removeFirstSegments(1);
					IFile change_log_file = proj_loc
							.getFile(modified_changelog_path);

					return openEditor(change_log_file);
				}

				parent_dec = parent_dec.getParent();

				if (parent_dec == null) {
					break;
				}
			}
		}

		return null;
	}

	private IFile getDocumentIFile(IEditorPart currentEditor) {
		IEditorInput cc = currentEditor.getEditorInput();

		if (cc instanceof IFileEditorInput) {
			return ((IFileEditorInput) cc).getFile();
		}
		return null;
	}

	protected String getDocumentLocation(IEditorPart currentEditor,
			boolean appendRoot) {


		IFile loc = getDocumentIFile(currentEditor);
		IEditorInput cc = null;
		String WorkspaceRoot;

		IWorkspaceRoot myWorkspaceRoot = getWorkspaceRoot();
		WorkspaceRoot = myWorkspaceRoot.getLocation().toOSString();

		if (currentEditor instanceof MultiPageEditorPart) {
			Object ed = ((MultiPageEditorPart) currentEditor).getSelectedPage();
			if (ed instanceof IEditorPart)
				cc = ((IEditorPart) ed).getEditorInput();
			if (cc instanceof FileEditorInput)
				return (appendRoot) ? WorkspaceRoot + ((FileEditorInput) cc).getFile().getFullPath().toOSString() :
					((FileEditorInput) cc).getFile().getFullPath().toOSString();
		}

		cc = currentEditor.getEditorInput();


		if (cc == null)
			return "";

		if ((cc instanceof SyncInfoCompareInput)
				|| (cc instanceof CompareEditorInput)) {

			CompareEditorInput test = (CompareEditorInput) cc;
			if (test.getCompareResult() == null) {
				return "";
			} else if (test.getCompareResult() instanceof ICompareInput) {
				ITypedElement leftCompare = ((ICompareInput) test.getCompareResult())
				.getLeft();
				if (leftCompare instanceof IResourceProvider){
					String localPath = ((IResourceProvider)leftCompare).getResource().getFullPath().toString();
					if (appendRoot) {
						return WorkspaceRoot + localPath;
					}
					return localPath;
				}
			} else {
				if (appendRoot)
					return WorkspaceRoot + test.getCompareResult().toString();
				return test.getCompareResult().toString();
			}
		} else if (cc instanceof FileStoreEditorInput) {
			return ((FileStoreEditorInput)cc).getName();
		}



		if (appendRoot) {
			return WorkspaceRoot + loc.getFullPath().toOSString();
		} else if (loc != null) {
			return loc.getFullPath().toOSString();
		} else {
			return "";
		}
	}

	protected void loadPreferences() {
		IPreferenceStore store = ChangelogPlugin.getDefault()
		.getPreferenceStore();

		pref_AuthorName = store.getString("IChangeLogConstants.AUTHOR_NAME"); //$NON-NLS-1$
		pref_AuthorEmail = store.getString("IChangeLogConstants.AUTHOR_EMAIL"); //$NON-NLS-1$

		pref_Formatter = store
				.getString("IChangeLogConstants.DEFAULT_FORMATTER"); // $NON-NLS-1$
	}

}
