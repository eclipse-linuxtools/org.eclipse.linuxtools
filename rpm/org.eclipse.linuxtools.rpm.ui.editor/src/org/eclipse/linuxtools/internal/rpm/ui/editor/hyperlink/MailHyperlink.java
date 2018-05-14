/*******************************************************************************
 * Copyright (c) 2008, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.swt.program.Program;

/**
 * Mail hyperlink.
 */
public class MailHyperlink extends URLHyperlink {

	private String fURLString;

	public MailHyperlink(IRegion region, String urlString) {
		super(region, urlString);
		fURLString = urlString;
	}

	@Override
	public void open() {
		if (fURLString != null) {
			Program.launch(fURLString);
			fURLString = null;
			return;
		}
	}

}