package org.eclipse.linuxtools.rpm.ui.editor;


public class PatchNumberDetector implements IStrictWordDetector {

	public boolean isWordPart(char c) {
		return Character.isDigit(c);
	}

	public boolean isWordStart(char c) {
		return Character.isDigit(c);
	}
	
	public boolean isEndingCharacter(char c) {
		return Character.isWhitespace(c);
	}

}
