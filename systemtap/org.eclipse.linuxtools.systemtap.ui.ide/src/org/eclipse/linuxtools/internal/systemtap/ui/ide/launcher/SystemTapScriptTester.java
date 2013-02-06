/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.util.AbstractList;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class SystemTapScriptTester extends PropertyTester {

	public static final String STP_SUFFIX = ".stp"; //$NON-NLS-1$

	public SystemTapScriptTester() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		@SuppressWarnings("unchecked")
		AbstractList<Object> list = (AbstractList<Object>) receiver;
		Object selectedObject = list.get(0);

		if (selectedObject instanceof FileEditorInput){
			return ((FileEditorInput) selectedObject).getPath().toString().endsWith(STP_SUFFIX);
		}

		if (selectedObject instanceof IFile){
			return ((IFile)selectedObject).getName().endsWith(STP_SUFFIX);
		}

		if (selectedObject instanceof PathEditorInput){
			return ((PathEditorInput)selectedObject).getPath().toString().endsWith(STP_SUFFIX);
		}

		return false;
	}

}
