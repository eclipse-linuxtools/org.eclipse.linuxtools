package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public interface IStrictWordDetector extends IWordDetector {
	public boolean isEndingCharacter(char c);
}
