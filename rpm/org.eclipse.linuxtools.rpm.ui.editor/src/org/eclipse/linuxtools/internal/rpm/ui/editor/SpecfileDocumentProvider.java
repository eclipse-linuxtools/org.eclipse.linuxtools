/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.BadLocationException;
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
			FastPartitioner partitioner = new FastPartitioner(new SpecfilePartitionScanner(),
					SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
			partitioner.connect(document, false);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	@Override
	public void connect(Object element) throws CoreException {
		super.connect(element);
		setDocumentLength(element);
	}

	@Override
	public boolean canSaveDocument(Object element) {
		if (element instanceof FileStoreEditorInput fei) {
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
				try (BufferedReader input = new BufferedReader(new FileReader(f))) {
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
				} catch (IOException | CoreException | BadLocationException e) {
					return true;
				}
			}
		}
		return super.canSaveDocument(element);
	}

	@Override
	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document,
			final boolean overwrite) throws CoreException {
		final DocumentProviderOperation saveOperation = super.createSaveOperation(element, document, overwrite);

		if (element instanceof IURIEditorInput) {
			return new DocumentProviderOperation() {

				@Override
				public void execute(IProgressMonitor monitor) {
				}

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					saveOperation.run(monitor);
					// Here's where we sneak in resetting the original document
					// length
					setDocumentLength(element);
				}

				@Override
				public ISchedulingRule getSchedulingRule() {
					return saveOperation.getSchedulingRule();
				}
			};
		}

		return saveOperation;
	}
}