/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.preferences;

import org.eclipse.linuxtools.rpm.ui.editor.Activator;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * Templates preference page
 *
 */
public class SpecTemplatePreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Default constructor
	 */
	public SpecTemplatePreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setTemplateStore(Activator.getDefault().getTemplateStore());
		setContextTypeRegistry(Activator.getDefault().getContextTypeRegistry());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
	 */
	@Override
	protected boolean isShowFormatterSetting() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean ok= super.performOk();
		Activator.getDefault().savePluginPreferences();
		return ok;
	}


}
