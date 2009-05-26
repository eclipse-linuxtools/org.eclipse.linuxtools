/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ValgrindTestOptionsTab extends ValgrindOptionsTab {
	protected ValgrindTestLaunchPlugin launchPlugin;
	
	@Override
	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindTestLaunchPlugin.getDefault();
	}
	
	public String[] getTools() {
		return tools;
	}

	@Override
	public IValgrindToolPage getDynamicTab() throws CoreException {
		return super.getDynamicTab();
	}
	
	public Button getTraceChildrenButton() {
		return traceChildrenButton;
	}

	public Button getChildSilentButton() {
		return childSilentButton;
	}

	public Button getRunFreeresButton() {
		return runFreeresButton;
	}

	public Button getDemangleButton() {
		return demangleButton;
	}

	public Spinner getNumCallersSpinner() {
		return numCallersSpinner;
	}

	public Button getErrorLimitButton() {
		return errorLimitButton;
	}

	public Button getShowBelowMainButton() {
		return showBelowMainButton;
	}

	public Spinner getMaxStackFrameSpinner() {
		return maxStackFrameSpinner;
	}

	public Text getSuppFileText() {
		return suppFileText;
	}
	
	public Combo getToolsCombo() {
		return toolsCombo;
	}
	
	// 3.4.0 specific
	public Button getMainStackFrameButton() {
		return mainStackSizeButton;
	}

	public Spinner getMainStackFrameSpinner() {
		return mainStackSizeSpinner;
	}
}
