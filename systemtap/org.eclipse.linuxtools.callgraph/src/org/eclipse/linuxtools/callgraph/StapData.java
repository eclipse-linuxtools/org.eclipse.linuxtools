/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains information to populate StapNodes with 
 *
 */
public class StapData {
	public boolean expandable;
	public static final int NOT_PART_OF_COLLAPSED_NODE = -10;
	public boolean noCaller;
	public boolean hasCollapsedChildren;
	public boolean isCollapsed;
	public boolean onlyChildWithThisName;
    public int id;		//id of the StapNode
    public int timesCalled, parent, style;
    public int levelOfRecursion;
    public int collapsedParent;
    public int uncollapsedPiece;	//An uncollapsed piece of this node
    private long time;	//execution time of this node
    public String markedMessage;	//alt text for this node
    public String name;		//text to be displayed
    public List<Integer> children;
    public List<Integer> collapsedChildren;
    
    private int partOfCollapsedNode;
    //TODO: every StapData holds a copy of its StapGraph
    //It is not used that often and one StapGraph per StapData is a lot 
    private StapGraph graph;
    private boolean marked;

    public boolean isPartOfCollapsedNode() {
		return (partOfCollapsedNode != NOT_PART_OF_COLLAPSED_NODE);
	}


    /**
     * Compare to StapData.NOT_PART_OF_COLLAPSED_NODE to verify, or check
     * isPartOfCollapseNode first.
     * 
     * @return The collapsed node this node is a part of (if any)
     */
    public int getPartOfCollapsedNode() {
    	return partOfCollapsedNode;
    }
	
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
        this.expandable = false;
        children = new ArrayList<Integer>();
        collapsedChildren = new ArrayList<Integer>();
        this.id = currentID;
        this.name = txt;
        this.graph = graphModel;
        this.hasCollapsedChildren = false;
        this.isCollapsed = false;
        this.onlyChildWithThisName = false;
        this.partOfCollapsedNode= NOT_PART_OF_COLLAPSED_NODE;
        this.collapsedParent = -1;
        this.parent = parent;
        this.levelOfRecursion = 0;
        this.marked = isMarked;
        this.uncollapsedPiece = -1;
        
        
    	//Add this data to the caller's list of IDs
		if (this.parent != -1) {
			if (graphModel.getNodeData(this.parent) != null) {
				graphModel.getNodeData(this.parent).addCallee(this.id);
				this.levelOfRecursion = graphModel.getNodeData(this.parent).levelOfRecursion + 1;
			}
		}
        
		//---------------Recursion management
        //Insert new level if necessary
		if (graphModel.levels.get(levelOfRecursion) == null)
			graphModel.levels.put(levelOfRecursion, new ArrayList<Integer>());
        graphModel.levels.get(levelOfRecursion).add(this.id);
        
		//Keep track of the lowest level of recursion
		if (levelOfRecursion > graphModel.getLowestLevelOfNodesAdded())
			graphModel.setLowestLevelOfNodesAdded(levelOfRecursion);
        

        this.noCaller = (parent == -1) ? true : false;
    }

    
    /**
     * Add the given id to the list of children, at the end.
     * 
     * @param id
     * @return
     */
    public int addCallee(int id) {
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
     * Sort the list of callees according to time
     */
    public void sortByTime(){
    	TreeMap<Long,ArrayList<StapData>> tempList = new TreeMap<Long,ArrayList<StapData>>();
    	//INDEX ALL THE STAPDATA INTO AN ARRAY AT THE CALCULATED INDEX
    	//SCATTERED INDICES : 0,1,...,5,..,10
    	for (int val : collapsedChildren){
    		if (tempList.get(graph.getNodeData(val).time) == null){
    			tempList.put(graph.getNodeData(val).time, new ArrayList<StapData>());
    		}
    		
    		tempList.get(graph.getNodeData(val).time).add(graph.getNodeData(val));
    	}

    	collapsedChildren.clear();
    	int count = 0;
		// ANOTHER PASS THROUGH TO INDEX CONTINUOUSLY 0,1,2,..
		for (long i : tempList.descendingKeySet()) {
			for (StapData j : tempList.get(i)){
				collapsedChildren.add(count, j.id);
			}
			count++;
		}
		
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
	 * Check if this StapData is marked
	 * @return
	 */
	public boolean isMarked() {
		return marked;
	}

	
	public boolean isOnlyChildWithThisName() {
		return onlyChildWithThisName;
	}


	public void setOnlyChildWithThisName(boolean onlyChildWithThisName) {
		this.onlyChildWithThisName = onlyChildWithThisName;
	}


	public long getTime() {
		if (time > 1200000000000000000l) {
			return graph.getEndTime() - time;
		}
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}


	public StapData setMessage(String message) {
		this.markedMessage = message;
		return this;
	}
	
	
	public StapData insertMessage(String message) {
		String tmp = message;
		if (this.markedMessage != null && this.markedMessage.length() > 0)
			tmp = this.markedMessage + tmp;
		this.markedMessage = tmp;
		return this;
	}

}
