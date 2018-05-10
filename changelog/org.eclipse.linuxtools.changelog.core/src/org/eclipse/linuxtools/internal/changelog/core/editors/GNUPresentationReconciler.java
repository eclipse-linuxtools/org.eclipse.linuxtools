/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
