/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ICompletable;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;


/**
 *
 * Build and hold completion metadata for Systemtap. This originally is generated from stap coverage data
 *
 */
public final class STPMetadataSingleton {

    public static TreeNode[] NO_MATCHES = new TreeNode[0];

    private static STPMetadataSingleton instance = null;

    private STPMetadataSingleton() {}

    public static STPMetadataSingleton getInstance() {
        if (instance == null) {
            instance = new STPMetadataSingleton();
        }
        return instance;
    }

    public TreeNode[] getFunctionCompletions(String prefix) {
        TreeNode node = TapsetLibrary.getFunctions();
        return getMatchingChildren(node, prefix);
    }

    public TreeNode[] getProbeCompletions(String prefix) {
        List<TreeNode> matches = new LinkedList<>();
        String groupName = extractProbeGroupName(prefix);

        for (TreeNode node : TapsetLibrary.getProbeCategoryNodes()) {
            TreeNode groupNode = node.getChildByName(groupName);
            if (groupNode != null) {
                node = groupNode;
            }

            matches.addAll(Arrays.asList(getMatchingChildren(node, prefix)));
        }

        return !matches.isEmpty() ? matches.toArray(new TreeNode[matches.size()]) : NO_MATCHES;
    }

    /**
     * Returns a list of variables available in the given probe.
     * @param probe The probe for which to find variables
     * @param prefix The prefix to complete.
     * @return a list of variables matching the prefix.
     */
    public TreeNode[] getProbeVariableCompletions(String probe, String prefix) {
        // The only probes that may have avilable variables are non-static ones.
        TreeNode node = getProbeAlias(probe);
        return node != null ? getMatchingChildren(node, prefix) : NO_MATCHES;
    }

    public boolean isVariableInProbe(String probe, String variable) {
        TreeNode node = getProbeAlias(probe);
        if (node == null) {
            return false;
        }
        for (int i = 0, n = node.getChildCount(); i < n; i++) {
            TreeNode child = node.getChildAt(i);
            String nodeVar = ((ProbevarNodeData) child.getData()).getCompletionText();
            if (nodeVar.equals(variable)) {
                return true;
            }
        }
        return false;
    }

    public TreeNode getProbeAlias(String probe) {
        TreeNode node = TapsetLibrary.getProbeAliases();
        if (node == null) {
            return null;
        }

        node = node.getChildByName(extractProbeGroupName(probe));
        if (node == null) {
            return null;
        }

        return node.getChildByName(probe);
    }

    private TreeNode[] getMatchingChildren(TreeNode node, String prefix) {
        ArrayList<TreeNode> matches = new ArrayList<>();

        for (int i = 0, n = node.getChildCount(); i < n; i++) {
            TreeNode childNode = node.getChildAt(i);
            String childName;
            if (childNode.getData() instanceof ICompletable) {
                childName = ((ICompletable) childNode.getData()).getCompletionText();
            } else {
                childName = childNode.toString();
            }
            if (childName.startsWith(prefix)) {
                matches.add(childNode);
            }
        }

        return matches.toArray(new TreeNode[0]);
    }

    private String extractProbeGroupName(String probeName) {
        int dotIndex = probeName.indexOf('.');
        int parenIndex = probeName.indexOf('(');
        if (dotIndex > 0 && parenIndex > 0) {
            return probeName.substring(0, Math.min(dotIndex, parenIndex));
        }
        if (dotIndex > 0) {
            return probeName.substring(0, dotIndex);
        }
        if (parenIndex > 0) {
            return probeName.substring(0, parenIndex);
        }
        return probeName;
    }

}
