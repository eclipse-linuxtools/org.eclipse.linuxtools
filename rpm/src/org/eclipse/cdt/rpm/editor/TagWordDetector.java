package org.eclipse.cdt.rpm.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class TagWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		// TODO Auto-generated method stub
		return Character.isLetterOrDigit(c) || (c == ':') || (c == '(') || (c == ')');
	}

	public boolean isWordStart(char c) {
		// TODO Auto-generated method stub
		return Character.isLetter(c);
	}
}
