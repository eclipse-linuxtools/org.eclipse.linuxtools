package org.eclipse.cdt.rpm.editor;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class SpecfileWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
