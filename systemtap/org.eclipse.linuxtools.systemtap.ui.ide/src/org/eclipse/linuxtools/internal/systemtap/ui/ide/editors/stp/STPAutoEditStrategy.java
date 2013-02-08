/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

/**
 * Very basic auto edit strategy simply completing opening with closing brackets and quotes.
 */
public class STPAutoEditStrategy extends
		DefaultIndentLineAutoEditStrategy {
	@Override
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		if (command.text.equals("\"")) { //$NON-NLS-1$
			command.text = "\"\""; //$NON-NLS-1$
		} else if (command.text.equals("(")){ //$NON-NLS-1$
			command.text = "()"; //$NON-NLS-1$
		} else if (command.text.equals("{")) { //$NON-NLS-1$
			command.text = "{}"; //$NON-NLS-1$
		} else if (command.text.equals("[")) { //$NON-NLS-1$
			command.text = "[]"; //$NON-NLS-1$
		}
		command.caretOffset = command.offset + 1;
		command.shiftsCaret = false;

	}
}