/*******************************************************************************
 * Copyright (c) 2009, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.StructureCreator;
import org.eclipse.compare.structuremergeviewer.StructureRootNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.markers.SpecfileTaskHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Structure creator making which structure is based on the following tree.
 *
 * <pre>
 * ROOT_NODE
 *         SECTIONS...N
 * SPECFILE_NODE
 *         SUB_PACKAGE...N
 *             SUB_PACKAGE_SECTIONS...N
 * </pre>
 */
public class SpecStructureCreator extends StructureCreator {

    private static class SpecNode extends DocumentRangeNode implements ITypedElement {

        public SpecNode(DocumentRangeNode parent, int type, String id,
                IDocument doc, int start, int length) {
            super(parent, type, id, doc, start, length);
            if (parent != null) {
                parent.addChild(this);
            }
        }

        @Override
        public String getName() {
            return getId();
        }

        @Override
        public String getType() {
                return "spec2"; //$NON-NLS-1$
        }

        @Override
        public Image getImage() {
            return CompareUI.getImage(getType());
        }
    }

    private static final String SPECFILE_ROOT_NODE = "Specfile Sections"; //$NON-NLS-1$

    @Override
    public String getName() {
        return Messages.SpecStructureCreator_0;
    }

    @Override
    public IStructureComparator locate(Object path, Object input) {
        return null;
    }

    @Override
    public String getContents(Object node, boolean ignoreWhitespace) {
        if (node instanceof IStreamContentAccessor) {
            IStreamContentAccessor sca = (IStreamContentAccessor) node;
            try {
                return readString(sca);
            } catch (CoreException e) {
                SpecfileLog.logError(e);
            }
        }
        return null;
    }

    private void parseSpecfile(DocumentRangeNode root, IDocument doc, IFile file) {
        SpecfileParser parser = new SpecfileParser();

        // setup the error and task handlers
        // FIXME: error markers do not show
        if (file != null) {
            FileEditorInput fei = new FileEditorInput(file);
            // this allows compare editor to work with specfiles with errors
            // without it, the compare editor is blank
            try {
                SpecfileEditor.getSpecfileDocumentProvider().disconnect(fei);
                SpecfileEditor.getSpecfileDocumentProvider().connect(fei);
            } catch (CoreException e) {
                SpecfileLog.logError(e);
            }
            parser.setErrorHandler(new SpecfileErrorHandler(fei, doc));
            parser.setTaskHandler(new SpecfileTaskHandler(fei, doc));

            Specfile specfile = parser.parse(doc);
            String id = specfile.getName();
            // Be a child under parent node of specfileSectionRoot (would be rootNode)
            SpecNode fileNode = new SpecNode((DocumentRangeNode) root.getParentNode(), 1, id, doc, 0, doc.getLength());
            for (SpecfileSection sec : specfile.getSections()) {
                try {
                    addNode(root, doc, sec.getName(),
                            doc.getLineOffset(sec.getLineNumber()),
                            doc.getLineOffset(sec.getSectionEndLine()) - doc.getLineOffset(sec.getLineNumber()),
                            2);
                } catch (BadLocationException e) {
                    SpecfileLog.logError(e);
                }
            }

            // Be a child under the parent file node
            for (SpecfilePackage sPackage : specfile.getPackages()
                    .getPackages()) {
                try {
                    SpecNode pNode = addNode(fileNode, doc,    sPackage.getPackageName(),
                            doc.getLineOffset(sPackage.getLineNumber()),
                            doc.getLineOffset(sPackage.getSectionEndLine())    - doc.getLineOffset(sPackage.getLineNumber()),
                            3);
                    for (SpecfileSection section : sPackage.getSections()) {
                        addNode(pNode, doc, section.getName(),
                                doc.getLineOffset(section.getLineNumber()),
                                doc.getLineOffset(section.getSectionEndLine()) - doc.getLineOffset(section.getLineNumber()),
                                4);
                    }
                } catch (BadLocationException e) {
                    SpecfileLog.logError(e);
                }
            }

        }
    }

    private IProgressMonitor beginWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return new NullProgressMonitor();
        }
        return new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
    }

    private SpecNode addNode(DocumentRangeNode parent, IDocument doc,
            String name, int start, int end, int type) {
        return new SpecNode(parent, type, name, doc, start, end);
    }

    private static String readString(InputStream is, String encoding) {
        if (is == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            StringBuffer buffer = new StringBuffer();
            char[] part = new char[2048];
            int read = 0;
            reader = new BufferedReader(new InputStreamReader(is, encoding));

            while ((read = reader.read(part)) != -1) {
                buffer.append(part, 0, read);
            }

            return buffer.toString();

        } catch (IOException ex) {
            // NeedWork
            SpecfileLog.logError(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    SpecfileLog.logError(ex);
                }
            }
        }
        return null;
    }

    public static String readString(IStreamContentAccessor sa)
            throws CoreException {
        InputStream is = sa.getContents();
        if (is != null) {
            String encoding = null;
            if (sa instanceof IEncodedStreamContentAccessor) {
                encoding = ((IEncodedStreamContentAccessor) sa)
                        .getCharset();
            }
            if (encoding == null) {
                encoding = ResourcesPlugin.getEncoding();
            }
            return readString(is, encoding);
        }
        return null;
    }

    @Override
    protected IDocumentPartitioner getDocumentPartitioner() {
        return new FastPartitioner(new SpecfilePartitionScanner(),
                SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
    }

    @Override
    protected String getDocumentPartitioning() {
        return SpecfilePartitionScanner.SPEC_FILE_PARTITIONING;
    }

    @Override
    protected IStructureComparator createStructureComparator(Object input,
            IDocument document, ISharedDocumentAdapter adapter,
            IProgressMonitor monitor) throws CoreException {

        final boolean isEditable;
        if (input instanceof IEditableContent) {
            isEditable = ((IEditableContent) input).isEditable();
        } else {
            isEditable = false;
        }

        // get the resource being compared, but treat compare with history as null resource
        IFile file = null;
        if (input instanceof IResourceProvider) {
            IResource res = ((IResourceProvider)input).getResource();
            file = res.getAdapter(IFile.class);
        }

        DocumentRangeNode rootNode = new StructureRootNode(document, input, this, adapter) {
            @Override
            public boolean isEditable() {
                return isEditable;
            }
        };

        try {
            monitor = beginWork(monitor);
            // Section Root
            SpecNode specfileSectionRoot = new SpecNode(rootNode, 0, SPECFILE_ROOT_NODE, document, 0, document.getLength());
            parseSpecfile(specfileSectionRoot, document, file);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }

        return rootNode;
    }
}
