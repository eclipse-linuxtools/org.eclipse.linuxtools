/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.io.File;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.PreferenceConstants;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;



/**
 * This class is used for obtaining all probes and functions from the tapsets.
 * If stored tapsets are in use, it will try to obtain the list from the TreeSettings memento.
 * Otherwise, or if there is a problem with the memento, it will instead run the TapsetParsers
 * in order to obtain tapset information.
 * @author Ryan Morse
 */
public final class TapsetLibrary {
    private static TreeNode functionTree = null;
    private static TreeNode probeTree = null;

    private static FunctionParser functionParser = FunctionParser.getInstance();
    private static ProbeParser probeParser = ProbeParser.getInstance();

    private static boolean initialized = false;

    public static TreeNode getProbes() {
        return probeTree;
    }

    public static TreeNode getStaticProbes() {
        return probeTree == null ? null : probeTree.getChildByName(Messages.ProbeParser_staticProbes);
    }

    public static TreeNode getProbeAliases() {
        return probeTree == null ? null : probeTree.getChildByName(Messages.ProbeParser_aliasProbes);
    }

    public static TreeNode[] getProbeCategoryNodes() {
        return new TreeNode[] {getStaticProbes(), getProbeAliases()};
    }

    public static TreeNode getFunctions() {
        return functionTree;
    }

    /**
     * Initialize all listeners associated with loading tapset contents, and perform
     * the first tapset load operation. Note that subsequent calls to this method will have no effect.
     */
    public synchronized static void init() {
        if (!initialized) {
            initialized = true;
            IPreferenceStore preferenceStore = IDEPlugin.getDefault().getPreferenceStore();
            preferenceStore.addPropertyChangeListener(propertyChangeListener);
            ConsoleLogPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(credentialChangeListener);

            functionParser.addJobChangeListener(parseCompletionListener);
            probeParser.addJobChangeListener(parseCompletionListener);

            if (preferenceStore.getBoolean(IDEPreferenceConstants.P_STORED_TREE)
                    && isTreeFileCurrent()) {
                readTreeFile();
            } else {
                runStapParser();
            }
        }
    }

    private static final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property.equals(IDEPreferenceConstants.P_TAPSETS)
                    || property.equals(PreferenceConstants.P_ENV.SYSTEMTAP_TAPSET.toPrefKey())
                    || property.equals(IDEPreferenceConstants.P_REMOTE_PROBES)) {
                runStapParser();
            } else if (property.equals(IDEPreferenceConstants.P_STORED_TREE)) {
                if (event.getNewValue().equals(false)) {
                    // When turning off stored trees, reload the tapset contents directly.
                    TreeSettings.deleteTrees();
                    runStapParser();
                } else {
                    // When turning on stored trees, store the current trees (if possible).
                    TreeSettings.setTrees(functionTree, probeTree);
                }
            }
        }
    };

    private static final IPropertyChangeListener credentialChangeListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            runStapParser();
        }
    };

    private static JobChangeAdapter parseCompletionListener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
            super.done(event);
            if (!event.getResult().isOK()) {
                return;
            }
            TreeTapsetParser parser = (TreeTapsetParser) event.getJob();
            if (parser.equals(functionParser)) {
                functionTree = parser.getTree();
            } else {
                probeTree = parser.getTree();
            }

            if (IDEPlugin.getDefault().getPreferenceStore().getBoolean(IDEPreferenceConstants.P_STORED_TREE)) {
                TreeSettings.setTrees(functionTree, probeTree);
            }
            synchronized (parser) {
                parser.notifyAll();
            }
        }
    };

    /**
     * This method will trigger the appropriate parsing jobs
     * to get the information directly from the files.
     * If the jobs are already in progess, they will be restarted.
     */
    public static void runStapParser() {
        stop();
        clearTrees();
        SharedParser.getInstance().clearTapsetContents();
        ManpageCacher.clear();
        functionParser.schedule();
        probeParser.schedule();
    }

    private static void clearTrees() {
        if (functionTree != null) {
            functionTree.dispose();
            functionTree = null;
        }
        if (probeTree != null) {
            probeTree.dispose();
            probeTree = null;
        }
    }

    /**
     * This method will get all of the tree information from
     * the TreeSettings xml file.
     */
    private static void readTreeFile() {
        functionTree = TreeSettings.getFunctionTree();
        probeTree = TreeSettings.getProbeTree();
    }

    /**
     * This method checks to see if the tapsets have changed
     * at all since the TreeSettings.xml file was created.
     * @return boolean indicating whether or not the TreeSettings.xml file has the most up-to-date version
     */
    private static boolean isTreeFileCurrent() {
        long treesDate = TreeSettings.getTreeFileDate();

        File f = getTapsetLocation();
        if (f == null || !checkIsCurrentFolder(treesDate, f)) {
            return false;
        }

        IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
        String[] tapsets = p.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);
        if (!tapsets[0].trim().isEmpty()) {
            for (int i = 0; i < tapsets.length; i++) {
                f = new File(tapsets[i]);
                if (!f.exists() || f.lastModified() > treesDate
                        || f.canRead() && !checkIsCurrentFolder(treesDate, f)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method attempts to locate the default tapset directory.
     * @return File representing the default tapset location, or
     * <code>null</code> if it cannot be found.
     */
    public static File getTapsetLocation() {
        final IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
        File f = attemptToGetFileFrom(p.getString(PreferenceConstants.P_ENV.SYSTEMTAP_TAPSET.toPrefKey()));
        if (f != null) {
            return f;
        }

        f = attemptToGetFileFrom(System.getenv(PreferenceConstants.P_ENV.SYSTEMTAP_TAPSET.toEnvKey()));
        if (f != null) {
            return f;
        }

        f = attemptToGetFileFrom("/usr/share/systemtap/tapset"); //$NON-NLS-1$
        if (f != null) {
            return f;
        }

        f = attemptToGetFileFrom("/usr/local/share/systemtap/tapset"); //$NON-NLS-1$
        if (f != null) {
            return f;
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                InputDialog i = new InputDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Localization.getString("TapsetBrowserView.TapsetLocation"), //$NON-NLS-1$
                        Localization.getString("TapsetBrowserView.WhereDefaultTapset"), null, null); //$NON-NLS-1$
                i.open();
                String path = i.getValue();
                if (path != null) {
                    // This preference update should trigger a property listener
                    // that will update the tapset trees.
                    p.setValue(PreferenceConstants.P_ENV.SYSTEMTAP_TAPSET.toPrefKey(), i.getValue());
                }
            }

        });
        return null;
    }

    private static File attemptToGetFileFrom(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        File f = new File(trimmed);
        return f.exists() ? f : null;
    }

    /**
     * This method checks the provided time stap against the folders
     * time stamp.  This is to see if the folder may have new data in it
     * @param time The current time stamp
     * @param folder The folder to check if it is newer the then time stamp
     * @return boolean indicating whether the time stamp is newer then the folder
     */
    private static boolean checkIsCurrentFolder(long time, File folder) {
        File[] fs = folder.listFiles();

        for (int i = 0; i < fs.length; i++) {
            if (fs[i].lastModified() > time) {
                return false;
            }

            if (fs[i].isDirectory() && fs[i].canRead()
                    && !checkIsCurrentFolder(time, fs[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Blocks the current thread until the parser has finished
     * parsing probes and functions.
     * @since 2.0
     */
    public static void waitForInitialization() {
        while (functionParser.getState() != Job.NONE) {
            try {
                synchronized (functionParser) {
                    functionParser.wait(5000);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        while (probeParser.getState() != Job.NONE) {
            try {
                synchronized (probeParser) {
                    probeParser.wait(5000);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * This method will stop all running tapset parsers, and will block
     * the calling thread until they have terminated.
     * @since 1.2
     */
    public static void stop() {
        functionParser.cancel();
        try {
            functionParser.join();
        } catch (InterruptedException e) {
            // The current thread was interrupted while waiting
            // for the parser thread to exit. Nothing to do
            // continue stopping.
        }
        probeParser.cancel();
        try {
            probeParser.join();
        } catch (InterruptedException e) {}
    }
}
