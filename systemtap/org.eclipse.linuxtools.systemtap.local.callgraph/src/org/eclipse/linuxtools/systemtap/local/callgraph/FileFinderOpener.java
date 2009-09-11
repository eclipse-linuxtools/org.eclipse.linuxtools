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
package org.eclipse.linuxtools.systemtap.local.callgraph;

import java.io.File;
import java.util.ArrayList;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;
/**
 * Helper class that finds and opens files. Finds based on function names,
 * opens based on path and in the current default editor. 
 *
 */
public class FileFinderOpener {
	
	
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
								files.add(filelocation.getURI().getPath());
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
		ArrayList<String> files = findFunctionsInProject(project, functionName);
		
		if (files == null || files.size() < 1)
			return;
		
		if (files.size() == 1) {
			open(files.get(0));
		} else {
			ElementListSelectionDialog d = new ElementListSelectionDialog(
					new Shell(), new LabelProvider());
			d.setTitle(Messages.getString("FileFinderOpener.0")); //$NON-NLS-1$
			d.setMessage(Messages.getString("FileFinderOpener.1") + functionName + "'. " + //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("FileFinderOpener.3")); //$NON-NLS-1$
			d.setElements(files.toArray());
			d.open();
			for (Object o : d.getResult()) {
				if (o instanceof String)
					open((String)o);
			}
		}	
		
		return;
	}
	
	
	public static void open(String path) {
		if (path == null)
			return;
		File fileToOpen = new File(path);
		 
		if (fileToOpen.exists() && fileToOpen.isFile()) {
		    IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 
		    try {
		        IDE.openEditorOnFileStore( page, fileStore );
		    } catch ( PartInitException e ) {
		    }
		} else {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("FileFinderOpener.4"), //$NON-NLS-1$
					Messages.getString("FileFinderOpener.5"), Messages.getString("FileFinderOpener.6") + path); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
		}
	}
	
}
