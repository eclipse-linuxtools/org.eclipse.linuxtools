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

package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.preferencespages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class STAnnotatedSourceEditorPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	
	/**
	 * A named preference that controls if the line number ruler is shown in the UI
	 * (value <code>"lineNumberRuler"</code>).
	 * <p>
	 * The preference value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_ST_RULER= "STRuler"; //$NON-NLS-1$

	public STAnnotatedSourceEditorPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public STAnnotatedSourceEditorPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public STAnnotatedSourceEditorPreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
