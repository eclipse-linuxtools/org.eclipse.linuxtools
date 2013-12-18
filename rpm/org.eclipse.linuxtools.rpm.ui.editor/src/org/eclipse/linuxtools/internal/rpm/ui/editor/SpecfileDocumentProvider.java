/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class SpecfileDocumentProvider extends TextFileDocumentProvider {

	private int originalLength;

	private void setDocumentLength(Object element) {
		IDocument doc = getDocument(element);
		if (doc == null) {
			originalLength = 0;
		} else {
			originalLength = doc.getLength();
		}
	}

	@Override
	public IDocument getDocument(Object element) {
		IDocument document = super.getDocument(element);
		if (document != null && document.getDocumentPartitioner() == null) {
			FastPartitioner partitioner = new FastPartitioner(
					new SpecfilePartitionScanner(),
					SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
			partitioner.connect(document, false);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#connect(java.lang.Object)
	 */
	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		setDocumentLength(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#canSaveDocument(java.lang.Object)
	 */
	@Override
	public boolean canSaveDocument(Object element) {
		if (element instanceof FileStoreEditorInput) {
			FileStoreEditorInput fei = (FileStoreEditorInput) element;
			IDocument doc = getDocument(element);
			if (!super.canSaveDocument(element)) {
				return false;
			}
			if (doc.getLength() != originalLength) {
				return true;
			}
			URI uri = fei.getURI();
			File f = URIUtil.toFile(uri);
			if (originalLength != 0) {
				try (BufferedReader input = new BufferedReader(
						new FileReader(f))) {
					boolean finished = false;
					char[] buffer = new char[100];
					int curoffset = 0;
					while (!finished) {
						int len = input.read(buffer);
						if (len <= 0) {
							break;
						}
						String origbytes = new String(buffer, 0, len);
						String curbytes = doc.get(curoffset, len);
						if (!curbytes.equals(origbytes)) {
							return true;
						}
						curoffset += len;
					}
					resetDocument(element);
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		}
		return super.canSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#createSaveOperation(java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */

	@Override
	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document, final boolean overwrite) throws CoreException {
		final DocumentProviderOperation saveOperation = super.createSaveOperation(element, document, overwrite);

		if (element instanceof IURIEditorInput) {
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				public void execute(IProgressMonitor monitor) throws CoreException {
				}

				/*
				 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					saveOperation.run(monitor);
					// Here's where we sneak in resetting the original document length
					setDocumentLength(element);
				}

				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				@Override
				public ISchedulingRule getSchedulingRule() {
					return saveOperation.getSchedulingRule();
				}
			};
		}

		return saveOperation;
	}
}