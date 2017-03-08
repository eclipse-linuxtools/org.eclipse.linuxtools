/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others..
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;

public class STPPresentationReconciler extends PresentationReconciler {

	public STPPresentationReconciler() {
		ColorManager colorManager = new ColorManager();
		STPElementScanner scanner = new STPElementScanner(colorManager);
		scanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getColor(STPColorConstants.DEFAULT))));
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, STPPartitionScanner.STP_COMMENT);
		setRepairer(dr, STPPartitionScanner.STP_COMMENT);

		dr = new DefaultDamagerRepairer(scanner);
		setDamager(dr, STPPartitionScanner.STP_CONDITIONAL);
		setRepairer(dr, STPPartitionScanner.STP_CONDITIONAL);
	}
}
