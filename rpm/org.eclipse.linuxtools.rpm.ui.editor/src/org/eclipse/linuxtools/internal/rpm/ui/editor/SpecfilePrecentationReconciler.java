/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileChangelogScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePackagesScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileScanner;

public class SpecfilePrecentationReconciler extends PresentationReconciler {

	public SpecfilePrecentationReconciler() {
		SpecfileScanner scanner = new SpecfileScanner();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(new SpecfilePackagesScanner());
		setDamager(dr, SpecfilePartitionScanner.SPEC_PACKAGES);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_PACKAGES);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, SpecfilePartitionScanner.SPEC_PREP);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_PREP);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, SpecfilePartitionScanner.SPEC_SCRIPT);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_SCRIPT);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, SpecfilePartitionScanner.SPEC_FILES);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_FILES);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, SpecfilePartitionScanner.SPEC_GROUP);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_GROUP);

		dr = new DefaultDamagerRepairer(new SpecfileChangelogScanner());
		setDamager(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);
		setRepairer(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);
	}

}
