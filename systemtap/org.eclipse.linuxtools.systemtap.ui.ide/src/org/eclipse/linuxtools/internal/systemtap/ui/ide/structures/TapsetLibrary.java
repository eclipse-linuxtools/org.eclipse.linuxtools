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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.FunctionParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.ProbeParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers.SharedParser;
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

    private static FunctionParser functionParser = FunctionParser.getInstance();
    private static ProbeParser probeParser = ProbeParser.getInstance();

    private static boolean initialized = false;

    public static TreeNode getProbes() {
        return probeParser.getTree();
    }

    public static TreeNode getStaticProbes() {
        return getProbes().getChildByName(Messages.ProbeParser_staticProbes);
    }

    public static TreeNode getProbeAliases() {
        return getProbes().getChildByName(Messages.ProbeParser_aliasProbes);
    }

    public static TreeNode[] getProbeCategoryNodes() {
        return new TreeNode[] {getStaticProbes(), getProbeAliases()};
    }

    public static TreeNode getFunctions() {
        return functionParser.getTree();
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
            if (property.equals(IDEPreferenceConstants.P_TAPSETS)) {
                applyTapsetChanges((String) event.getOldValue(), (String) event.getNewValue());
            } else if (property.equals(PreferenceConstants.P_ENV.SYSTEMTAP_TAPSET.toPrefKey())
                    || property.equals(IDEPreferenceConstants.P_REMOTE_PROBES)) {
                runStapParser();
            } else if (property.equals(IDEPreferenceConstants.P_STORED_TREE)) {
                if (event.getNewValue().equals(false)) {
                    // When turning off stored trees, reload the tapset contents directly.
                    TreeSettings.deleteTrees();
                    runStapParser();
                } else if (isReady()) {
                    // When turning on stored trees, store the current trees immediately.
                    TreeSettings.setTrees(getFunctions(), getProbes());
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
            if (event.getResult().isOK()) {
                if (isReady() && IDEPlugin.getDefault().getPreferenceStore().
                        getBoolean(IDEPreferenceConstants.P_STORED_TREE)) {
                    TreeSettings.setTrees(getFunctions(), getProbes());
                }

                if (event.getJob() instanceof ProbeParser) {
                    ManpageCacher.clear(TapsetItemType.PROBE, TapsetItemType.PROBEVAR);
                } else {
                    ManpageCacher.clear(TapsetItemType.FUNCTION);
                }
            }
        }
    };

    private static boolean isReady() {
        IStatus probeResult = probeParser.getLatestResult();
        IStatus funcResult = functionParser.getLatestResult();
        return probeResult != null && funcResult != null
                && probeResult.isOK() && funcResult.isOK();
    }

    /**
     * This method will trigger the appropriate parsing jobs
     * to get the information directly from the files.
     * If the jobs are already in progess, they will be restarted.
     */
    public static void runStapParser() {
        stop();
        SharedParser.getInstance().clearTapsetContents();
        functionParser.schedule();
        probeParser.schedule();
    }

    private static void applyTapsetChanges(String oldTapsets, String newTapsets) {
        List<String> oldList = Arrays.asList(oldTapsets.split(File.pathSeparator));
        List<String> newList = Arrays.asList(newTapsets.split(File.pathSeparator));
        List<String> additions = new ArrayList<>(newList);
        additions.removeAll(oldList);
        additions.remove(""); //$NON-NLS-1$
        List<String> deletions = new ArrayList<>(oldList);
        deletions.removeAll(newList);
        deletions.remove(""); //$NON-NLS-1$
        String[] additionArray = additions.toArray(new String[additions.size()]);
        String[] deletionArray = deletions.toArray(new String[deletions.size()]);
        SharedParser.getInstance().clearTapsetContents();
        probeParser.runUpdate(additionArray, deletionArray);
        functionParser.runUpdate(additionArray, deletionArray);
    }

    /**
     * This method will get all of the tree information from the TreeSettings xml file.
     */
    public static void readTreeFile() {
        functionParser.setTree(TreeSettings.getFunctionTree());
        probeParser.setTree(TreeSettings.getProbeTree());
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
     * This method will stop all running tapset parsers, and will block
     * the calling thread until they have terminated.
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
