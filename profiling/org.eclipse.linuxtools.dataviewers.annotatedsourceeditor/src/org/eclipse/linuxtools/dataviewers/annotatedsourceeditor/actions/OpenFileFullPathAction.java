/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.AbstractSTAnnotatedSourceEditorInput;


public abstract class OpenFileFullPathAction extends AbstractOpenSourceFileAction {
	private String filepath;
	public OpenFileFullPathAction(String filepath, long ts) {
		super(filepath, ts);
		this.filepath = filepath;
	}

	@Override
	public abstract AbstractSTAnnotatedSourceEditorInput getInput(IFileStore fs);

	@Override
	public IFileStore getFileStore(){
		return 	EFS.getLocalFileSystem().getStore(new Path(filepath));
	}

}
