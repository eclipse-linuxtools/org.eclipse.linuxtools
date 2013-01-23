/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov <akurtako@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Ruler action to toglle showing the annotation column.
 *
 * @since 4.2
 *
 */
public class AnnotationColumnToggle extends AbstractRulerActionDelegate {

	@Override
	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		return new Action() {
			@Override
			public void runWithEvent(Event event) {
				toggleSTRuler();
			}

			private void toggleSTRuler() {
				// globally
				IPreferenceStore store = EditorsUI.getPreferenceStore();
				store.setValue(STAnnotatedCSourceEditor.ST_RULER,
						!isSTRulerVisible());
			}

			protected boolean isSTRulerVisible() {
				IPreferenceStore store = EditorsUI.getPreferenceStore();
				return store != null ? store
						.getBoolean(STAnnotatedCSourceEditor.ST_RULER) : true;
			}
		};
	}
}
