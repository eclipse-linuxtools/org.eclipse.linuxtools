package org.eclipse.cdt.rpm.editor.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import com.redhat.eclipse.changelog.core.IParserChangeLogContrib;

public class SpecfileChangelogParser implements IParserChangeLogContrib {

	public SpecfileChangelogParser() {
	}

	public String parseCurrentFunction(IEditorPart editor) throws CoreException {
		return "";
	}

	public String parseCurrentFunction(IEditorInput input, int offset) throws CoreException {
		return "";
	}

}
