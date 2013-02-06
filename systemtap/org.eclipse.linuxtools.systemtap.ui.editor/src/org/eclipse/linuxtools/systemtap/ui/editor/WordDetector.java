/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class WordDetector implements IWordDetector {
	
	@Override
	public boolean isWordPart(char character) {
		if (character == '.') return true;
		
		return Character.isJavaIdentifierPart(character);
	}
	
	@Override
	public boolean isWordStart(char character) {
		return Character.isJavaIdentifierStart(character);
	}
}
