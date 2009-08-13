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
package org.eclipse.linuxtools.systemtap.localgui.graphing;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains information to populate StapNodes with 
 *
 */
public class StapData {
	public boolean expandable;
    public int id;			  //id of the StapNode
    public long time;
    public int called;
    public int caller;
    public int style;
    public int levelOfRecursion;
    public String name;
    public boolean noCaller;
    public List<Integer> callees;
    public List<Integer> collapsedCallees;
    private StapGraph graph;
    public boolean hasCollapsedChildren;
    public boolean isCollapsed;
    public boolean onlyChildWithThisName;
    private boolean partOfCollapsedNode;
    public int collapsedCaller;
    private boolean marked;
    public String markedMessage;

    public boolean isPartOfCollapsedNode() {
		return partOfCollapsedNode;
	}



	
	/**
	 * Initialize StapData object
	 * 
	 * @param graphModel
	 * @param style
	 * @param txt
	 * @param time
	 * @param called
	 * @param currentID
	 * @param caller
	 * @param isMarked
	 * @param message
	 */
	public StapData(StapGraph graphModel, int style, String txt, 
    		long time, int called, int currentID, int caller, boolean isMarked, String message) {
        this.time = time;
        this.style = style;
        this.called = called;
        this.expandable = false;
        callees = new ArrayList<Integer>();
        collapsedCallees = new ArrayList<Integer>();
        this.id = currentID;
        this.name = txt;
        this.graph = graphModel;
        this.hasCollapsedChildren = false;
        this.isCollapsed = false;
        this.onlyChildWithThisName = false;
        this.partOfCollapsedNode= false;
        this.collapsedCaller = -1;
        this.caller = caller;
        this.levelOfRecursion = 0;
        this.marked = isMarked;
        this.markedMessage = message;
        
        
    	//Add this data to the caller's list of IDs
		if (this.caller != -1) {
			if (graphModel.getData(this.caller) != null) {
				graphModel.getData(this.caller).addCallee(this.id, this.time);
				this.levelOfRecursion = graphModel.getData(this.caller).levelOfRecursion + 1;
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
        

        this.noCaller = (caller == -1) ? true : false;
    }

    
    /**
     * Add the given id to my list of children/callees. Sort based on time
     * 
     * @param id
     * @return
     */
    public int addCallee(int id, long time) {
    	
    	//TODO: This is phenomenally inefficient. We should just add them all
    	//then call a sort once instead of doing some crazy n! insertion :P
    	
    	//Insert id based on its time
    	int size = callees.size();
    	
    	if (size ==0) {
    		callees.add(id);
    		return callees.size();
    	}
    	int position = search(time);
    	
    	if (position == -1) callees.add(id);
    	else callees.add(position, id);
   
        return callees.size();
    }
    

    /**
     * Returns the proper position in callees list for the node with the given time.
     * Afterwards an insert at the return value will put the node in the right spot. 
     * @param time
     * @return location in callees 
     */
    private int search(long time) {
    	if (time > graph.getData(callees.get(0)).time)
    		return 0;

    	for (int i = 1; i < callees.size(); i++) {
    		if (time < graph.getData(callees.get(i -1)).time && 
    				time > graph.getData(callees.get(i)).time)
    				return i;
    	}
    	
    	return -1;
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
    	for (int val : collapsedCallees){
    		if (tempList.get(graph.getData(val).time) == null){
    			tempList.put(graph.getData(val).time, new ArrayList<StapData>());
    		}
    		
    		tempList.get(graph.getData(val).time).add(graph.getData(val));
    	}

    	collapsedCallees.clear();
    	int count = 0;
		// ANOTHER PASS THROUGH TO INDEX CONTINUOUSLY 0,1,2,..
		for (long i : tempList.descendingKeySet()) {
			for (StapData j : tempList.get(i)){
				collapsedCallees.add(count, j.id);
			}
			count++;
		}
		
    }
    
   /**
    * Indicate that this StapData is part of a collapsed node (will not be drawn in
    * uncollapsed mode)
    * @param partOfCollapsedNode
    */
	public void setPartOfCollapsedNode(boolean partOfCollapsedNode) {
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
}
