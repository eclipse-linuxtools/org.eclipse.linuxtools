/* Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.ICColorConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPColorConstants;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.graphics.RGB;


public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		LogManager.logDebug("Start initializeDefaultPreferences:", this);
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();

		//ide
		store.setDefault(IDEPreferenceConstants.P_STORED_TREE, true);
		
		//ide.path
//		store.setDefault(PreferenceConstants.P_DEFAULT_TAPSET, "/usr/share/systemtap/tapset");
		store.setDefault(IDEPreferenceConstants.P_KERNEL_SOURCE, "");
		store.setDefault(IDEPreferenceConstants.P_EXCLUDED_KERNEL_SOURCE, 
				"CVS/" + File.pathSeparator + 
				".svn/" + File.pathSeparator +
				"{arch}/" + File.pathSeparator +
				".arch-ids/" + File.pathSeparator +
				".bzr/" + File.pathSeparator +
				"debian/" + File.pathSeparator +
				".git/");
		store.setDefault(IDEPreferenceConstants.P_REMOTE_LOCAL_KERNEL_SOURCE, PathPreferencePage.LOCAL);

		//ide.stap.tapsets
		store.setDefault(IDEPreferenceConstants.P_TAPSETS, "");

		//ide.editor
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_EDITOR_BACKGROUND, new RGB(255,255,255));
		store.setDefault(IDEPreferenceConstants.P_SHOW_LINE_NUMBERS, false);
		
		//ide.editor.codeassist
		store.setDefault(IDEPreferenceConstants.P_USE_CODE_ASSIST, true);
		store.setDefault(IDEPreferenceConstants.P_COMPLETION, IDEPreferenceConstants.P_COMPLETION_INSERT);
		store.setDefault(IDEPreferenceConstants.P_ACTIVATION_DELAY, 200);
		store.setDefault(IDEPreferenceConstants.P_ACTIVATION_TRIGGER, ".");
		
		//ide.editor.conditionalfilters
		store.setDefault(IDEPreferenceConstants.P_CONDITIONAL_FILTERS, 
				"if(pid=currentpid)" + File.pathSeparator + 
				"if(execname=cmdname)" + File.pathSeparator +
				"if(cpu=0)" + File.pathSeparator +
				"if(caller=functionname)");
		
		
		//ide.stap.stapoptions
		for(int i=0; i<IDEPreferenceConstants.P_STAP.length; i++) {
			store.setDefault(IDEPreferenceConstants.P_STAP[i][2], false);
		}
		
		for(int i=0; i<IDEPreferenceConstants.P_STAP_OPTS.length; i++) {
			store.setDefault(IDEPreferenceConstants.P_STAP_OPTS[i], "");
		}

		//ide.editor.syntaxcoloring
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_DEFAULT_COLOR, STPColorConstants.DEFAULT);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_KEYWORD_COLOR, STPColorConstants.KEYWORD);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_EMBEDDED_C_COLOR, STPColorConstants.EMBEDDEDC);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_EMBEDDED_COLOR, STPColorConstants.EMBEDDED);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_COMMENT_COLOR, STPColorConstants.COMMENT);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_TYPE_COLOR, STPColorConstants.TYPE);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_STP_STRING_COLOR, STPColorConstants.STP_STRING);

		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_DEFAULT_COLOR, ICColorConstants.DEFAULT);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_KEYWORD_COLOR, ICColorConstants.KEYWORD);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_COMMENT_COLOR, ICColorConstants.COMMENT);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_PREPROCESSOR_COLOR, ICColorConstants.PREPROCESSOR);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_TYPE_COLOR, ICColorConstants.TYPE);
		PreferenceConverter.setDefault(store, IDEPreferenceConstants.P_C_STRING_COLOR, ICColorConstants.STRING);

		LogManager.logDebug("End initializeDefaultPreferences:", this);
	}
}
