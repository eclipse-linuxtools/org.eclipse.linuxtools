/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation 
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.parsers;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;


/**
 * @author pmuldoon (Phil Muldoon)
 */
public class CompareParser implements IParserChangeLogContrib {

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorPart)
	 */
	public String parseCurrentFunction(IEditorPart editor) throws CoreException {
		if (editor instanceof CompareEditor) {
//			CompareEditor compare_editor = (CompareEditor) editor;
//			IEditorInput input = compare_editor.getEditorInput();
//			CompareEditorInput test = (CompareEditorInput) input;
			//System.out.println(test.getCompareResult());

			return "";
		} else
			return "";
	}

	/**
	 * @see IParserChangeLogContrib#parseCurrentFunction(IEditorInput, int)
	 */
	public String parseCurrentFunction(IEditorInput editor, int offset)
			throws CoreException {
		// TODO Auto-generated method stub
		return "";
	}

}
