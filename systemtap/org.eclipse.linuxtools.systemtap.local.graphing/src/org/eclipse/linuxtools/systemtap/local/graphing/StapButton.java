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

package org.eclipse.linuxtools.systemtap.local.graphing;


import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;

/**
 * Currently unused class for adding buttons
 * 
 * @author chwang
 *
 */
public class StapButton extends GraphNode{
	public static final int CYCLE_LEFT = 0;
	public static final int CYCLE_RIGHT = 1;
	public static final int INVALID = 0;
	public int actionID;
	public int targetID;
	public int data;
	public int buttonID;

	public StapButton(IContainer graphModel, int style, int buttonID, int actionID, int targetID) {
		super(graphModel, style);
//		String imageLocation = "";
		this.actionID = actionID;
		this.targetID = targetID;
		this.buttonID = buttonID;
		this.data = INVALID;
//		if (actionID == CYCLE_LEFT)
//			imageLocation = PluginConstants.PLUGIN_LOCATION+"/icons/func_obj.gif";
//		if (actionID == CYCLE_RIGHT)
//			imageLocation = PluginConstants.PLUGIN_LOCATION+"/icons/sample.gif";
//
//		Image img = new Image(Display.getCurrent(), imageLocation);
//		this.setImage(img);
		
		this.setText("B: " + targetID ); //$NON-NLS-1$
	}
	
	@Override
	public Object getData() {
		return (Integer) data;
	}
	
	public void setData(int data) {
		this.data = data;
	}
	
	public Class<?> getStapButtonClass() {
		return this.getClass();
	}

}
