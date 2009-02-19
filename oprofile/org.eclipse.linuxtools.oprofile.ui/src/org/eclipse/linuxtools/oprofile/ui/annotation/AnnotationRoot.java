///*******************************************************************************
// * Copyright (c) 2009 Red Hat, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
// *******************************************************************************/ 
//package org.eclipse.linuxtools.oprofile.ui.annotation;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//
//import org.eclipse.core.resources.IResource;
//import org.eclipse.jface.text.source.Annotation;
//import org.eclipse.jface.text.source.IAnnotationModel;
//import org.eclipse.ui.IEditorReference;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.texteditor.ITextEditor;
//
//public class AnnotationRoot {
//
//	private static AnnotationRoot _annotationRoot = new AnnotationRoot();
//	private HashMap<String, HashSet<Annotation>> annotations;
//	
//	private AnnotationRoot() {
//		annotations = new HashMap<String, HashSet<Annotation>>();
//		_annotationRoot = this;
//	}
//	
//	public static AnnotationRoot getDefault() {
//		return _annotationRoot;
//	}
//	
//	
//	
//	
//	public void removeAllAnnotations() {
//		if (annotations != null && annotations.size() > 0) {
//			//cache attached annotations and the associated model?
//			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
//		    for (int i = 0; i < windows.length; i++) {
//				IWorkbenchPage[] pages = windows[i].getPages();
//				for (int j = 0; j < pages.length; j++) {
//				    IEditorReference[] editors = pages[j].getEditorReferences();
//				    for (int k = 0; k < editors.length; k++) {
//				        IWorkbenchPart editor = editors[k].getPart(false);
//				        if (editor instanceof ITextEditor) {
//							String editorFilePath = ((IResource)((ITextEditor)editor).getEditorInput().getAdapter(IResource.class)).getLocation().toString();
//							
//							if (annotations.containsKey(getUniqPath(editorFilePath))) {
//								IAnnotationModel model = ((ITextEditor) editor).getDocumentProvider().getAnnotationModel(((ITextEditor) editor).getEditorInput());
//								
//								HashSet<Annotation> toRemove = annotations.remove(getUniqPath(editorFilePath));
//								
//								for (Iterator<Annotation> it = toRemove.iterator(); it.hasNext(); ) {
//									model.removeAnnotation(it.next());
//								}
//							}
//				        }
//				    }
//				}      
//		    }
//		    
//		    annotations.clear();
//		}
//	}
//	
//	private String getUniqPath(String s) {
//		File f = new File(s);
//		String path = "";  //$NON-NLS-1$
//
//		try {
//			path = f.getCanonicalPath();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		return path;
//	}
//}
