package org.eclipse.cdt.rpm.editor;


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
