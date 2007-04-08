package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class PackageWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || (c == '-');
	}

	public boolean isWordStart(char c) {
		return Character.isLetter(c);
	}
}
