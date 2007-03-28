package org.eclipse.cdt.rpm.editor;

public class SuffixNumberDetector implements IStrictWordDetector {

	public boolean isEndingCharacter(char c) {
		return (c == ':');
	}

	public boolean isWordPart(char c) {
		return Character.isDigit(c);
	}

	public boolean isWordStart(char c) {
		// TODO Auto-generated method stub
		return Character.isDigit(c);
	}

}
