/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerPartitionScanner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class DockerDocumentProvider extends FileDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			DockerPartitionScanner scanner = new DockerPartitionScanner();
			IDocumentPartitioner partitioner = new FastPartitioner(scanner,
					DockerPartitionScanner.ALLOWED_CONTENT_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}

	@Override
	public String getEncoding(Object element) {
		return Charset.defaultCharset().name();
	}

	@Override
	public boolean isReadOnly(Object element) {
		if (element instanceof FileStoreEditorInput) {
			String path = ((FileStoreEditorInput) element).getURI().getPath();
			return !new File(path).canWrite();
		} else {
			return super.isReadOnly(element);
		}
	}

	@Override
	public boolean isModifiable(Object element) {
		if (element instanceof FileStoreEditorInput) {
			String path = ((FileStoreEditorInput) element).getURI().getPath();
			return new File(path).canWrite();
		} else {
			return super.isModifiable(element);
		}
	}

	@Override
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding)
			throws CoreException {
		if (editorInput instanceof FileStoreEditorInput) {
			String path = ((FileStoreEditorInput) editorInput).getURI().getPath();
			InputStream in;
			try {
				in = new FileInputStream(path);
			} catch (FileNotFoundException e) {
				return false;
			}
			super.setDocumentContent(document, in, encoding);
			return true;
		} else {
			return super.setDocumentContent(document, editorInput, encoding);
		}
	}

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		if (element instanceof FileStoreEditorInput) {

			String encoding = null;

			ElementInfo info = getElementInfo(element);
			Path filePath = Paths.get(((FileStoreEditorInput) element).getURI().getPath());

			encoding = getEncoding(element);

			Charset charset;
			try {
				charset = Charset.forName(encoding);
			} catch (UnsupportedCharsetException ex) {
				String message = NLS.bind(Messages.DockerDocumentProvider_encoding_not_supported, encoding);
				IStatus s = new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, ex);
				throw new CoreException(s);
			} catch (IllegalCharsetNameException ex) {
				String message = NLS.bind(Messages.DockerDocumentProvider_encoding_not_legal, encoding);
				IStatus s = new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, ex);
				throw new CoreException(s);
			}

			CharsetEncoder encoder = charset.newEncoder();
			encoder.onMalformedInput(CodingErrorAction.REPLACE);
			encoder.onUnmappableCharacter(CodingErrorAction.REPORT);

			InputStream stream;

			try {
				byte[] bytes;
				ByteBuffer byteBuffer = encoder.encode(CharBuffer.wrap(document.get()));
				if (byteBuffer.hasArray())
					bytes = byteBuffer.array();
				else {
					bytes = new byte[byteBuffer.limit()];
					byteBuffer.get(bytes);
				}
				stream = new ByteArrayInputStream(bytes, 0, byteBuffer.limit());
			} catch (CharacterCodingException ex) {
				Assert.isTrue(ex instanceof UnmappableCharacterException);
				String message = NLS.bind(
						Messages.DockerDocumentProvider_cannot_be_mapped
								+ Messages.DockerDocumentProvider_chars_not_supported,
						encoding);
				IStatus s = new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, EditorsUI.CHARSET_MAPPING_FAILED, message,
						null);
				throw new CoreException(s);
			}

			if (Files.exists(filePath)) {
				// inform about the upcoming content change
				fireElementStateChanging(element);
				try (FileWriter fw = new FileWriter(filePath.toFile());
						InputStreamReader istream = new InputStreamReader(stream)) {
					char[] bb = new char[1024];
					int nRead = istream.read(bb);
					while (nRead > 0) {
						fw.write(bb, 0, nRead);
						nRead = istream.read(bb);
					}
				} catch (RuntimeException | IOException x) {
					// inform about failure
					fireElementStateChangeFailed(element);
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, x.getMessage()));
				}

				// If here, the editor state will be flipped to "not dirty".
				// Thus, the state changing flag will be reset.

				if (info != null) {

					ResourceMarkerAnnotationModel model = (ResourceMarkerAnnotationModel) info.fModel;
					if (model != null)
						model.updateMarkers(info.fDocument);
				}

			} else {
				try {
					Files.createFile(filePath);
					try (FileWriter fw = new FileWriter(filePath.toFile());
							InputStreamReader istream = new InputStreamReader(stream)) {
						char[] bb = new char[1024];
						int nRead = istream.read(bb);
						while (nRead > 0) {
							fw.write(bb, 0, nRead);
							nRead = istream.read(bb);
						}
					} catch (IOException x) {
						throw x;
					}
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				} finally {
					monitor.done();
				}
			}

		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

}