/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others..
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implmentation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerCommentScanner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerInstructionScanner;
import org.eclipse.linuxtools.internal.docker.editor.scanner.DockerPartitionScanner;

public class DockerPresentationReconciler extends PresentationReconciler {

	public DockerPresentationReconciler() {
		super();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new DockerCommentScanner());
		this.setDamager(dr, DockerPartitionScanner.TYPE_COMMENT);
		this.setRepairer(dr, DockerPartitionScanner.TYPE_COMMENT);

		dr = new DefaultDamagerRepairer(new DockerInstructionScanner());
		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
