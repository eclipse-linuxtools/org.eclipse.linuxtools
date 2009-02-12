package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ValgrindTestOptionsTab extends ValgrindOptionsTab {
	private ValgrindLaunchMockPlugin mockPlugin;

	public ValgrindTestOptionsTab(ValgrindLaunchMockPlugin mockPlugin) {
		this.mockPlugin = mockPlugin;
//		ValgrindTestsPlugin.getDefault().setValgrindTab(this);
	}
	
	public String[] getTools() {
		return tools;
	}
	
	@Override
	protected ValgrindLaunchPlugin getPlugin() {
		return mockPlugin;
	}
	
	public ILaunchConfigurationTab getDynamicTab() {
		return dynamicTab;
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

}
