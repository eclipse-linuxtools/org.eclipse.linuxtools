/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.linuxtools.internal.profiling.ui.ProfileUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ProfileUIUtils {
	
	/**
	 * Opens the specified file in an editor (or selects an already open
	 * editor) and highlights the specified line.
	 * @param path - absolute path of file to open
	 * @param line - line number to select, 0 to not select a line
	 * @throws PartInitException - Failed to open editor
	 * @throws BadLocationException - Line number not valid in file
	 */
	public static void openEditorAndSelect(String path, int line) throws PartInitException, BadLocationException {
		Path p = new Path(path);

		if (p.toFile().exists()) {
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore file = EFS.getLocalFileSystem().getStore(p);

			IEditorPart editor = IDE.openEditorOnFileStore(activePage, file);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					int start = document.getLineOffset(line - 1); //zero-indexed
					textEditor.selectAndReveal(start, 0);
				}
			}
		}
	}
	
	/**
	 * @since 2.0
	 */
	public static void openEditorAndSelect(IFile file, int line) throws PartInitException, BadLocationException {
		if (file.exists()) {
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

			IEditorPart editor = IDE.openEditor(activePage, file);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					int start = document.getLineOffset(line - 1); //zero-indexed
					textEditor.selectAndReveal(start, 0);
				}
			}
		}
	}
	
	/**
	 * Opens the specified file in an editor (or selects an already open
	 * editor) and highlights the specified line.
	 * @param result - result of performing source lookup with a ISourceLocator
	 * @param line - line number to select, 0 to not select a line
	 * @throws PartInitException - Failed to open editor
	 * @throws BadLocationException - Line number not valid in file
	 * @see DebugUITools#lookupSource(Object, ISourceLocator)
	 */
	public static void openEditorAndSelect(ISourceLookupResult result, int line) throws PartInitException, BadLocationException {
		IEditorInput input = result.getEditorInput();
		String editorID = result.getEditorId();
		
		if (input == null || editorID == null) {
			// Consult the CDT DebugModelPresentation
			Object sourceElement = result.getSourceElement();
			if (sourceElement != null) {
				// Resolve IResource in case we get a LocalFileStorage object
				if (sourceElement instanceof LocalFileStorage) {
					IPath filePath = ((LocalFileStorage) sourceElement).getFullPath();
					URI fileURI = URIUtil.toURI(filePath);
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IFile[] files = root.findFilesForLocationURI(fileURI);
					if (files.length > 0) {
						// Take the first match
						sourceElement = files[0];
					}
				}
				
				IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(CDebugCorePlugin.getUniqueIdentifier());
				input = pres.getEditorInput(sourceElement);
				editorID = pres.getEditorId(input, sourceElement);
				pres.dispose();
			}
		}
		if (input != null && editorID != null) {
			// Open the editor
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();;

			IEditorPart editor = IDE.openEditor(activePage, input, editorID);
			// Select the line
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editor;

				if (line > 0) {
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());

					IRegion lineRegion = document.getLineInformation(line - 1); //zero-indexed
					textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
				}
			}
		}
	}
	
	/**
	 * Open a file in the Editor at the specified offset, highlighting the given length
	 * 
	 * @param path : Absolute path pointing to the file which will be opened.
	 * @param offset : Offset of the function to be highlighted.
	 * @param length : Length of the function to be highlighted.
	 * @throws PartInitException if the editor could not be initialized
	 */
	public static void openEditorAndSelect(String path, int offset, int length) throws PartInitException {
		Path p = new Path (path);

		if (p.toFile().exists()) {
			IWorkbenchPage activePage = ProfileUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(p);

			IEditorPart editor = IDE.openEditorOnFileStore(activePage, fileStore);
			if (editor instanceof ITextEditor) {
				ITextEditor text = (ITextEditor) editor;
				text.selectAndReveal(offset, length);
			}
		}
	}
	
	/**
	 * Find an ICProject that contains the specified absolute path.
	 * 
	 * @param absPath An absolute path (usually to some file/folder in a project)
	 * @return an ICProject corresponding to the project that contains the absolute path
	 * @throws CoreException
	 */
	public static ICProject findCProjectWithAbsolutePath(final String absPath) throws CoreException{
		final String workspaceLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		final ArrayList<ICProject> ret = new ArrayList<ICProject>();
		
		// visitor object to check for the matching path string
		ICElementVisitor vis = new ICElementVisitor() {
			public boolean visit(ICElement element) {
				if (element.getElementType() == ICElement.C_CCONTAINER
						|| element.getElementType() == ICElement.C_PROJECT){
					return true;
				}else if (absPath.equals(workspaceLoc+element.getPath().toFile().getAbsolutePath())){
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
	 * @param numArgs : The number of arguments this function is expected to have.
	 * A value of -1 will ignore the number of arguments when searching.
	 * @param fileHint : The name of the file where we expect to find functionName.
	 * It is null if we do not want to use this option.
	 * @return a HashMap<String, int []> of String absolute paths of files and the
	 * function's corresponding node-offset and length.
	 */
	public static HashMap<String,int[]> findFunctionsInProject(ICProject project, String functionName,
			int numArgs, String fileHint)  {
		  HashMap<String,int[]> files = new HashMap<String,int[]>() ;

		  IIndexManager manager = CCorePlugin.getIndexManager();
		  IIndex index = null;
		    try {
				index = manager.getIndex(project);
				index.acquireReadLock();
				IBinding[] bindings = index.findBindings(functionName.toCharArray(), IndexFilter.ALL, null);
				for (IBinding bind : bindings) {
					if (bind instanceof IFunction
							&& (numArgs == -1 || ((IFunction)bind).getParameters().length == numArgs)) {
						IFunction ifunction = (IFunction) bind;
						IIndexName[] names = index.findNames(ifunction, IIndex.FIND_DEFINITIONS);
						for (IIndexName iname : names) {
							IIndexFile file = iname.getFile();
							if (file != null) {
								String loc = file.getLocation().getURI().getPath();
								if (fileHint != null){
									if (loc.equals(new File(fileHint).getCanonicalPath())){
										//TODO: Consider changing data structure so that we can
										// store multiple same-named functions (different args)
										// from the same file.
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
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				index.releaseReadLock();
			}
		   return files;
	}
	
	/**
	 * Helper function for findFunctionsInProject
	 * @param needResult True if the function should relax constraints in order
	 * to return some value. False if a failure to find the function(s) is acceptable.
	 */
	public static HashMap<String,int[]> findFunctionsInProject(ICProject project, String functionName, 
			int numArgs, String fileHint, boolean needResult){
		HashMap<String, int []> map = findFunctionsInProject(project, functionName, numArgs, fileHint);
		if (needResult && map.size() == 0){
			map = findFunctionsInProject(project, functionName, -1, fileHint);
			if (map.size() == 0){
				return findFunctionsInProject(project, functionName, -1, null);
			}
		}
		return map;
	}
	
}
