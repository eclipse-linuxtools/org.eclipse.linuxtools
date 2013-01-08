/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class SyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	public SyntaxColoringPreferencePage() {
		super();
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("SyntaxColoringPreferencePage.SyntaxColoringOptions"));
	}
	
	public void init(IWorkbench workbench) {
	}
	
	@Override
	protected Control createContents(Composite parent) {
		final TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);
		
		//STP Editor
		TabItem stpEditor = new TabItem(tabFolder, SWT.NULL);
		stpEditor.setText(Localization.getString("SyntaxColoringPreferencePage.STPEditor"));
		Composite comp = new Composite(tabFolder, SWT.NULL);
		stpEditor.setControl(comp);

		stpDC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_DEFAULT_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.DefaultColor"), comp);
		stpKC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_KEYWORD_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.KeywordColor"), comp);
		stpEC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_EMBEDDED_C_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.EmbeddedCColor"), comp);
		stpEE = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_EMBEDDED_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.EmbeddedColor"), comp);
		stpCC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_COMMENT_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.CommentColor"), comp);
		stpTC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_TYPE_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.TypeColor"), comp);
		stpSC = createColorFieldEditor(
				IDEPreferenceConstants.P_STP_STRING_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.StringColor"), comp);
		
		//C Editor
		TabItem cEditor = new TabItem(tabFolder, SWT.NULL);
		cEditor.setText(Localization.getString("SyntaxColoringPreferencePage.CEditor"));
		comp = new Composite(tabFolder, SWT.NULL);
		cEditor.setControl(comp);

		cDC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_DEFAULT_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.DefaultColor"), comp);
		cKC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_KEYWORD_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.KeywordColor"), comp);
		cPC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_PREPROCESSOR_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.PreprocessorColor"), comp);
		cCC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_COMMENT_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.CommentColor"), comp);
		cTC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_TYPE_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.TypeColor"), comp);
		cSC = createColorFieldEditor(
				IDEPreferenceConstants.P_C_STRING_COLOR,
				Localization.getString("SyntaxColoringPreferencePage.StringColor"), comp);

	    return tabFolder;
	}
	
	private ColorFieldEditor createColorFieldEditor(String name, String lblText, Composite parent) {
		ColorFieldEditor cfe = new ColorFieldEditor(name, lblText, parent);
		cfe.setPage(this);
		cfe.setPreferenceStore(getPreferenceStore());
		cfe.load();
		
		return cfe;
	}
	
	@Override
	protected void performDefaults() {
		stpDC.loadDefault();
		stpKC.loadDefault();
		stpEC.loadDefault();
		stpEE.loadDefault();
		stpCC.loadDefault();
		stpTC.loadDefault();
		stpSC.loadDefault();
		cDC.loadDefault();
		cKC.loadDefault();
		cPC.loadDefault();
		cCC.loadDefault();
		cTC.loadDefault();
		cSC.loadDefault();
		
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		stpDC.store();
		stpKC.store();
		stpEC.store();
		stpEE.store();
		stpCC.store();
		stpTC.store();
		stpSC.store();
		cDC.store();
		cKC.store();
		cPC.store();
		cCC.store();
		cTC.store();
		cSC.store();

		return true;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		stpDC.dispose();
		stpKC.dispose();
		stpEC.dispose();
		stpEE.dispose();
		stpCC.dispose();
		stpTC.dispose();
		stpSC.dispose();
		cDC.dispose();
		cKC.dispose();
		cPC.dispose();
		cCC.dispose();
		cTC.dispose();
		cSC.dispose();
		stpDC = null;
		stpKC = null;
		stpEC = null;
		stpEE = null;
		stpCC = null;
		stpTC = null;
		stpSC = null;
		cDC = null;
		cKC = null;
		cPC = null;
		cCC = null;
		cTC = null;
		cSC = null;
	}
	
	private ColorFieldEditor stpDC, stpKC, stpEC, stpEE, stpCC, stpTC, stpSC;
	private ColorFieldEditor cDC, cKC, cPC, cCC, cTC, cSC;
}
