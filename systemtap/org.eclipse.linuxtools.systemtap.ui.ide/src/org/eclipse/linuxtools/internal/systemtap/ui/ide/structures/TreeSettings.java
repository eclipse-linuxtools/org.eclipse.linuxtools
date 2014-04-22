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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.StapTreeDataFactory;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;


/**
 * Handles access to the cached stap tapset library tree, including reading the cache back from disk
 * on startup, writing the cache to disk when the cache is initially generated, checking to make sure
 * that the cache is up-to-date, and providing accessor methods to the rest of the IDE that allow other
 * classes to use the cached tree data.
 * @author Ryan Morse
 */
public final class TreeSettings {
    private static final String FILE_NAME = "TreeSettings"; //$NON-NLS-1$
    private static final String FILE_DIRECTORY = ".systemtapgui"; //$NON-NLS-1$

    private static final String M_DISP = "display"; //$NON-NLS-1$
    private static final String M_DATA = "data"; //$NON-NLS-1$
    private static final String M_DATATYPE = "datatype"; //$NON-NLS-1$
    private static final String M_DEFINITON = "definition"; //$NON-NLS-1$
    private static final String M_CLICKABLE = "clickable"; //$NON-NLS-1$
    private static final String M_NULL = "<null>"; //$NON-NLS-1$
    private static final String M_ITEM = "item"; //$NON-NLS-1$

    private static final String T_FUNCTIONS = "functionTree"; //$NON-NLS-1$
    private static final String T_PROBES = "probeTree"; //$NON-NLS-1$
    private static final String T_DATE = "modifiedDate"; //$NON-NLS-1$
    private static final String T_VERSION = "version"; //$NON-NLS-1$
    private static final String VERSION_NUMBER = "3.0"; //$NON-NLS-1$

    private static TreeNode cachedFunctions;
    private static TreeNode cachedProbes;
    private static File settingsFile = null;

    private TreeSettings() {}

    /**
     * Deletes the Function and Probe Alias trees that have been saved to the filesystem
     * as an {@link IMemento}.
     * @return <code>true</code> if the delete attempt succeeded, <code>false</code> otherwise.
     */
    static boolean deleteTrees() {
        boolean deleted;
        try {
            deleted = settingsFile.delete();
        } catch (SecurityException e) {
            deleted = false;
        }
        if (deleted) {
            clearCachedTrees();
        }
        return deleted;
    }

    /**
     * Saves the provided Function and Probe Alias trees into an {@link IMemento} on
     * the filesystem. <p>
     * Note: Both trees must be saved at the same time to better ensure that they
     * are both obtained from the same tapset state.
     * @param functions The Function tree to store in cache.
     * @param probes The Probe Alias tree to store in cache.
     * @return <code>true</code> if the caching is successful.
     */
    public static synchronized boolean setTrees(TreeNode functions, TreeNode probes) {
        if (functions == null || probes == null || !isTreeFileAvailable()) {
            return false;
        }

        XMLMemento data = XMLMemento.createWriteRoot(FILE_NAME);
        writeTree(data, T_FUNCTIONS, functions);
        writeTree(data, T_PROBES, probes);

        data.createChild(T_DATE)
            .putTextData((Long.valueOf(Calendar.getInstance().getTimeInMillis())).toString());

        data.createChild(T_VERSION).putTextData(VERSION_NUMBER);

        try (FileWriter writer = new FileWriter(settingsFile)) {
            data.save(writer);
        } catch (IOException e) {
            return false;
        }

        clearCachedTrees();
        return true;
    }

    /**
     * Writes the tree passed in to the {@link IMemento} argument.
     * @param data The {@link IMemento} to store the tree to.
     * @param name The name to give to the <code>parent</code> node.
     * @param tree The {@link TreeNode} to store.
     */
    private static void writeTree(IMemento data, String name, TreeNode tree) {
        IMemento child = data.createChild(name);
        child.putString(M_DISP, tree.toString());
        Object treeData = tree.getData();
        if (treeData != null) {
            child.putString(M_DATA, treeData.toString());
            child.putString(M_DATATYPE, StapTreeDataFactory.getDataObjectID(treeData));
        }
        if (tree instanceof TreeDefinitionNode) {
            child.putString(M_DEFINITON,
                    getStringFromValue(((TreeDefinitionNode) tree).getDefinition()));
        }
        child.putBoolean(M_CLICKABLE, tree.isClickable());
        for (int i = 0, n = tree.getChildCount(); i < n; i++) {
            writeTree(child, M_ITEM, tree.getChildAt(i));
        }
    }

    private static void clearCachedTrees() {
        if (cachedFunctions != null) {
            cachedFunctions.dispose();
            cachedFunctions = null;
        }
        if (cachedProbes != null) {
            cachedProbes.dispose();
            cachedProbes = null;
        }
    }

    /**
     * Allows access to the Tapset Function tree, which contains information about all
     * functions stored in the tapset library.
     * @return The {@link TreeNode} root of the Function tree.
     * @since 2.0
     */
    public static synchronized TreeNode getFunctionTree() {
        if (cachedFunctions == null) {
            cachedFunctions = readData(T_FUNCTIONS);
        }
        return cachedFunctions;
    }

    /**
     * Allows access to the Tapset Probe Alias tree, which contains a list of all probe aliases
     * in the tapset library.
     * @return The {@link TreeNode} root of the Probe Alias tree.
     * @since 2.0
     */
    public synchronized static TreeNode getProbeTree() {
        if (cachedProbes == null) {
            cachedProbes = readData(T_PROBES);
        }
        return cachedProbes;
    }

    /**
     * Reads the contents of the cached memento to recreate the stored trees.
     * @return True if the read is successful.
     */
    private static TreeNode readData(String section) {
        IMemento data = getTreeFileMemento();
        if (data == null) {
            return null;
        }
        return readTree(data.getChild(section));
    }

    /**
     * Opposite action as writeTree. Reconstruct a tree from a previously-saved {@link IMemento}.
     * @param data The {@link IMemento} to read the tree out of.
     * @return The reconstructed {@link TreeNode}.
     */
    private static TreeNode readTree(IMemento data) {
        String disp = data.getString(M_DISP);
        String def = data.getString(M_DEFINITON);
        boolean c = data.getBoolean(M_CLICKABLE);
        Object d = StapTreeDataFactory.createObjectFromString(data.getString(M_DATA), data.getString(M_DATATYPE));

        TreeNode parent;
        if (def == null) {
            parent = new TreeNode(d, disp, c);
        } else {
            parent = new TreeDefinitionNode(d, disp, getValueFromString(def), c);
        }
        for (IMemento child : data.getChildren()) {
            parent.add(readTree(child));
        }
        return parent;
    }

    /**
     * Returns the modification date for the tree file.
     * Use this to make sure that the cache is not out of date.
     * @return The datestamp for the Tree file.
     */
    public synchronized static long getTreeFileDate() {
        IMemento data = getTreeFileMemento();
        if (data != null) {
            IMemento child = data.getChild(T_DATE);
            try {
                return Long.parseLong(child.getTextData());
            } catch (NumberFormatException e) {}
        }
        return -1;
    }

    private static IMemento getTreeFileMemento() {
        if (!isTreeFileAvailable()) {
            return null;
        }

        try (FileReader reader = new FileReader(settingsFile)) {
            IMemento data = XMLMemento.createReadRoot(reader, FILE_NAME);
            IMemento versionChild = data.getChild(T_VERSION);
            if (versionChild != null && versionChild.getTextData().equals(VERSION_NUMBER)) {
                return data;
            }
            return null;
        } catch (IOException | WorkbenchException fnfe) {
            return null;
        }
    }

    private static boolean isTreeFileAvailable() {
        if (settingsFile != null) {
            return true;
        }

        IPath path = new Path(System.getenv("HOME")). //$NON-NLS-1$
                append(FILE_DIRECTORY).append(FILE_NAME).
                addFileExtension("xml"); //$NON-NLS-1$
        settingsFile = path.toFile();

        try {
            if (!settingsFile.exists()){
                // Create a new settings file-and its parent
                // directories- if one does not exist.
                settingsFile.getParentFile().mkdirs();
                settingsFile.createNewFile();
            }
        } catch (IOException ioe) {
            return false;
        }

        return true;
    }

    private static String getStringFromValue(String val) {
        return val == null ? M_NULL : val;
    }

    private static String getValueFromString(String string) {
        return M_NULL.equals(string) ? null : string;
    }
}
