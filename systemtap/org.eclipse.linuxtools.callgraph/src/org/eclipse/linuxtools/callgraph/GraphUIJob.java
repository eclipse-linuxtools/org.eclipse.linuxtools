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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer within the SystemTap View
 * 
 * @author chwang
 *
 */
public class GraphUIJob extends UIJob{
	private SystemTapParser parser;

	
	public GraphUIJob(String name, SystemTapParser parser) {
		super(name);
		//CREATE THE SHELL
		this.parser = parser;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		CallgraphView view = new CallgraphView();
		if (!view.setParser(parser))
			return Status.CANCEL_STATUS;
		view.initialize(this.getDisplay(), monitor);
	    
		return Status.OK_STATUS;
	}	
	
	/**
	 * For easier JUnit testing only. Allows public access to run method without scheduling an extra job.
	 *  
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return runInUIThread(m);
	}
	
	
}

