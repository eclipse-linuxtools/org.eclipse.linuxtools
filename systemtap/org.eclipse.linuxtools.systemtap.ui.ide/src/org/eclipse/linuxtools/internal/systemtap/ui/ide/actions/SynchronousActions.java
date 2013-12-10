/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

/**
 * A helper class for performing actions in the UI Thread.
 * @author Andrew Ferrazzutti
 * @since 2.2
 */
final class SynchronousActions {

	/**
	 * Given an editor reference, synchronously restores its editor and returns it.
	 * @param ref The reference from which an editor is to be restored.
	 * @return The reference's editor.
	 */
	static IEditorPart getRestoredEditor(final IEditorReference ref) {
		if (ref.getEditor(false) == null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					ref.getEditor(true);
				}
			});
		}
		return ref.getEditor(false);
	}
}
