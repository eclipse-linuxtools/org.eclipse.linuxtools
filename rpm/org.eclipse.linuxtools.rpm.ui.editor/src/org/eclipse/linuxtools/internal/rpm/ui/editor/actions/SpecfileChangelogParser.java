/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class SpecfileChangelogParser implements IParserChangeLogContrib {

	@Override
	public String parseCurrentFunction(IEditorPart editor) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String parseCurrentFunction(IEditorInput input, int offset) {
		return ""; //$NON-NLS-1$
	}

}
