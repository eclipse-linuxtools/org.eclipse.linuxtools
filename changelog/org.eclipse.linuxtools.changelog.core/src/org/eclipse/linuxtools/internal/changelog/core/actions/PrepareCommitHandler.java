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
package org.eclipse.linuxtools.internal.changelog.core.actions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.internal.changelog.core.LineComparator;
import org.eclipse.linuxtools.internal.changelog.core.Messages;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IContributorResourceAdapter2;

public class PrepareCommitHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {

		IRunnableWithProgress code = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) {
				// monitor.beginTask("Loading Clipboard", 1000);
				loadClipboard(monitor);
				// monitor.done();
			}
		};

		ProgressMonitorDialog pd = new ProgressMonitorDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());

		try {
			pd.run(false /* fork */, false /* cancelable */, code);
		} catch (InvocationTargetException e) {
			ChangelogPlugin
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID,
							IStatus.ERROR, e.getMessage(), e));
		} catch (InterruptedException e) {
			ChangelogPlugin
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, ChangelogPlugin.PLUGIN_ID,
							IStatus.ERROR, e.getMessage(), e));
		}

		return null;
	}

	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o,
					ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
		}

		return null;
	}

	private void loadClipboard(IProgressMonitor monitor) {

		IEditorPart currentEditor;

		try {
			currentEditor = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
		} catch (Exception e) {
			// no editor is active now so do nothing

			return;
		}

		if (currentEditor == null)
			return;

		// System.out.println(currentEditor.getTitle());
		String diffResult = "";
		IEditorInput input = currentEditor.getEditorInput();
		ResourceMapping mapping = getResourceMapping(input);
		IProject project = null;
		IResource[] resources = new IResource[1];

		if (mapping != null) {
			project = mapping.getProjects()[0];
			resources[0] = (IResource) mapping.getModelObject();
		} else if (input instanceof IFileEditorInput) {
			IFileEditorInput f = (IFileEditorInput) input;
			project = f.getFile().getProject();
			resources[0] = f.getFile();
		} else {
			return; // can't get what we need
		}

		RepositoryProvider r = RepositoryProvider.getProvider(project);
		if (r == null) {
			return; // There is no repository provider for this project, i.e
					// it's not shared.
		}
		SyncInfoSet set = new SyncInfoSet();
		Subscriber s = r.getSubscriber();
		s.collectOutOfSync(resources, IResource.DEPTH_ZERO, set, monitor);
		SyncInfo[] infos = set.getSyncInfos();

		if (infos.length == 1) {
			int kind = SyncInfo.getChange(infos[0].getKind());
			if (kind == SyncInfo.CHANGE) {
				try {
					IDiff d = s.getDiff(infos[0].getLocal());
					if (d instanceof IThreeWayDiff
							&& ((IThreeWayDiff) d).getDirection() == IThreeWayDiff.OUTGOING) {
						IThreeWayDiff diff = (IThreeWayDiff) d;
						monitor.beginTask(null, 100);
						IResourceDiff localDiff = (IResourceDiff) diff
								.getLocalChange();
						IFile file = (IFile) localDiff.getResource();
						monitor.subTask(Messages
								.getString("ChangeLog.MergingDiffs")); // $NON-NLS-1$
						String osEncoding = file.getCharset();
						IFileRevision ancestorState = localDiff
								.getBeforeState();
						IStorage ancestorStorage;
						if (ancestorState != null)
							ancestorStorage = ancestorState.getStorage(monitor);
						else {
							ancestorStorage = null;
							return;
						}

						try {
							LineComparator left = new LineComparator(
									ancestorStorage.getContents(), osEncoding);
							LineComparator right = new LineComparator(
									file.getContents(), osEncoding);
							for (RangeDifference tmp : RangeDifferencer
									.findDifferences(left, right)) {
								if (tmp.kind() == RangeDifference.CHANGE) {
									LineNumberReader l = new LineNumberReader(
											new InputStreamReader(
													file.getContents()));
									int rightLength = tmp.rightLength() > 0 ? tmp
											.rightLength()
											: tmp.rightLength() + 1;
									String line0 = null;
									String preDiffResult = "";
									for (int i = 0; i < tmp.rightStart(); ++i) {
										// We have equivalence at the start.
										// This could be due to a new entry with
										// the
										// same date stamp as the subsequent
										// entry. In this case, we want the diff
										// to
										// have the date stamp at the top so it
										// forms a complete entry. So, we cache
										// those equivalent lines for later
										// usage if needed.
										try {
											String line = l.readLine();
											if (line0 == null)
												line0 = line;
											preDiffResult += line + "\n";
										} catch (IOException e) {
											break;
										}
									}
									for (int i = 0; i < rightLength; ++i) {
										try {
											String line = l.readLine();
											// If the last line of the diff
											// matches the first line of the old
											// file and
											// there was equivalence at the
											// start of the ChangeLog, then we
											// want to put
											// the equivalent section at top so
											// as to give the best chance of
											// forming
											// a ChangeLog entry that can be
											// used as a commit comment.
											if (i == rightLength
													- tmp.rightStart()) {
												if (tmp.rightStart() != 0
														&& line.equals(line0)) {
													diffResult = preDiffResult += diffResult;
													i = rightLength; // stop
																		// loop
												} else
													diffResult += line + "\n";
											} else
												diffResult += line + "\n"; // $NON-NLS-1$
										} catch (IOException e) {
											// do nothing
										}
									}
								}
							}
						} catch (UnsupportedEncodingException e) {
							// do nothing for now
						}
						monitor.done();
					}
				} catch (CoreException e) {
					// do nothing
				}
			}
		}

		if (!diffResult.equals(""))
			populateClipboardBuffer(diffResult);
	}

	private void populateClipboardBuffer(String input) {

		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench()
				.getDisplay());
		clipboard.setContents(new String[] { input },
				new Transfer[] { plainTextTransfer });
		clipboard.dispose();
	}

}
