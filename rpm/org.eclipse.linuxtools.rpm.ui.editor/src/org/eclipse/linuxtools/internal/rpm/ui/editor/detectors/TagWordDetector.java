/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.detectors;

import org.eclipse.jface.text.rules.IWordDetector;

public class TagWordDetector implements IWordDetector {

	@Override
	public boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || (c == ':') || (c == '(') || (c == ')');
	}

	@Override
	public boolean isWordStart(char c) {
		return Character.isLetter(c);
	}
}
