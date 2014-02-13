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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

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
	private TreeSettings() {
	}
	/**
	 * Returns the modification date for the Tree File. Used to make sure that the cache is not out of
	 * date.
	 * @return The datestamp for the Tree file.
	 */
	public static long getTreeFileDate() {
		if (!readData()) {
			return -1;
		}
		return treeFileDate;
	}

	/**
	 * Allows access to the Tapset Function tree, which contains information about all
	 * functions stored in the tapset library.
	 * @return The <code>TreeNode</code> root of the Function tree.
	 * @since 2.0
	 */
	public static TreeNode getFunctionTree() {
		if (!readData()) {
			return null;
		}
		return functions;
	}

	/**
	 * Allows access to the Tapset Probe Alias tree, which contains a list of all probe aliases
	 * in the tapset library.
	 * @return The <code>TreeNode</code> root of the Probe Alias tree.
	 * @since 2.0
	 */
	public static TreeNode getProbeTree() {
		if (!readData()) {
			return null;
		}
		return probes;
	}

	/**
	 * Sets the Probe Alias and Function trees that are being cached to the trees given as arguments.
	 * @param func The Function tree to store in cache.
	 * @param probe The Probe Alias tree to store in cache.
	 * @return True if the caching is successful.
	 * @since 2.0
	 */
	public static boolean setTrees(TreeNode func, TreeNode probe) {
		if (null == func || null == probe) {
			return false;
		}
		functions = func;
		probes = probe;
		return writeData();
	}

	/**
	 * Reads the contents of the cache file into memory.
	 * @return True if the read is successful.
	 */
	private static boolean readData() {
		if (null == settingsFile && !openFile()) {
			return false;
		}

		try (FileReader reader = new FileReader(settingsFile)) {
			if(!reader.ready()) {
				reader.close();
				return false;
			}

			XMLMemento data = XMLMemento.createReadRoot(reader, "TreeSettings"); //$NON-NLS-1$

			IMemento child = data.getChild("functionTree"); //$NON-NLS-1$
			String s = child.getString("string"); //$NON-NLS-1$
			if ("<null>".equals(s)) { //$NON-NLS-1$
				s = null;
			}
			String d = child.getString("data"); //$NON-NLS-1$
			if ("<null>".equals(d)) { //$NON-NLS-1$
				d = null;
			}

			functions = new TreeNode(d, s, false);
			readTree(child, functions, 0);

			child = data.getChild("probeTree"); //$NON-NLS-1$
			s = child.getString("string"); //$NON-NLS-1$
			if ("<null>".equals(s)) { //$NON-NLS-1$
				s = null;
			}
			d = child.getString("data"); //$NON-NLS-1$
			if ("<null>".equals(d)) { //$NON-NLS-1$
				d = null;
			}
			probes = new TreeNode(d, s, false);
			readTree(child, probes, 0);

			child = data.getChild("modifiedDate"); //$NON-NLS-1$
			treeFileDate = Long.parseLong(child.getString("date")); //$NON-NLS-1$
		} catch(IOException|WorkbenchException fnfe) {
			return false;
		}

		return true;
	}

	/**
	 * Writes the tree data currently stored by this class to disk for later access.
	 * @return True if the write is successful.
	 */
	private static boolean writeData() {
		if (null == settingsFile && !openFile()) {
			return false;
		}

		try {
			XMLMemento data = XMLMemento.createWriteRoot("TreeSettings"); //$NON-NLS-1$

			IMemento child = data.createChild("functionTree"); //$NON-NLS-1$
			writeTree(child, functions, 0);

			child = data.createChild("probeTree"); //$NON-NLS-1$
			writeTree(child, probes, 0);

			child = data.createChild("modifiedDate"); //$NON-NLS-1$
			child.putString("date", (Long.valueOf(Calendar.getInstance().getTimeInMillis())).toString()); //$NON-NLS-1$

			FileWriter writer = new FileWriter(settingsFile);
			data.save(writer);
		} catch(FileNotFoundException fnfe) {
			return false;
		} catch(IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Writes the tree passed in to the <code>IMemento</code> argument, up to the specified depth.
	 * @param child The <code>IMemento</code> to store the tree to.
	 * @param tree The <code>TreeNode</code> to store.
	 * @param depth The maximum depth level to write out.
	 */
	private static void writeTree(IMemento child, TreeNode tree, int depth) {
		if (null == tree.toString()) {
			child.putString("string", "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			child.putString("string", tree.toString()); //$NON-NLS-1$
		}

		if (null == tree.getData()) {
			child.putString("data", "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			child.putString("data", tree.getData().toString()); //$NON-NLS-1$
		}

		if (tree instanceof TreeDefinitionNode) {
			if (null == ((TreeDefinitionNode) tree).getDefinition()) {
				child.putString("definition", "<null>"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				child.putString(
						"definition", ((TreeDefinitionNode) tree).getDefinition()); //$NON-NLS-1$
			}
		}

		child.putInteger("click", (tree.isClickable()?1:0)); //$NON-NLS-1$
		for(int i=0; i<tree.getChildCount(); i++) {
			writeTree(child.createChild("level" + depth), tree.getChildAt(i), depth+1); //$NON-NLS-1$
		}
	}

	/**
	 * Opposite action as writeTree. Reads the <code>IMemento</code> passed in into the <code>TreeNode</code>
	 * up to the requested maximum depth.
	 * @param data The <code>IMemento</code> to read the tree out of.
	 * @param parent The <code>TreeNode</code> to store the tree in.
	 * @param depth The maximum depth to read.
	 */
	private static void readTree(IMemento data, TreeNode parent, int depth) {
		IMemento[] children = data.getChildren("level" + depth); //$NON-NLS-1$

		try {
			if(null != children) {
				for(int i=0; i<children.length; i++) {
					String s = children[i].getString("string"); //$NON-NLS-1$
					String d = children[i].getString("data"); //$NON-NLS-1$
					String def = children[i].getString("definition"); //$NON-NLS-1$

					boolean c = ((0==children[i].getInteger("click").intValue())?false:true); //$NON-NLS-1$

					if ("<null>".equals(s)) { //$NON-NLS-1$
						s = null;
					}
					if ("<null>".equals(d)) { //$NON-NLS-1$
						d = null;
					}

					TreeNode t;
					if(null == def) {
						t = new TreeNode(d, s, c);
					} else {
						if ("<null>".equals(def)) { //$NON-NLS-1$
							def = null;
						}

						t = new TreeDefinitionNode(d, s, def, c);
					}
					parent.add(t);

					readTree(children[i], t, depth+1);
				}
			}
		} catch(NullPointerException e) {
		}
	}

	private static boolean openFile() {
		settingsFile = new File(System.getenv("HOME") + "/.systemtapgui/" + fileName); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			if (!settingsFile.exists()){
				// Create a new settings file-and its parent
				// directories- if one does not exist.
				settingsFile.getParentFile().mkdirs();
				settingsFile.createNewFile();
			}
		} catch(IOException ioe) {
			return false;
		}

		return true;
	}

	private static long treeFileDate;
	private static TreeNode functions;
	private static TreeNode probes;
	private static final String fileName = "/TreeSettings.xml"; //$NON-NLS-1$
	private static File settingsFile = null;
}
