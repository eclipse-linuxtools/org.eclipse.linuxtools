/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.detectors;

import org.eclipse.jface.text.rules.IWordDetector;

public class PackageWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || (c == '-') || (c == '_') || (c == '+');
	}

	public boolean isWordStart(char c) {
		return (c == ' ') || (c == ',') || (c == '\t') || (c == ':');
	}
}
