/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Neil Guzman - prepare/download sources implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RPMHandlerUtils;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.utils.DownloadJob;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.utils.RPMUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpecfileEditorPrepareSourcesActionDelegate extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final Shell shell = HandlerUtil.getActiveShellChecked(event);
        final SpecfileParser specparser = new SpecfileParser();
        final IResource resource = RPMHandlerUtils.getResource(event);
        final RPMProject rpj = RPMHandlerUtils.getRPMProject(resource);
        final IFile workFile = (IFile) rpj.getSpecFile();
        final Specfile specfile = specparser.parse(workFile);

        if (!downloadFile(shell, rpj, specfile)) {
            return null;
        }

        Job job = new Job("Preparing sources") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(
                        NLS.bind(Messages.PrepareSources_prepareSources, rpj.getSpecFile().getName()), IProgressMonitor.UNKNOWN);
                int offset = rpj.getSpecFile().getName().lastIndexOf("."); //$NON-NLS-1$
                MessageConsoleStream out = getConsole(
                        rpj.getSpecFile().getName().substring(0, offset))
                        .newMessageStream();
                IStatus is = null;
                try {
                    is = rpj.buildPrep(out);
                } catch (CoreException e) {
                    SpecfileLog.logError(Messages.PrepareSources_coreException,
                            e);
                    RPMUtils.showErrorDialog(shell,
                            Messages.PrepareSources_error,
                            Messages.PrepareSources_coreException);
                    return is;
                } finally {
                    monitor.done();
                }
                return is;
            }
        };
        job.setUser(true); // suppress UI. That's done in encapsulated
        job.schedule();
        return null;
    }

    public boolean downloadFile(Shell shell, RPMProject rpj, Specfile specfile) {
        // retrieve source(s) from specfile
        final List<SpecfileSource> sourceURLList = specfile != null ? (List<SpecfileSource>) specfile
                .getSources() : null;
        for (final SpecfileSource sourceurls : sourceURLList) {
            try {
                String resolvedURL = UiUtils.resolveDefines(specfile, sourceurls.getFileName());
                URL url = null;
                try {
                    url = new URL(resolvedURL);
                } catch (MalformedURLException e) {
                    SpecfileLog.logError(
                            NLS.bind(Messages.PrepareSources_downloadSourcesMalformedURL, resolvedURL), e);
                    RPMUtils.showErrorDialog(shell,
                            Messages.PrepareSources_error,
                            NLS.bind(Messages.PrepareSources_downloadSourcesMalformedURL, resolvedURL));
                    return false;
                }

                URLConnection connection = url.openConnection();

                if (!(connection instanceof HttpURLConnection) ||
                        ((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                    connection.connect();
                    // grab the name of the file from the URL
                    int offset = url.toString().lastIndexOf("/"); //$NON-NLS-1$
                    String filename = url.toString().substring(offset + 1);

                    // create the path to the "to be downloaded" file
                    IFile file = rpj.getConfiguration().getSourcesFolder()
                            .getFile(new Path(filename));

                    Job downloadJob = new DownloadJob(file, connection);
                    downloadJob.setUser(true);
                    downloadJob.schedule();
                    try {
                        downloadJob.join();
                    } catch (InterruptedException e1) {
                        return false;
                    }
                    if (!downloadJob.getResult().isOK()) {
                        return false;
                    }
                }
            } catch (OperationCanceledException e) {
                SpecfileLog.logError(Messages.PrepareSources_downloadCancelled,
                        e);
                RPMUtils.showErrorDialog(shell, Messages.PrepareSources_error,
                        Messages.PrepareSources_downloadCancelled);
                return false;
            } catch (IOException e) {
                SpecfileLog.logError(
                        Messages.PrepareSources_downloadConnectionFail, e);
                RPMUtils.showErrorDialog(shell, Messages.PrepareSources_error,
                        Messages.PrepareSources_downloadConnectionFail);
                return false;
            }
        }
        return true;
    }

    /**
     * Get the console.
     *
     * @param packageName
     *            The name of the package(RPM) this console will be for.
     * @return A console instance.
     */
    public MessageConsole getConsole(String packageName) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        String projectConsoleName = NLS.bind(Messages.PrepareSources_consoleName, packageName);
        MessageConsole ret = null;
        for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles()) {
            if (cons.getName().equals(projectConsoleName)) {
                ret = (MessageConsole) cons;
            }
        }
        // no existing console, create new one
        if (ret == null) {
            ret = new MessageConsole(projectConsoleName, null, null, true);
        }
        conMan.addConsoles(new IConsole[] { ret });
        ret.clearConsole();
        ret.activate();
        return ret;
    }

}
