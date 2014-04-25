/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.handlers.PerfSaveStatsHandler;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.widgets.Display;

/**
 * This class handles the execution of the perf stat command
 * and stores the resulting data.
 */
public class StatData extends AbstractDataManipulator {

    private String prog;
    private String [] args;
    private int runCount;
    private String [] events;

    public StatData(String title, IPath workDir, String prog, String [] args, int runCount, String[] events) {
        super(title, workDir);
        this.prog = prog;
        this.args = args;
        this.runCount = runCount;
        this.events = events;
    }

    public StatData(String title, IPath workDir, String prog, String [] args, int runCount, String[] events, IProject project) {
        super(title, workDir, project);
        this.prog = prog;
        this.args = args;
        this.runCount = runCount;
        this.events = events;
    }

    @Override
    public void parse() {
        String file;
        try {
            String prefix = PerfPlugin.PERF_DEFAULT_STAT.replace('.', '-');
            file = Files.createTempFile(prefix, "").toString(); //$NON-NLS-1$
            String [] cmd = getCommand(this.prog, this.args, file);
            performCommand(cmd, file);
        } catch (IOException e) {
        }
    }

    protected String [] getCommand(String prog, String [] args, String file) {
        List<String> ret = new ArrayList<>(Arrays.asList(
                new String[] {"perf", "stat" })); //$NON-NLS-1$ //$NON-NLS-2$
        if (runCount > 1) {
            ret.add("-r"); //$NON-NLS-1$
            ret.add(String.valueOf(runCount));
        }
        if (events != null) {
            for (String event : events) {
                ret.add("-e"); //$NON-NLS-1$
                ret.add(event);
            }
        }
        ret.add("-o"); //$NON-NLS-1$
        ret.add(file);
        ret.add(prog);
        ret.addAll(Arrays.asList(args));
        return ret.toArray(new String [0]);
    }

    protected String getProgram () {
        return prog;
    }

    protected String [] getArguments () {
        return args;
    }

    /**
     * Save latest perf stat result under $workingDirectory/perf.stat. If file
     * already exists rename it to perf.old.stat, in order to keep a reference
     * to the previous session and be consistent with the way perf handles perf
     * report data files.
     */
    public void updateStatData() {
        URI curStatPathURI = null;
        URI oldStatPathURI = null;
        // build file name format
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PerfPlugin.PERF_COMMAND);
        stringBuilder.append("%s."); //$NON-NLS-1$
        stringBuilder.append(PerfSaveStatsHandler.DATA_EXT);
        String statNameFormat = stringBuilder.toString();

        // get current stat file
        IPath workingDir = getWorkDir();
        String curStatName = String.format(statNameFormat, ""); //$NON-NLS-1$
        IPath curStatPath = workingDir.append(curStatName);
        IRemoteFileProxy proxy = null;
        try {
            curStatPathURI = new URI(curStatPath.toPortableString());
            proxy = RemoteProxyManager.getInstance().getFileProxy(curStatPathURI);

            IFileStore curFileStore = proxy.getResource(curStatPathURI.getPath());
            if (curFileStore.fetchInfo().exists()) {
                // get previous stat file
                String oldStatName = String.format(statNameFormat, ".old"); //$NON-NLS-1$
                IPath oldStatPath = workingDir.append(oldStatName);
                oldStatPathURI = new URI(oldStatPath.toPortableString());
                IFileStore oldFileStore = proxy.getResource(oldStatPathURI.getPath());
                if (oldFileStore.fetchInfo().exists()) {
                    oldFileStore.delete(EFS.NONE, null);
                }
                curFileStore.copy(oldFileStore, EFS.NONE, null);
                curFileStore.delete(EFS.NONE, null);
            }
            PerfSaveStatsHandler saveStats = new PerfSaveStatsHandler();
            saveStats.saveData(PerfPlugin.PERF_COMMAND);
        } catch (URISyntaxException|CoreException e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
        }
    }
}
