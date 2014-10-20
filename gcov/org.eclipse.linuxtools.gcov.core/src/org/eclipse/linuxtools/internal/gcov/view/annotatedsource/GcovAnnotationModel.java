/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.linuxtools.binutils.link2source.STLink2SourceSupport;
import org.eclipse.linuxtools.internal.gcov.parser.CovManager;
import org.eclipse.linuxtools.internal.gcov.parser.Line;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Annotation model responsible for management of GcovAnnotation elements.
 */
public final class GcovAnnotationModel implements IAnnotationModel {

    private static final String THOROUGH_COVERAGE = "org.eclipse.linuxtools.gcov.ThoroughCoverageAnnotation"; //$NON-NLS-1$
    private static final String COVERAGE = "org.eclipse.linuxtools.gcov.CoverageAnnotation"; //$NON-NLS-1$
    private static final String NO_COVERAGE = "org.eclipse.linuxtools.gcov.NoCoverageAnnotation"; //$NON-NLS-1$

    /** Key for identifying our model from other Editor models. */
    private static final Object KEY = new Object();

    /** List of GcovAnnotation elements */
    private List<GcovAnnotation> annotations = new ArrayList<>();

    /** List of IAnnotationModelListener */
    private List<IAnnotationModelListener> annotationModelListeners = new ArrayList<>();

    private final ITextEditor editor;
    private final IDocument document;
    private int openConnections = 0;
    private boolean annotated = false;

    private IDocumentListener documentListener = new IDocumentListener() {
        @Override
        public void documentChanged(DocumentEvent event) {
            updateAnnotations(false);
        }

        @Override
        public void documentAboutToBeChanged(DocumentEvent event) {}
    };

    private GcovAnnotationModel(ITextEditor editor, IDocument document) {
        this.editor = editor;
        this.document = document;
        updateAnnotations(true);
    }

    /**
     * Attaches a coverage annotation model for the given editor if the editor
     * can be annotated. Does nothing if the model is already attached.
     *
     * @param editor Editor to which an annotation model should be attached
     */
    public static void attach(ITextEditor editor) {
        IDocumentProvider provider = editor.getDocumentProvider();
        if (provider == null) {
            return;
        }
        IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
        if (!(model instanceof IAnnotationModelExtension)) {
            return;
        }
        IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;
        IDocument document = provider.getDocument(editor.getEditorInput());

        GcovAnnotationModel coveragemodel = (GcovAnnotationModel) modelex.getAnnotationModel(KEY);
        if (coveragemodel == null) {
            coveragemodel = new GcovAnnotationModel(editor, document);
            modelex.addAnnotationModel(KEY, coveragemodel);
        } else {
            coveragemodel.updateAnnotations(false);
        }
    }

    public static void clear (ITextEditor editor) {
        IDocumentProvider provider = editor.getDocumentProvider();
        if (provider == null) {
            return;
        }
        IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
        if (!(model instanceof IAnnotationModelExtension)) {
            return;
        }
        IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;
        IAnnotationModel coverageModel = modelex.getAnnotationModel(KEY);
        if (coverageModel instanceof GcovAnnotationModel) {
            ((GcovAnnotationModel) coverageModel).clear();
        }
    }

    private void updateAnnotations(boolean force) {
    	// We used to not annotate any editor displaying content of an element whose project was not tracked.
    	// This logic fails when we have a linked-in file which won't point back to a project that has
    	// been registered so it has been removed.

    	SourceFile coverage = findSourceCoverageForEditor();
    	if (coverage != null) {
    		if (!annotated || force) {
    			createAnnotations(coverage);
    		}
    	} else {
    		if (annotated) {
    			clear();
    		}
    	}
    }

    private SourceFile findSourceCoverageForEditor() {
        if (editor.isDirty()) {
            return null;
        }
        final IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return null;
        }
        ICElement element = CDTUITools.getEditorInputCElement(input);
        if (element == null) {
            return null;
        }
        return findSourceCoverageForElement(element);
    }

    // Private resource proxy visitor to run through a project's resources to see if
    // it contains a link to a C element's resource.  This allows us to locate the
    // project (and it's binary) that has gcov data for a particular resource that has been linked into
    // the project.  We can't just query the resource for it's project in such a case.  This
    // is part of the fix for bug: 447554
    private class FindLinkedResourceVisitor implements IResourceProxyVisitor {

    	final private ICElement element;
    	private boolean keepSearching = true;
    	private boolean found;

    	public FindLinkedResourceVisitor(ICElement element) {
    		this.element = element;
    	}

    	public boolean foundElement() {
    		return found;
    	}

    	@Override
    	public boolean visit(IResourceProxy proxy) {
    		if (proxy.isLinked() && proxy.requestResource().getLocationURI().equals(element.getLocationURI())) {
    			found = true;
    			keepSearching = false;
    		}
    		return keepSearching;
    	}

    }

    private SourceFile findSourceCoverageForElement(ICElement element) {
    	List<SourceFile> sources = new ArrayList<> ();
    	ICProject cProject = element.getCProject();
    	IPath target = GcovAnnotationModelTracker.getInstance().getBinaryPath(cProject.getProject());
    	if (target == null) {
    		// We cannot find a target for this element, using it's project.
    		// This can be caused by linking in a file to the project which may
    		// not have a project or may point to another unseen project if the file originated
    		// there.
    		IProject[] trackedProjects = GcovAnnotationModelTracker.getInstance().getTrackedProjects();
    		for (IProject proj : trackedProjects) {
    			// Look at all projects that are registered for gcov viewing and see if the
    			// element is linked in.
    			try {
    				FindLinkedResourceVisitor visitor = new FindLinkedResourceVisitor(element);
    				proj.accept(visitor, IResource.DEPTH_INFINITE);
    				// If we find a match, make note of the target and the real C project.
    				if (visitor.foundElement()) {
    					target = GcovAnnotationModelTracker.getInstance().getBinaryPath(proj);
    					cProject = CoreModel.getDefault().getCModel().getCProject(proj.getName());
    					break;
    				}
    			} catch (CoreException e) {
    			}
    		}
    		if (target == null)
    			return null;
    	}

    	try {
    		IBinary[] binaries = cProject.getBinaryContainer().getBinaries();
    		for (IBinary b : binaries) {
    			if (b.getResource().getLocation().equals(target)) {
    				CovManager covManager = new CovManager(b.getResource().getLocation().toOSString());
    				covManager.processCovFiles(covManager.getGCDALocations(), null);
    				sources.addAll(covManager.getAllSrcs());
    			}
    		}
    	} catch (IOException|CoreException|InterruptedException e) {
    	}

    	for (SourceFile sf : sources) {
    		IPath sfPath = new Path(sf.getName());
    		IFile file = STLink2SourceSupport.getFileForPath(sfPath, cProject.getProject());
    		if (file != null && element.getResource().getLocation().equals(file.getLocation())) {
    			return sf;
    		}
    	}

    	IPath binFolder = target.removeLastSegments(1);
    	for (SourceFile sf : sources) {
    		String sfPath = Paths.get(binFolder.toOSString()).resolve(sf.getName()).normalize().toString();
    		if (sfPath.equals(element.getLocationURI().getPath())) {
    			return sf;
    		}
    	}

    	return null;
    }

    private void createAnnotations(SourceFile sourceFile) {
        AnnotationModelEvent event = new AnnotationModelEvent(this);
        clear(event);
        List<Line> lines = sourceFile.getLines();

        List<Long> tmp = new ArrayList<>();
        for (Line line : lines) {
            // Remove 0 from our calculation
            if (line.getCount() != 0) {
                tmp.add(line.getCount());
            }
        }
        Long[] counts = tmp.toArray(new Long[0]);
        Arrays.sort(counts);

        float outlierThreshold = 0;
        if (!tmp.isEmpty()) {
            // Formula for outlier (upper quartile)
            final int q1 = (int) Math.floor(0.25 * counts.length);
            final int q3 = (int) Math.floor(0.75 * counts.length);
            outlierThreshold = counts[q3] + (1.5f * (counts[q3] - counts[q1]));
        }

        for (int i = 0; i < lines.size(); i++) {
            try {
                Line line = lines.get((i+1) % lines.size());
                String type = COVERAGE;
                if (line.getCount() == 0) {
                    type = NO_COVERAGE;
                } else if (line.getCount() > outlierThreshold) {
                    type = THOROUGH_COVERAGE;
                }
                if (line.exists()) {
                    GcovAnnotation ca = new GcovAnnotation(document.getLineOffset(i),
                            document.getLineLength(i), line.getCount(), type);
                    annotations.add(ca);
                    event.annotationAdded(ca);
                }
            } catch (BadLocationException e) {
            }
        }
        fireModelChanged(event);
        annotated = true;
    }

    private void clear() {
        AnnotationModelEvent event = new AnnotationModelEvent(this);
        clear(event);
        fireModelChanged(event);
        annotated = false;
    }

    private void clear(AnnotationModelEvent event) {
        for (final GcovAnnotation ca : annotations) {
            event.annotationRemoved(ca, ca.getPosition());
        }
        annotations.clear();
    }

    @Override
    public void addAnnotationModelListener(IAnnotationModelListener listener) {
        if (!annotationModelListeners.contains(listener)) {
            annotationModelListeners.add(listener);
            fireModelChanged(new AnnotationModelEvent(this, true));
        }
    }

    @Override
    public void removeAnnotationModelListener(IAnnotationModelListener listener) {
        annotationModelListeners.remove(listener);
    }

    private void fireModelChanged(AnnotationModelEvent event) {
        event.markSealed();
        if (!event.isEmpty()) {
            for (final IAnnotationModelListener l : annotationModelListeners) {
                if (l instanceof IAnnotationModelListenerExtension) {
                    ((IAnnotationModelListenerExtension) l).modelChanged(event);
                } else {
                    l.modelChanged(this);
                }
            }
        }
    }

    @Override
    public void connect(IDocument document) {
        if (this.document != document) {
            throw new IllegalArgumentException("Can't connect to different document."); //$NON-NLS-1$
        }
        for (final GcovAnnotation ca : annotations) {
            try {
                document.addPosition(ca.getPosition());
            } catch (BadLocationException ex) {
            }
        }
        if (openConnections++ == 0) {
            document.addDocumentListener(documentListener);
        }
    }

    @Override
    public void disconnect(IDocument document) {
        if (this.document != document) {
            throw new IllegalArgumentException("Can't disconnect from different document."); //$NON-NLS-1$
        }
        for (final GcovAnnotation ca : annotations) {
            document.removePosition(ca.getPosition());
        }
        if (--openConnections == 0) {
            document.removeDocumentListener(documentListener);
        }
    }

    @Override
    public Position getPosition(Annotation annotation) {
        return (annotation instanceof GcovAnnotation) ? ((GcovAnnotation) annotation).getPosition() : null;
    }

    @Override
    public Iterator<?> getAnnotationIterator() {
        return annotations.iterator();
    }

    @Override
    public void addAnnotation(Annotation annotation, Position position) {}

    @Override
    public void removeAnnotation(Annotation annotation) {}

}
