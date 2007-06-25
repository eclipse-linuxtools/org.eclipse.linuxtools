/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.changelog.core.actions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.operations.DiffOperation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author klee
 *
 */
public class StringDiffOperation extends DiffOperation {

	
	public static final String EMPTY_DIFF = "empty_diff";
	
	   final ByteArrayOutputStream os = new ByteArrayOutputStream();
	   
	   private String diffResult = "";
	   private boolean diffDone = false;
	   
	public StringDiffOperation(IWorkbenchPart part, ResourceMapping[] mappings, LocalOption[] options, boolean isMultiPatch, boolean includeFullPathInformation, IPath patchRoot) {
		super(part, mappings, options, isMultiPatch, includeFullPathInformation,
				patchRoot,null);
		
	}
	
	
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
	    super.execute(monitor);
	    
	    if (os.size() == 0 ||
		    	(!patchHasContents && !patchHasNewFiles)) {
	            reportEmptyDiff();
	        } else {
	            
	        	
	        	diffResult = os.toString();
	        }
	
		diffDone = true;
		
	}
	
	
	public String getResult() {
		return diffDone ? diffResult : null;
	}

	

	protected PrintStream openStream() throws CVSException {
		return new PrintStream(os);
	}

}
