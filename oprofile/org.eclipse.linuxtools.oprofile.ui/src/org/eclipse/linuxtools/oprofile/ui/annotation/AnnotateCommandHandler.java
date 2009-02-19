/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.annotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSample;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSymbol;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class AnnotateCommandHandler implements IHandler {
	
	/**
	 * Original class Copyright (c) 2006 Mountainminds GmbH & Co. KG. 
	 * Adapted by Red Hat Inc, Copyright (c) 2009. 
	 * Tracks the workbench editors and to attach annotations. 
	 * 
	 * @author  Marc R. Hoffmann
	 * @author  Adapted by: Kent Sebastian
	 */
	class EditorTracker {
	  private final IWorkbench workbench;
	  
	  private IWindowListener windowListener = new IWindowListener() {
	    public void windowOpened(IWorkbenchWindow window) {
	      window.getPartService().addPartListener(partListener);
	    }
	    public void windowClosed(IWorkbenchWindow window) {
	      window.getPartService().removePartListener(partListener);
	    }
	    public void windowActivated(IWorkbenchWindow window) { }
	    public void windowDeactivated(IWorkbenchWindow window) { }
	  };
	  
	  private IPartListener2 partListener = new IPartListener2() {
	    public void partOpened(IWorkbenchPartReference partref) { 
	      annotateDispatch(partref);
	    }
	    public void partActivated(IWorkbenchPartReference partref) { }
	    public void partBroughtToTop(IWorkbenchPartReference partref) { }
	    public void partVisible(IWorkbenchPartReference partref) { }
	    public void partInputChanged(IWorkbenchPartReference partref) { }
	    public void partClosed(IWorkbenchPartReference partref) { }
	    public void partDeactivated(IWorkbenchPartReference partref) { }
	    public void partHidden(IWorkbenchPartReference partref) { }
	  };
	  
	  public EditorTracker() {
	    this.workbench = PlatformUI.getWorkbench();
	    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
	    for (int i = 0; i < windows.length; i++) {
	      windows[i].getPartService().addPartListener(partListener);
	    }
	    workbench.addWindowListener(windowListener);
	  }
	  
	  public void dispose() {
	    workbench.removeWindowListener(windowListener);
	    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
	    for (int i = 0; i < windows.length; i++) {
	      windows[i].getPartService().removePartListener(partListener);
	    }
	  }
	  	  
	  private void annotateDispatch(IWorkbenchPartReference partref) {
	    IWorkbenchPart part = partref.getPart(false);
	    if (part instanceof ITextEditor) {
	    	annotateEditor((ITextEditor)part);
	    }
	  }
	}

	
	private IUiModelElement currentSession = null;
	private EditorTracker editorTracker = null;
	private ArrayList<Annotation> annotations = null;
	private ArrayList<UiModelSymbol> sessionSymbols = null;
	private HashSet<String> sessionSymbolFilePaths = null;

	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		boolean toggleEnabled = true;//((ToolItem)((Event)exEvent.getTrigger()).widget).getSelection();
		
		if (toggleEnabled) {
			if (!UiModelRoot.getDefault().hasChildren()) {
				//no sessions to annotate
				OprofileCorePlugin.showErrorDialog("ui.annotate.no.sessions", null); //$NON-NLS-1$
			} else {
				//choose the session
				IUiModelElement[] events = UiModelRoot.getDefault().getChildren();
				if (events.length == 1 && events[0].getChildren().length == 1) {
					currentSession = events[0].getChildren()[0];
				} else {
					currentSession = openSessionSelectionDialog();
				}
				
				//create the list of symbols from the session
				populateSamples();
				
				//annotate current editors
				annotations = new ArrayList<Annotation>();
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			    for (int i = 0; i < windows.length; i++) {
					IWorkbenchPage[] pages = windows[i].getPages();
					for (int j = 0; j < pages.length; j++) {
					    IEditorReference[] editors = pages[j].getEditorReferences();
					    for (int k = 0; k < editors.length; k++) {
					        IWorkbenchPart editor = editors[k].getPart(false);
					        if (editor instanceof ITextEditor) {
								annotateEditor((ITextEditor) editor);
					        }
					    }
					}      
			    }
			    
			    //create the editor tracker to annotate editors as they open
				editorTracker = new EditorTracker();
			}
		} else {
			//remove annotations
			if (editorTracker != null) {
				editorTracker.dispose();
				editorTracker = null;
			}
			
			removeAllAnnotations();
			if (sessionSymbols != null) {
				sessionSymbols.clear();
			}
			if (sessionSymbolFilePaths != null) {
				sessionSymbolFilePaths.clear();
			}
			currentSession = null;
		}
		

		return null;	//required
	}

	private IUiModelElement openSessionSelectionDialog() {
		// FIXME TODO do this later
		
		return UiModelRoot.getDefault().getChildren()[0].getChildren()[0];
	}
	
	private void populateSamples() {
		if (currentSession != null) {
			sessionSymbols = new ArrayList<UiModelSymbol>();
			sessionSymbolFilePaths = new HashSet<String>();
			
			IUiModelElement[] images = currentSession.getChildren();
			if (images != null) {
				IUiModelElement image = images[0]; //image must be index 0, dependent index 1 if exists
				IUiModelElement[] symbols = image.getChildren();
				
				if (symbols != null && symbols.length > 0) {
					for (IUiModelElement s : symbols) {
						UiModelSymbol symbol = (UiModelSymbol)s;
						sessionSymbols.add(symbol);
						
						File symbolFileName = new File(symbol.getFileName());
						try {
							sessionSymbolFilePaths.add(symbolFileName.getCanonicalPath());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void annotateEditor(ITextEditor editor) {
		IResource editorResource = ((IResource)editor.getEditorInput().getAdapter(IResource.class));
		String editorFilePath = null;

		try {
			editorFilePath = editorResource.getLocation().toFile().getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (currentSession != null && fileHasAnnotations(editorFilePath)) {
	    	IAnnotationModel model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
	    	IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());

			if (sessionSymbols != null && sessionSymbols.size() > 0) {
				Iterator<UiModelSymbol> it = sessionSymbols.iterator();
				
				while (it.hasNext()) {
					UiModelSymbol symbol = it.next();
					if (symbol.hasChildren()) {
						try {
							//hack to ensure proper path comparison
							File symbolFileName = new File(symbol.getFileName());
							if (symbolFileName.getCanonicalPath().equals(editorFilePath)) {
								IUiModelElement[] samples = symbol.getChildren();
								
								for (IUiModelElement sample : samples) {
									UiModelSample spl = (UiModelSample)sample;
									Annotation a = new Annotation(true);
									a.setType(getAnnotationType(spl.getCountPercentage()));
									a.setText(OprofileUiPlugin
											.getPercentageString(spl.getCountPercentage())
											+ " " + OprofileUiMessages.getString("annotation.text.from") 		//$NON-NLS-1$ //$NON-NLS-2$
											+ symbol.getParent().getParent().getParent().toString()
											+ " " + OprofileUiMessages.getString("annotation.text.in") 		//$NON-NLS-1$ //$NON-NLS-2$
											+ symbol.getParent().getParent().toString());
									
									IRegion region = null;
									
									try {
										region = doc.getLineInformation(spl.getLine() - 1);
									} catch (BadLocationException e) {
										e.printStackTrace();
										continue;
									}
									
									annotations.add(a);
									model.addAnnotation(a, new Position(region.getOffset(), region.getLength()));
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private String getAnnotationType(double countPercentage) {
		String type = null;
		
		if (countPercentage < OprofileUiPlugin.MINIMUM_SAMPLE_PERCENTAGE) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_MIN_PERCENTAGE;
		} else if (countPercentage < 0.05) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_05;
		} else if (countPercentage < 0.1) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_10;
		} else if (countPercentage < 0.2) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_20;
		} else if (countPercentage < 0.3) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_30;
		} else if (countPercentage < 0.4) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_40;
		} else if (countPercentage < 0.5) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_LT_50;
		} else if (countPercentage >= 0.5) {
			type = OprofileUiPlugin.ANNOTATION_TYPE_GT_50;
		}

		return type;
	}

	private boolean fileHasAnnotations(String editorFilePath) {
		if (sessionSymbolFilePaths != null && sessionSymbolFilePaths.size() > 0) {
			return sessionSymbolFilePaths.contains(editorFilePath);
		}
		
		return false;
	}

	private void removeAllAnnotations() {
		if (annotations != null && annotations.size() > 0) {
			//cache attached annotations and the associated model?
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		    for (int i = 0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
				    IEditorReference[] editors = pages[j].getEditorReferences();
				    for (int k = 0; k < editors.length; k++) {
				        IWorkbenchPart editor = editors[k].getPart(false);
				        if (editor instanceof ITextEditor) {
							IAnnotationModel model = ((ITextEditor) editor).getDocumentProvider().getAnnotationModel(((ITextEditor) editor).getEditorInput());
							for (Iterator<Annotation> it = annotations.iterator(); it.hasNext(); ) {
								model.removeAnnotation(it.next());
							}
				        }
				    }
				}      
		    }
		    
		    annotations.clear();
		}
	}
	
	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}
}
