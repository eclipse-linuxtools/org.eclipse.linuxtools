/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and ohters.
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

package org.eclipse.linuxtools.internal.rpm.ui.editor.detectors;

import org.eclipse.jface.text.rules.IWordDetector;

public class PackageWordDetector implements IWordDetector {

	@Override
	public boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || (c == '-') || (c == '_') || (c == '+');
	}

	@Override
	public boolean isWordStart(char c) {
		return (c == ' ') || (c == ',') || (c == '\t') || (c == ':');
	}
}
