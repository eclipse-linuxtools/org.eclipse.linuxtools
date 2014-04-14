/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.editor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.EditorPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

public class SimpleDocumentProvider extends AbstractDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof IEditorInput) {
			IDocument document= new Document();
			setDocumentContent(document, (IEditorInput) element);
			return document;
		}

		return null;
	}

	/**
	 * Tries to read the file pointed at by <code>input</code> if it is an
	 * <code>IPathEditorInput</code>. If the file does not exist, <code>true</code>
	 * is returned.
	 *
	 * @param document the document to fill with the contents of <code>input</code>
	 * @param input the editor input
	 * @throws CoreException if reading the file fails
	 */
	private static void setDocumentContent(IDocument document, IEditorInput input) throws CoreException {
		Reader reader = null;
		try {
			if (input instanceof FileStoreEditorInput){
				reader = new InputStreamReader(((FileStoreEditorInput)input).getURI().toURL().openStream());
			} else if (input instanceof IPathEditorInput){
				reader= new FileReader(((IPathEditorInput)input).getPath().toFile());
			} else {
				return;
			}
		} catch (FileNotFoundException e) {
			// return empty document and save later
			return;
		} catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID ,Localization.getString("SimpleDocumentProvider.incorrectURL"), e)); //$NON-NLS-1$
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID, Localization.getString("SimpleDocumentProvider.errorCreatingFile"), e)); //$NON-NLS-1$
		}

		try {
			setDocumentContent(document, reader);
			return;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID, IStatus.OK, Localization.getString("SimpleDocumentProvider.errorCreatingFile"), e)); //$NON-NLS-1$
		}
	}

	/**
	 * Reads in document content from a reader and fills <code>document</code>
	 *
	 * @param document the document to fill
	 * @param reader the source
	 * @throws IOException if reading fails
	 */
	private static void setDocumentContent(IDocument document, Reader reader) throws IOException {
		try (Reader in= new BufferedReader(reader)) {

			StringBuffer buffer= new StringBuffer(512);
			char[] readBuffer= new char[512];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}

			document.set(buffer.toString());
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(Object element) {
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof IPathEditorInput) {
			IPathEditorInput pei= (IPathEditorInput) element;
			IPath path= pei.getPath();
			File file= path.toFile();

			try {
				file.createNewFile();

				if (file.exists()) {
					if (file.canWrite()) {
						Writer writer= new FileWriter(file);
						writeDocumentContent(document, writer, monitor);
					} else {
						throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID, IStatus.OK, "file is read-only", null)); //$NON-NLS-1$
					}
				} else {
					throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID, IStatus.OK, "error creating file", null)); //$NON-NLS-1$
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, EditorPlugin.ID, IStatus.OK, Localization.getString("errorCreatingFile"), e)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Saves the document contents to a stream.
	 *
	 * @param document the document to save
	 * @param writer the stream to save it to
	 * @param monitor a progress monitor to report progress
	 * @throws IOException if writing fails
	 */
	private static void writeDocumentContent(IDocument document, Writer writer, IProgressMonitor monitor) throws IOException {
		try (Writer out= new BufferedWriter(writer)) {
			out.write(document.get());
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isModifiable(java.lang.Object)
	 */
	@Override
	public boolean isModifiable(Object element) {
		if (element instanceof IPathEditorInput) {
			IPathEditorInput pei= (IPathEditorInput) element;
			File file= pei.getPath().toFile();
			return file.canWrite() || !file.exists(); // Allow to edit new files
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isReadOnly(java.lang.Object)
	 */
	@Override
	public boolean isReadOnly(Object element) {
		return !isModifiable(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isStateValidated(java.lang.Object)
	 */
	@Override
	public boolean isStateValidated(Object element) {
		return true;
	}
}