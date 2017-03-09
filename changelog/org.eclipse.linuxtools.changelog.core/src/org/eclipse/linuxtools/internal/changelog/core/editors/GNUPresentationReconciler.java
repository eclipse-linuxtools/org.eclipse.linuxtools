/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

public class GNUPresentationReconciler extends PresentationReconciler {

	public GNUPresentationReconciler() {
		GNUElementScanner scanner = new GNUElementScanner();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, GNUPartitionScanner.CHANGELOG_EMAIL);
		setRepairer(dr, GNUPartitionScanner.CHANGELOG_EMAIL);

		dr = new GNUFileEntryDamagerRepairer(scanner);
		setDamager(dr, GNUPartitionScanner.CHANGELOG_SRC_ENTRY);
		setRepairer(dr, GNUPartitionScanner.CHANGELOG_SRC_ENTRY);

		dr = new MultilineRuleDamagerRepairer(scanner);
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

}
