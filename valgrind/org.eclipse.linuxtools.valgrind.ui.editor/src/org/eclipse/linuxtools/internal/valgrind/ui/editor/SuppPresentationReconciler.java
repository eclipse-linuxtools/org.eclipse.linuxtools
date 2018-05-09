/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implmentation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;

public class SuppPresentationReconciler extends PresentationReconciler {
	
	public SuppPresentationReconciler() {
		SuppressionsElementScanner elementScanner = new SuppressionsElementScanner();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(elementScanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(elementScanner);
        this.setDamager(dr, SuppressionsPartitionScanner.SUPP_TOOL);
        this.setRepairer(dr, SuppressionsPartitionScanner.SUPP_TOOL);

        dr = new DefaultDamagerRepairer(elementScanner);
        this.setDamager(dr, SuppressionsPartitionScanner.SUPP_TYPE);
        this.setRepairer(dr, SuppressionsPartitionScanner.SUPP_TYPE);

        dr = new DefaultDamagerRepairer(elementScanner);
        this.setDamager(dr, SuppressionsPartitionScanner.SUPP_CONTEXT);
        this.setRepairer(dr, SuppressionsPartitionScanner.SUPP_CONTEXT);
	}

}
