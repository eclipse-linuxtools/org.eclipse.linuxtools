package org.eclipse.cdt.rpm.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public interface IStrictWordDetector extends IWordDetector {
	public boolean isEndingCharacter(char c);
}
