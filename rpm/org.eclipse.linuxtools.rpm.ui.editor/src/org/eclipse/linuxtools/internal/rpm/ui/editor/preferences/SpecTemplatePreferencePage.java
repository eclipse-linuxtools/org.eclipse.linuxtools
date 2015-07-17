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

package org.eclipse.linuxtools.internal.rpm.ui.editor.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Templates preference page
 *
 */
public class SpecTemplatePreferencePage extends TemplatePreferencePage {

    /**
     * Default constructor
     */
    public SpecTemplatePreferencePage() {
        super();
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTemplateStore(Activator.getDefault().getTemplateStore());
        setContextTypeRegistry(Activator.getDefault().getContextTypeRegistry());
    }

    @Override
    protected boolean isShowFormatterSetting() {
        return true;
    }


    @Override
    public boolean performOk() {
        boolean ok= super.performOk();
        try {
            InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).flush();
        } catch (BackingStoreException e) {
            //Error while saving.
            ok = false;
        }
        return ok;
    }


}
