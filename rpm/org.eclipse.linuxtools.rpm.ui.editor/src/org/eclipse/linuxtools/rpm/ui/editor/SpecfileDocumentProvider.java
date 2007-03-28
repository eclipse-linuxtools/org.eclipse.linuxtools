package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class SpecfileDocumentProvider extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			SpecfilePartitioner partitioner =
				new SpecfilePartitioner(
					new SpecfilePartitionScanner(),
					SpecfilePartitionScanner.SPEC_PARTITION_TYPES);
			partitioner.connect(document, false);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}