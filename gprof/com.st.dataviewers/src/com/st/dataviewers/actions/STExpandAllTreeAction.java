/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.dataviewers.actions;

import org.eclipse.jface.action.Action;

import com.st.dataviewers.abstractviewers.AbstractSTTreeViewer;
import com.st.dataviewers.abstractviewers.STDataViewersImages;
import com.st.dataviewers.abstractviewers.STDataViewersMessages;

/**
 * This action expands all the tree
 *
 */
public class STExpandAllTreeAction extends Action {

	private final AbstractSTTreeViewer stViewer;
	
	/**
	 * Constructor
	 * @param stViewer the stViewer to expand
	 */
	public STExpandAllTreeAction(AbstractSTTreeViewer stViewer) {
		super(STDataViewersMessages.expandAllAction_title,
				STDataViewersImages.getImageDescriptor(STDataViewersImages.IMG_EXPANDALL));
		this.stViewer = stViewer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		Object input = stViewer.getViewer().getInput();
		if (input != null) {
			stViewer.getViewer().expandAll();
		}
	}
}
