/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information to populate StapNodes with
 */
public class StapData {
    public static final int NOT_PART_OF_COLLAPSED_NODE = -10;
    public boolean isCollapsed, marked;
    public boolean onlyChildWithThisName; //Should show up as collapsed and uncollapsed
    public int id;
    public int timesCalled, parent, style;
    public int levelOfRecursion;
    public int collapsedParent, uncollapsedPiece, partOfCollapsedNode;
    private long time;    //execution time of this node
    public String markedMessage;    //alt text for this node
    public String name;        //text to be displayed
    public List<Integer> children, collapsedChildren;
    private StapGraph graph;        //Store a reference to the parent graph


    /**
     * Initialize StapData object. This object is not intended to be called by users.
     *
     * @param graphModel StapGraph containing the StapNode that matches this StapData
     * @param style
     * @param txt Text to be displayed when rendering the StapNode
     * @param time Time taken for this particular node to execute
     * @param called Number of times this particular node was called
     * @param currentID The unique identifier for this node
     * @param caller The parent of this node
     * @param isMarked
     */
    public StapData(StapGraph graphModel, int style, String txt,
            long time, int called, int currentID, int parent, boolean isMarked) {
        this.time = time;
        this.style = style;
        this.timesCalled = called;
        children = new ArrayList<>();
        collapsedChildren = new ArrayList<>();
        this.id = currentID;
        this.name = txt;
        this.graph = graphModel;
        this.isCollapsed = false;
        this.onlyChildWithThisName = false;
        this.partOfCollapsedNode= NOT_PART_OF_COLLAPSED_NODE;
        this.collapsedParent = -1;
        this.parent = parent;
        this.levelOfRecursion = 0;
        this.marked = isMarked;
        this.uncollapsedPiece = -1;


        //Add this data to the caller's list of IDs
        if (this.parent != -1 && graphModel.getNodeData(this.parent) != null) {
            graphModel.getNodeData(this.parent).addCallee(this.id);
            this.levelOfRecursion = graphModel.getNodeData(this.parent).levelOfRecursion + 1;
        }

        //---------------Recursion management
        //Insert new level if necessary
        if (graphModel.levels.get(levelOfRecursion) == null)
            graphModel.levels.put(levelOfRecursion, new ArrayList<>());
        graphModel.levels.get(levelOfRecursion).add(this.id);

        //Keep track of the lowest level of recursion
        if (levelOfRecursion > graphModel.getLowestLevelOfNodesAdded())
            graphModel.setLowestLevelOfNodesAdded(levelOfRecursion);
    }


    /**
     * Add the given id to the list of children, at the end.
     *
     * @param id
     * @return
     */
    private int addCallee(int id) {
        children.add(id);
        return id;
    }

    /**
     * Creates a node in the given graphModel using this stapData
     * @param graphModel
     * @return the generated stapNode
     */

    public StapNode makeNode(StapGraph graphModel) {
        return new StapNode(graphModel, style, this);
    }

   /**
    * Indicate that this StapData is part of a collapsed node (will not be drawn in
    * uncollapsed mode)
    * @param partOfCollapsedNode
    */
    public void setPartOfCollapsedNode(int partOfCollapsedNode) {
        this.partOfCollapsedNode = partOfCollapsedNode;
    }


    /**
     * Indicate that this StapData was marked by the user
     */
    public void setMarked() {
        marked = true;
    }


    /**
     * Check if this StapData is marked -- returns the result of
     * marked || markedMessage.length() > 0 (in case marked was not set)
     * @return
     */
    public boolean isMarked() {
        return marked || (markedMessage != null && markedMessage.length() > 0);
    }


    public boolean isOnlyChildWithThisName() {
        return onlyChildWithThisName;
    }

    /**
     * If the node has not yet terminated (i.e. the time is > 1200000000000000000l) this
     * function will return graph.getEndTime() - time. In other words, getTime will assume
     * that only the start time has been recorded if time is abnormally large, and will compensate
     * by assuming that the node 'terminates' at the current endTime.
     *
     * @return long time
     */
    public long getTime() {
        if (time > 1200000000000000000l) {
            return graph.getEndTime() - time;
        }
        return time;
    }


    /**
     * Sets the time
     *
     * @param time
     */
    public void setTime(long time) {
        this.time = time;
    }

/**
 * Sets the message for this data object to the given string,
 * overwriting the current markedMessage. Sets marked to true.
 *
 * Returns this.
 * @param message
 * @return this
 */
    public StapData setMessage(String message) {
        if (message == null || message.length() < 1) {
            return this;
        }
        this.markedMessage = message;
        this.marked = true;
        return this;
    }


    /**
     * Inserts the message after the current message. No spaces or newlines are appended.
     * @param message
     * @return
     */
    public StapData insertMessage(String message) {
        if (message == null || message.length() < 1) {
            return this;
        }
        String tmp = message;
        if (this.markedMessage != null && this.markedMessage.length() > 0) {
            tmp = this.markedMessage + tmp;
        }
        this.markedMessage = tmp;
        this.marked = true;
        return this;
    }

    /**
     * Return true if <code>partOfCollapseNode!= StapData.NOT_PART_OF_COLLAPSED_NODE</code
     * @return
     */
    public boolean isPartOfCollapsedNode() {
        return (partOfCollapsedNode != NOT_PART_OF_COLLAPSED_NODE);
    }


    /**
     * Compare to StapData.NOT_PART_OF_COLLAPSED_NODE to verify, or check
     * isPartOfCollapseNode first. May return a negative number if invalid.
     *
     * @return The collapsed node this node is a part of (if any)
     */
    public int getPartOfCollapsedNode() {
        return partOfCollapsedNode;
    }


}
