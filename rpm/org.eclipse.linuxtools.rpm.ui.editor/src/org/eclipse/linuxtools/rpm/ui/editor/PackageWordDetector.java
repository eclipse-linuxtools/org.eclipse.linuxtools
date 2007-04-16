package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class PackageWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || (c == '-') || (c == '_');
	}

	public boolean isWordStart(char c) {
		return (c == ' ') || (c == ',') || (c == '\t') || (c == ':');
	}
}
