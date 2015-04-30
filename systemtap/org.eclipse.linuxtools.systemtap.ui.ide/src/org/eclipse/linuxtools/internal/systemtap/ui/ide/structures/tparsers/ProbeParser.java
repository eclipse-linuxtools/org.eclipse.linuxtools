/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.tparsers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.Messages;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * Runs stap -vp1 & stap -L in order to get all of the probes
 * that are defined in the tapsets.  Builds probeAlias trees
 * with the values obtained from the tapsets.
 *
 * @author Ryan Morse
 * @since 2.0
 */
public final class ProbeParser extends TreeTapsetParser {

    public static final String PROBE_REGEX = "(?s)(?<!\\w)probe\\s+{0}\\s*\\+?="; //$NON-NLS-1$
    private static final String TAPSET_PROBE_REGEX = "probe {0} \\+?="; //$NON-NLS-1$
    private static final String PROBE_FORM_CHECK_REGEX = "\\w+((\\(\\w+\\))?(\\.\\w+)?)*( \\$?\\w+:\\w+)*"; //$NON-NLS-1$
    private static final Pattern PROBE_GROUP_PATTERN = Pattern.compile("[^\\.\\(]+"); //$NON-NLS-1$

    private static ProbeParser parser = null;
    public static ProbeParser getInstance(){
        if (parser != null) {
            return parser;
        }
        parser = new ProbeParser();
        return parser;
    }

    private ProbeParser() {
        super(Messages.ProbeParser_name);
    }

    /**
     * @param tree To be valid, the first-level children of this tree must
     * be two nodes respectively named "Static Probes" and "Probe Alias".
     */
    @Override
    protected boolean isValidTree(TreeNode tree) {
        return super.isValidTree(tree) &&
                tree.getChildByName(Messages.ProbeParser_staticProbes) != null
                && tree.getChildByName(Messages.ProbeParser_aliasProbes) != null;
    }

    /**
     * Runs stap to collect all available tapset probes.
     * ProbeTree organized as:
     *    Root->Named Groups->ProbePoints->Variables
     */
    @Override
    protected int runAction(IProgressMonitor monitor) {
        int result = addStaticProbes(monitor);
        if (result == IStatus.OK) {
            result = addProbeAliases(monitor);
        }
        return result;
    }

    /**
     * Runs stap to obtain a log of all static probes, and populate the probe tree with them.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    private int addStaticProbes(IProgressMonitor monitor) {
        TreeNode statics = new TreeNode(Messages.ProbeParser_staticProbes, false);
        tree.add(statics);
        if (monitor.isCanceled()) {
            return IStatus.CANCEL;
        }

        String probeDump = runStap(new String[]{"--dump-probe-types"}, null, false); //$NON-NLS-1$
        int result = verifyRunResult(probeDump);
        if (result != IStatus.OK) {
            return result;
        }
        if (!doQuickErrorCheck(probeDump)) {
            return IStatus.ERROR;
        }

        boolean canceled = false;
        try (Scanner st = new Scanner(probeDump)) {
            TreeNode groupNode = null;
            while (st.hasNextLine()) {
                if (monitor.isCanceled()) {
                    canceled = true;
                    break;
                }
                String tokenString = st.nextLine();
                groupNode = addOrFindProbeGroup(extractProbeGroupName(tokenString), groupNode, statics);
                groupNode.add(makeStaticProbeNode(tokenString));
            }
        }
        statics.sortTree();
        return !canceled ? IStatus.OK : IStatus.CANCEL;
    }

    /**
     * Runs stap to obtain a log of all probe aliases & their variables,
     * and populate the probe tree with them.
     * @return An {@link IStatus} severity level for the result of the operation.
     */
    private int addProbeAliases(IProgressMonitor monitor) {
        TreeNode statics = tree.getChildByName(Messages.ProbeParser_staticProbes);
        if (statics == null) {
            return IStatus.ERROR;
        }
        TreeNode aliases = new TreeNode(Messages.ProbeParser_aliasProbes, false);
        tree.add(aliases);
        if (monitor.isCanceled()) {
            return IStatus.CANCEL;
        }

        String probeDump = runStap(new String[]{"-L"}, "**", false); //$NON-NLS-1$ //$NON-NLS-2$
        int result = verifyRunResult(probeDump);
        if (result != IStatus.OK) {
            return result;
        }
        if (!doQuickErrorCheck(probeDump)) {
            return IStatus.ERROR;
        }

        boolean canceled = false;
        try (Scanner st = new Scanner(probeDump)) {
            TreeNode groupNode = null;
            while (st.hasNextLine()) {
                if (monitor.isCanceled()) {
                    canceled = true;
                    break;
                }
                String tokenString = st.nextLine();
                // If the token starts with '_' or '__' it is a private probe so
                // skip it.
                if (tokenString.startsWith("_")) { //$NON-NLS-1$
                    continue;
                }
                // Only add this group if it is not a static probe group
                String groupName = extractProbeGroupName(tokenString);
                if (statics.getChildByName(groupName) != null) {
                    continue;
                }
                groupNode = addSingleProbeAlias(tokenString, aliases, groupNode, groupName, null);
            }
        }
        aliases.sortTree();
        return !canceled ? IStatus.OK : IStatus.CANCEL;
    }

    /**
     * Performs a quick check of validity in a probe dump.
     * @param probeDump The output of a call to stap that prints a probe list.
     * @return <code>false</code> if the output of the dump is invalid.
     */
    private boolean doQuickErrorCheck(String probeDump) {
        if (probeDump == null) {
            return false;
        }
        // Check just the first probe printed
        try (Scanner scanner = new Scanner(probeDump)) {
            return Pattern.matches(PROBE_FORM_CHECK_REGEX, scanner.nextLine());
        }
    }

    /**
     * Adds a single probe alias to the collection.
     * @param probeLine A line of probe information printed by a call to "stap -L".
     * @param aliases The tree of probe aliases. The probe will be added to this tree.
     * @param groupNode For optimization, pass an existing group node here, as it will be used if the
     * probe belongs in it. Otherwise, or if <code>null</code> is passed, a new one will be created.
     * @param groupName The name of the probe group, or <code>null</code> if it is unknown at the time
     * this method is called.
     * @param definition The path of the file in which this probe is defined, or <code>null</code> if it
     * is unknown at the time this method is called.
     */
    private TreeNode addSingleProbeAlias(String probeLine, TreeNode aliases, TreeNode groupNode,
            String groupName, String definition) {
        StringTokenizer probeTokenizer = new StringTokenizer(probeLine);
        String probeName = probeTokenizer.nextToken();

        TreeNode probeNode = makeProbeAliasNode(probeName,
                definition == null ? findDefinitionOf(probeName) : definition);

        groupNode = addOrFindProbeGroup(
                groupName == null ? extractProbeGroupName(probeName) : groupName,
                        groupNode, aliases);

        groupNode.add(probeNode);
        addAllVarNodesToProbeNode(probeTokenizer, probeNode);
        return groupNode;
    }

    /**
     * Finds the appropriate parent group node for a probe alias to group probes by name.
     * If it doesn't yet exist, create it and add it to the view's tree.
     * @param groupName The name of the probe group.
     * @param groupNode For optimization, pass an existing group node here, as it will be
     * used if the probe belongs in it. Otherwise, or if <code>null</code> is passed, a new one will be created.
     * @param category The parent tree node in which to put the group node.
     * @return The found or created group node that will be the parent of the probe's entry item in the view.
     */
    private TreeNode addOrFindProbeGroup(String groupName, TreeNode groupNode, TreeNode category) {
        // If the current probe belongs to a group other than
        // the most recent group. This should rarely be needed because the
        // probe list is sorted... mostly.
        if (groupNode == null || !groupNode.toString().equals(groupName)) {
            groupNode = category.getChildByName(groupName);
        }

        // Create a new group and add it
        if (groupNode == null) {
            groupNode = new TreeNode(groupName, true);
            category.add(groupNode);
        }
        return groupNode;
    }

    /**
     * @return the name of the group a probe belongs to, based on the probe's name.
     */
    private String extractProbeGroupName(String probeName) {
        Matcher m = PROBE_GROUP_PATTERN.matcher(probeName);
        return m.find() ? m.group() : probeName;
    }

    private TreeNode makeStaticProbeNode(String probeName) {
        return new TreeNode(new ProbeNodeData(probeName), probeName, true);
    }

    private TreeNode makeProbeAliasNode(String probeName, String definition) {
        return new TreeDefinitionNode(new ProbeNodeData(probeName), probeName, definition, true);
    }

    /**
     * Searches the tapset content dump for the path of the file which defines the provided probe alias.
     * @param probeName The alias of the probe to find the definition file of.
     * @return The path of the probe's definition file, or <code>null</code> if a definition
     * file can't be found (which is the case for static probes).
     */
    private String findDefinitionOf(String probeName) {
        String tapsetContents = SharedParser.getInstance().getTapsetContents();
        if (tapsetContents == null) {
            return null;
        }
        Matcher probeMatcher = Pattern.compile(MessageFormat.format(
                TAPSET_PROBE_REGEX, Pattern.quote(probeName))).matcher(tapsetContents);
        if (!probeMatcher.find()) {
            return null;
        }
        int fileLocIndex = tapsetContents.substring(0, probeMatcher.start())
                .lastIndexOf(SharedParser.TAG_FILE);
        try (Scanner scanner = new Scanner(tapsetContents.substring(fileLocIndex))) {
            return SharedParser.findFileNameInTag(scanner.nextLine());
        }
    }

    /**
     * Extracts the local variables from a (partially examined) probe alias token, and
     * adds them as child tree entries of their parent probe.
     */
    private void addAllVarNodesToProbeNode(StringTokenizer varTokenizer, TreeNode probeNode) {
        StringBuilder prev = new StringBuilder(""); //$NON-NLS-1$
        // the remaining tokens are variable names and variable types name:type.
        while (varTokenizer.hasMoreTokens()) {
            String token = varTokenizer.nextToken();

            // Because some variable types contain spaces (var2:struct task_struct)
            // the only way to know if we have the entire string representing a
            // variable is if we reach the next token containing a ':' or we reach
            // the end of the stream.
            if (token.contains(":") && prev.length() > 0) { //$NON-NLS-1$
                prev.setLength(prev.length() - 1); // Remove the trailing space.
                addVarNodeToProbeNode(prev.toString(), probeNode);
                prev.setLength(0);
            }
            prev.append(token + " "); //$NON-NLS-1$
        }

        // Add the last token if there is one
        if (prev.length() > 0) {
            prev.setLength(prev.length() - 1); // Remove the trailing space.
            addVarNodeToProbeNode(prev.toString(), probeNode);
        }
        probeNode.sortLevel();
    }

    private void addVarNodeToProbeNode(String info, TreeNode probeNode) {
        probeNode.add(new TreeNode(new ProbevarNodeData(info), info, false));
    }

    @Override
    protected int delTapsets(String[] tapsets, IProgressMonitor monitor) {
        TreeNode aliases = tree.getChildByName(Messages.ProbeParser_aliasProbes);

        // Search through alias groups for probes whose definition files
        // come from removed directories, and remove them from the group.
        for (int i = 0; i < tapsets.length; i++) {
            for (int g = 0, gn = aliases.getChildCount(); g < gn; g++) {
                if (monitor.isCanceled()) {
                    return IStatus.CANCEL;
                }
                TreeNode group = aliases.getChildAt(g);
                for (int p = 0, pn = group.getChildCount(); p < pn; p++) {
                    String definition = ((TreeDefinitionNode) group.getChildAt(p)).getDefinition();
                    if (definition != null && definition.startsWith(tapsets[i])) {
                        group.remove(p--);
                        pn--;
                    }
                }
                // If removing the only probe left in a probe group, remove the group.
                if (group.getChildCount() == 0) {
                    aliases.remove(g--);
                    gn--;
                }
            }
        }
        return IStatus.OK;
    }

    @Override
    protected int addTapsets(String tapsetContents, String[] additions, IProgressMonitor monitor) {
        boolean canceled = false;
        TreeNode aliases = tree.getChildByName(Messages.ProbeParser_aliasProbes);
        Map<String, ArrayList<String>> fileToItemMap = new HashMap<>();

        // Search tapset contents for all files provided by each added directory.
        for (int i = 0; i < additions.length; i++) {
            int firstTagIndex = 0;
            while (true) {
                // Get the contents of each file provided by the directory additions[i].
                firstTagIndex = tapsetContents.indexOf(
                        SharedParser.makeFileTag(additions[i]), firstTagIndex);
                if (firstTagIndex == -1) {
                    break;
                }
                int nextTagIndex = tapsetContents.indexOf(SharedParser.TAG_FILE, firstTagIndex + 1);
                String fileContents = nextTagIndex != -1
                        ? tapsetContents.substring(firstTagIndex, nextTagIndex)
                                : tapsetContents.substring(firstTagIndex);

                String filename;
                try (Scanner st = new Scanner(fileContents)) {
                    filename = SharedParser.findFileNameInTag(st.nextLine());
                }

                // Search file contents for the probes the file provides.
                ArrayList<String> newItems = new ArrayList<>();
                Matcher matcher = Pattern.compile(MessageFormat.format(
                        TAPSET_PROBE_REGEX, "(\\S+)")) //$NON-NLS-1$
                        .matcher(fileContents);
                while (matcher.find()) {
                    newItems.add(matcher.group(1));
                }
                if (!newItems.isEmpty()) {
                    fileToItemMap.put(filename, newItems);
                }
                // Remove the contents of the file that was just examined from the total contents.
                tapsetContents = tapsetContents.substring(0, firstTagIndex).concat(
                        tapsetContents.substring(firstTagIndex + fileContents.length()));
            }
        }
        // Run stap on each discovered probe to obtain their variable information.
        for (String file : fileToItemMap.keySet()) {
            for (String newitem : fileToItemMap.get(file)) {
                if (canceled || monitor.isCanceled()) {
                    canceled = true;
                    break;
                }
                addSingleProbeAlias(runStap(new String[]{"-L"}, newitem, false), //$NON-NLS-1$
                        aliases, null, null, file);
            }
        }

        aliases.sortTree();
        return !canceled ? IStatus.OK : IStatus.CANCEL;
    }

}
