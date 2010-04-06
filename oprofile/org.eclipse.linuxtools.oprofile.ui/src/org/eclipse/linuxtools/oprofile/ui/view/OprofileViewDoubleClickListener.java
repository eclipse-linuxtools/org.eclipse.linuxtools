/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelEvent;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelImage;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSample;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSymbol;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.ui.PartInitException;

/**
 * Listener for the oprofile view when a user double clicks on an element in the tree.
 * 
 * Different things occur based on the event:
 *   
 *   UiModelEvent 		- nothing (?)
 *   UiModelSession 	- save the session to a different name
 *   UiModelImage 		- nothing (?)
 *   UiModelSymbol		- nothing (?)
 *   UiModelSample		- go to line number in appropriate file
 */
public class OprofileViewDoubleClickListener implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		TreeViewer tv = (TreeViewer) event.getSource();
		TreeSelection tsl = (TreeSelection) tv.getSelection();
		IUiModelElement element = (IUiModelElement) tsl.getFirstElement();
		
		if (element instanceof UiModelEvent) {
//			UiModelEvent event = (UiModelEvent)element;

		} else if (element instanceof UiModelSession) {
			/* moved into an action menu */
		} else if (element instanceof UiModelImage) {
//			UiModelImage image = (UiModelImage)element;
	
		} else if (element instanceof UiModelSymbol) {
			UiModelSymbol symbol = (UiModelSymbol)element;
			String symbolString = symbol.getParent().getLabelText();
			String fileName = symbol.getFileName();
			String functionName = symbol.getFunctionName();
			HashMap<String,int[]> map;
			
			ICProject project;
			try {
				project = findCProjectWithFileName(symbolString);
				if (project == null){
					return;
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
				return;
			}
			
			if (fileName.length() > 0 && functionName.length() > 0){
				// try and go to the function in the file
				map = findFunctionsInProject(project, functionName, fileName);
				
				for (String loc : map.keySet()){
					try {
						ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
				
			}else if (functionName.length() > 0){
				// try to find the file name that has this function
				map = findFunctionsInProject(project, functionName, null);
				
				for (String loc : map.keySet()){
					try {
						ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}else if (fileName.length() > 0) {
				// can this ever happen ?
				// jump to 1st line in the file
				try {
					ProfileUIUtils.openEditorAndSelect(fileName, 1);
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		} else if (element instanceof UiModelSample) {
			//jump to line number in the appropriate file
			UiModelSample sample = (UiModelSample)element;
			int line = sample.getLine();
			
			//get file name from the parent sample 
			String fileName = ((UiModelSymbol)sample.getParent()).getFileName();
			
			try {
				ProfileUIUtils.openEditorAndSelect(fileName, line);
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ICProject findCProjectWithFileName(final String symbolString) throws CoreException{
		final ArrayList<ICProject> ret = new ArrayList<ICProject>();
		
		// visitor object to check for the matching path string
		ICElementVisitor vis = new ICElementVisitor() {
			public boolean visit(ICElement element) throws CoreException {
				if (element.getElementType() == ICElement.C_CCONTAINER
						|| element.getElementType() == ICElement.C_PROJECT){
					return true;
				}else if (symbolString.endsWith((element.getPath().toFile().getAbsolutePath()))){
					ret.add(element.getCProject());
				}
				return false;
		}};
		
			ICProject[] cProjects = CCorePlugin.getDefault().getCoreModel().getCModel().getCProjects();
			for (ICProject proj : cProjects){
					// visit every project
					proj.accept(vis);
			}
		
		// is it possible to find more than one matching project ?
		return ret.size() == 0 ? null : ret.get(0);
	}
	
	/**
	 * Get a mapping between a file name, and the data relevant to locating
	 * the corresponding function name for a given project.
	 * 
	 * @param project : C Project Type
	 * @param functionName : Name of a function 
	 * @param fileHint : The name of the file where we expect to find functionName.
	 * It is null if we do not want to use this option.
	 * @return a HashMap<String, int []> of String absolute paths of files and the
	 * function's corresponding node-offset and length.
	 */
	private static HashMap<String,int[]> findFunctionsInProject(ICProject project, String functionName, String fileHint)  {
		  HashMap<String,int[]> files = new HashMap<String,int[]>() ;
		  int numOfArgs = 0;
		  
		  // detect function with arguments and narrow search accordingly
		  if (functionName.matches(".*\\(.*\\)")){
			  int start = functionName.indexOf('(');
			  if (functionName.contains(",")){
				  int end = functionName.indexOf(')');
				  numOfArgs = functionName.substring(start, end).split(",").length;
			  }else{
				  numOfArgs = 1;
			  }
			  functionName = functionName.substring(0, start);
		  }

		  IIndexManager manager = CCorePlugin.getIndexManager();
		  IIndex index = null;
		    try {
				index = manager.getIndex(project);
				index.acquireReadLock();
				IBinding[] bindings = index.findBindings(functionName.toCharArray(), IndexFilter.ALL, null);
				for (IBinding bind : bindings) {
					if (bind instanceof IFunction && (((IFunction)bind).getParameters().length == numOfArgs)) {
						IFunction ifunction = (IFunction) bind;
						IIndexName[] names = index.findNames(ifunction, IIndex.FIND_DEFINITIONS);
						for (IIndexName iname : names) {
							IIndexFile file = iname.getFile();
							if (file != null) {
								String loc = file.getLocation().getURI().getPath();
								if (fileHint != null){
									if (loc.endsWith(fileHint)){
										files.put(loc, new int [] {iname.getNodeOffset(), iname.getNodeLength()});
									}
								}else{
									files.put(loc, new int [] {iname.getNodeOffset(), iname.getNodeLength()});
								}
							}
						}
					}
				}
				
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (DOMException e) {
				e.printStackTrace();
			}finally{
				index.releaseReadLock();
			}
		   return files;
	}
	
}
