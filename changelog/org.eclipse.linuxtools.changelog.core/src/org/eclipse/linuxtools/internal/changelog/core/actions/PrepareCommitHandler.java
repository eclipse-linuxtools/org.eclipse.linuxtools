/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class PrepareCommitHandler extends ChangeLogAction implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) {

        IRunnableWithProgress code = monitor -> loadClipboard(monitor);

        ProgressMonitorDialog pd = new ProgressMonitorDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell());

        try {
            pd.run(false /* fork */, false /* cancelable */, code);
		} catch (InvocationTargetException|InterruptedException e) {
			ChangelogPlugin.getDefault().getLog().log(Status.error(e.getMessage(), e));
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

        if (currentEditor == null) {
            return;
        }

        IFile changelog = getChangelogFile(getDocumentLocation(currentEditor, false));
        if (changelog == null) {
            return;
        }

        String diffResult = "";
        IProject project = null;
        IResource[] resources = new IResource[] { changelog };
        project = changelog.getProject();

        RepositoryProvider r = RepositoryProvider.getProvider(project);
        if (r == null) {
            return; // There is no repository provider for this project, i.e
                    // it's not shared.
        }
        SyncInfoSet set = new SyncInfoSet();
        Subscriber s = r.getSubscriber();
        try {
            s.refresh(resources, IResource.DEPTH_ZERO, monitor);
        } catch (TeamException e1) {
            // Ignore, continue anyways
        }
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

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {

    }

}
