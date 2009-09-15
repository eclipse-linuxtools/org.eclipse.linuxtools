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
package org.eclipse.linuxtools.systemtap.local.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapUIErrorMessages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
/**
 * Helper class that finds and opens files. Finds based on function names,
 * opens based on path and in the current default editor. 
 *
 */
public class FileFinderOpener {
	
	private static HashMap<String, Integer> offset = new HashMap<String, Integer>();
	private static HashMap<String, Integer> length = new HashMap<String, Integer>();

	
	/**
	 * @param project : C Project Type
	 * @param functionName : name of a function 
	 * @return an ArrayList of String paths (relative to current workspace) of
	 * files with specified function name
	 */
	private static ArrayList<String> findFunctionsInProject(ICProject project, 
			String functionName)  {
		  ArrayList<String> files = new ArrayList<String>() ;

		  IIndexManager manager = CCorePlugin.getIndexManager();
		  IIndex index = null;
		    try {
				index = manager.getIndex(project);
				index.acquireReadLock();
				IBinding[] bindings = index.findBindings(functionName.toCharArray(), IndexFilter.ALL, null);
				for (IBinding bind : bindings) {
					if (bind instanceof IFunction) {
						IFunction ifunction = (IFunction) bind;
						IIndexName[] names = index.findNames(ifunction,
								IIndex.FIND_DEFINITIONS);
						for (IIndexName iname : names) {
							IIndexFile file = iname.getFile();
							if (file != null) {
								IIndexFileLocation filelocation = file.getLocation();
								String loc = filelocation.getURI().getPath();
								files.add(loc);
								offset.put(loc, iname.getNodeOffset());
								length.put(loc, iname.getNodeLength());
							}
						}
					}
				}
				
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		   index.releaseReadLock();
		   return files;
	}
	
	
	public static void findAndOpen(ICProject project, String functionName) {
		offset.clear();
		length.clear();

		ArrayList<String> files = findFunctionsInProject(project, functionName);
		
		if (files == null || files.size() < 1)
			return;
		
		if (files.size() == 1) {
			open(files.get(0), offset.get(files.get(0)), length.get(files.get(0)));
		} else {
			ElementListSelectionDialog d = new ElementListSelectionDialog(
					new Shell(), new LabelProvider());
			d.setTitle("Multiple files found"); 
			d.setMessage("Multiple files found which define a function '" + functionName + "'. " +  
					"Please select a file or files to open"); 
			d.setElements(files.toArray());
			d.open();
			for (Object o : d.getResult()) {
				if (o instanceof String) {
					String s = (String) o;
					open(s, offset.get(s), length.get(s));
				}
			}
		}	
		
		return;
	}
	
	
	public static void open(String path, int offset, int length) {
		if (path == null)
			return;
		File fileToOpen = new File(path);
		 
		if (fileToOpen.exists() && fileToOpen.isFile()) {
		    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 
		    try {
		        IEditorPart ed = IDE.openEditorOnFileStore( page, fileStore );
		        if (ed instanceof ITextEditor && offset > 0) {
		        	ITextEditor text = (ITextEditor) ed;
		        	text.selectAndReveal(offset, length);
//		        	IDocument doc = text.getDocumentProvider().getDocument(text.getEditorInput());
//		        	int line = doc.getLineOfOffset(offset);
//		        	while (line < doc.getNumberOfLines()) {
//		        		String contents =doc.get(doc.getLineOffset(line), doc.getLineLength(line)); 
//		        		if (contents.contains(targetFunction)) {
//		        	
//		        		}
//
//		        		line++;
//		        	}
		        }
		    } catch ( PartInitException e ) {
//			} catch (BadLocationException e) {
			}
		} else {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages("File not found", 
					"File not found", "Could not find file to open:\n\n" + path);  
			mess.schedule();
		}
	}
	
}
