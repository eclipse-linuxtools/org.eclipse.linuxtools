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
package org.eclipse.linuxtools.internal.rpm.ui.editor.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.StructureCreator;
import org.eclipse.compare.structuremergeviewer.StructureRootNode;
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
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;
import org.eclipse.swt.graphics.Image;

/**
 * Structure creator making which structure is based on the following tree.
 *
 * <pre>
 * ROOT_NODE
 * 		SECTIONS...N
 * 		SUB_PACKAGE...N
 * 			SUB_PACKAGE_SECTIONS...N
 * </pre>
 */
public class SpecStructureCreator extends StructureCreator {

	static class SpecNode extends DocumentRangeNode implements ITypedElement {

		public SpecNode(DocumentRangeNode parent, int type, String id,
				IDocument doc, int start, int length) {
			super(parent, type, id, doc, start, length);
			if (parent != null) {
				parent.addChild(SpecNode.this);
			}
		}

		public String getName() {
			return this.getId();
		}

		public String getType() {
			return "spec"; //$NON-NLS-1$
		}

		public Image getImage() {
			return CompareUI.getImage(getType());
		}
	}

	public String getName() {
		return Messages.SpecStructureCreator_0;
	}

	@Override
	public IStructureComparator locate(Object path, Object input) {
		return null;
	}

	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca = (IStreamContentAccessor) node;
			try {
				return readString(sca);
			} catch (CoreException ex) {
			}
		}
		return null;
	}

	private void parseSpecfile(DocumentRangeNode root, IDocument doc,
			IProgressMonitor monitor) {
		try {
			SpecfileParser parser = new SpecfileParser();
			Specfile specfile = parser.parse(doc);
			String id = specfile.getName();
			SpecNode parent = new SpecNode(root, 0, id, doc, 0, doc.getLength());
			monitor = beginWork(monitor);
			for (SpecfileSection sec : specfile.getSections()) {
				try {
					addNode(parent, doc, sec.getName(), doc.getLineOffset(sec
							.getLineNumber()), doc.getLineOffset(sec
							.getSectionEndLine())
							- doc.getLineOffset(sec.getLineNumber()));
				} catch (BadLocationException e) {
					SpecfileLog.logError(e);
				}
			}
			for (SpecfilePackage sPackage : specfile.getPackages()
					.getPackages()) {
				try {
					SpecNode pNode = addNode(parent, doc, sPackage
							.getPackageName(), doc.getLineOffset(sPackage
							.getLineNumber()), doc.getLineOffset(sPackage
							.getSectionEndLine())
							- doc.getLineOffset(sPackage.getLineNumber()));
					for (SpecfileSection section : sPackage.getSections()) {
						addNode(pNode, doc, section.getName(), doc
								.getLineOffset(section.getLineNumber()), doc
								.getLineOffset(section.getSectionEndLine())
								- doc.getLineOffset(section.getLineNumber()));
					}
				} catch (BadLocationException e) {
					SpecfileLog.logError(e);
				}
			}
		} finally {
			monitor.done();
		}
	}

	private IProgressMonitor beginWork(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
	}

	private SpecNode addNode(DocumentRangeNode root, IDocument doc,
			String name, int start, int end) {
		return new SpecNode(root, 1, name, doc, start, end);
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
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					// silently ignored
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

		DocumentRangeNode rootNode = new StructureRootNode(document, input,
				this, adapter) {
			@Override
			public boolean isEditable() {
				return isEditable;
			}
		};
		parseSpecfile(rootNode, document, monitor);
		return rootNode;
	}

}
