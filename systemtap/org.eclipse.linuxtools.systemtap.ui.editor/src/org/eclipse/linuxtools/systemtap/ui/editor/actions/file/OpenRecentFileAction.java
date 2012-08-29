/*******************************************************************************
 * Copyright (c) 2006
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.editor.actions.file;

import java.io.File;

import org.eclipse.linuxtools.internal.systemtap.ui.editor.RecentFileLog;



public class OpenRecentFileAction extends OpenFileAction {
	public OpenRecentFileAction(int item) {
		super();
		
		index = item;
		update();
		super.init(getWorkbenchWindow());
	}
	
	@Override
	protected File queryFile() {
		int index = Integer.parseInt(this.getText().substring(0, 1));
		
		String path = RecentFileLog.getString("path" + (index-1));
		if (path != null && path.length() > 0)
			return new File(path);
		return null;
	}

	public void update() {
		String name = RecentFileLog.getString("file" + index);
		this.setText(index+1 + " " + (name != null ? name : "NA"));
	}

	private int index;
}
