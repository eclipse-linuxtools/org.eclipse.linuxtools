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
