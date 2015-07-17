/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ColorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREFKEY_COV_USE_COLORS = "PREFKEY_COV_USE_COLORS"; //$NON-NLS-1$
    public static final String PREFKEY_COV_USE_GRADIENT = "PREFKEY_COV_USE_GRADIENT"; //$NON-NLS-1$

    public static final String PREFKEY_COV_MAX_COLOR = "PREFKEY_COV_MAX_COLOR"; //$NON-NLS-1$
    public static final String PREFKEY_COV_MIN_COLOR = "PREFKEY_COV_MIN_COLOR"; //$NON-NLS-1$
    public static final String PREFKEY_COV_0_COLOR = "PREFKEY_COV_0_COLOR"; //$NON-NLS-1$

    private ColorFieldEditor fcfeMax;
    private ColorFieldEditor fcfeMin;
    private ColorFieldEditor fcfeMno;
    private BooleanFieldEditor fbfeUseGradient;
    private BooleanFieldEditor fbfeUseColors;

    public ColorPreferencePage() {
        super(Messages.ColorPreferencePage_Title, AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/toggle.gif"), //$NON-NLS-1$
                FieldEditorPreferencePage.GRID);
        this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
        this.setDescription(Messages.ColorPreferencePage_Description);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        fbfeUseColors = new BooleanFieldEditor(PREFKEY_COV_USE_COLORS, Messages.ColorPreferencePage_ColorizeCode,
                this.getFieldEditorParent());
        this.addField(fbfeUseColors);
        fbfeUseGradient = new BooleanFieldEditor(PREFKEY_COV_USE_GRADIENT, Messages.ColorPreferencePage_UseGradient,
                this.getFieldEditorParent());
        this.addField(fbfeUseGradient);

        fcfeMax = new ColorFieldEditor(PREFKEY_COV_MAX_COLOR, Messages.ColorPreferencePage_BackColorHighest,
                this.getFieldEditorParent());
        this.addField(fcfeMax);
        fcfeMin = new ColorFieldEditor(PREFKEY_COV_MIN_COLOR, Messages.ColorPreferencePage_BackColorLowest,
                this.getFieldEditorParent());
        this.addField(fcfeMin);
        fcfeMno = new ColorFieldEditor(PREFKEY_COV_0_COLOR, Messages.ColorPreferencePage_BackColorNotCovered,
                this.getFieldEditorParent());
        this.addField(fcfeMno);
    }

    private void update() {
        boolean useColor = fbfeUseColors.getBooleanValue();
        fbfeUseGradient.setEnabled(useColor, this.getFieldEditorParent());
        fcfeMax.setEnabled(useColor, this.getFieldEditorParent());
        fcfeMno.setEnabled(useColor, this.getFieldEditorParent());
        boolean useGradient = useColor && fbfeUseGradient.getBooleanValue();
        fcfeMin.setEnabled(useGradient, this.getFieldEditorParent());
    }

    @Override
    protected void initialize() {
        super.initialize();
        update();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        update();
    }

}
