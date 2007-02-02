/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This class represents the C Launcher Tab in the launcher framework.
 * @author keiths
 */
public class CLaunchConfigurationTab extends AbstractLaunchConfigurationTab
{
	// The text widget displaying launch config name
	Text _launchConfigText;

	// The launch configuration
	ILaunchConfiguration _launch = null;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return OprofileLaunchMessages.getString("tab.claunch.name"); //$NON-NLS-1$
	}

	public boolean isValid(ILaunchConfiguration config)
	{
		boolean valid = false;
		
		// It is not possible to add a bogus run configuration. So only need
		// to check that the config is not null.
		String claunch;
		
		try
		{
			claunch = config.getAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, (String) null);
		}
		catch (CoreException ce)
		{
			claunch = null;
		}
		
		// Valid if claunch is not set or if it is set and project/executable exists
		if ((claunch != null && claunch.length() > 0)
			 || (true) /* how to check if project exists? */)
			valid = true;
		
		//System.out.println("CLaunchTab isValid = " + valid);
		return valid;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config)
	{
		if (_launch != null)
		{
			String memento;
			try
			{
				memento = _launch.getMemento();
			}
			catch (CoreException ce)
			{
				memento = null;
			}
			if (memento != null)
				config.setAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, memento);
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config)
	{
		String launchConfig;
		try
		{
			launchConfig = config.getAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, (String) null);
		}
		catch (CoreException ce)
		{
			launchConfig = null;
		}
		
		if (launchConfig != null)
		{
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			try
			{
				_launch = manager.getLaunchConfiguration(launchConfig);
			}
			catch (CoreException ce)
			{
				_launch = null;
			}
			
			if (_launch != null)
			{
				_launchConfigText.setText(_launch.getName());
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
	{
		config.setAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, (String) null);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		GridLayout grid = new GridLayout();
		top.setLayout(grid);
		
		createVerticalSpacer(top, 1);
		
		Composite launchTop = new Composite(top, SWT.NONE);
		GridLayout launchLayout = new GridLayout();
		launchLayout.numColumns = 2;
		launchLayout.marginHeight = 0;
		launchLayout.marginWidth = 0;
		launchTop.setLayout(launchLayout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		launchTop.setLayoutData(data);
		
		Label label = new Label(launchTop, SWT.NONE);
		label.setText(OprofileLaunchMessages.getString("tab.claunch.launchConfig.label.text")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		_launchConfigText = new Text(launchTop, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		_launchConfigText.setLayoutData(data);
		_launchConfigText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent mev)
			{
				// FIXME
			}
		});
		
		Button button = createPushButton(launchTop, OprofileLaunchMessages.getString("tab.claunch.launchConfig.button.browse.text"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent sev)
			{
				handleBrowseCLaunchConfigs();
			}
		});
	}

	/**
	 * Displays a dialog allowing the user to select a C Launch Configuration.
	 */
	private void handleBrowseCLaunchConfigs()
	{
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider()
		{
			public String getText(Object element)
			{
				// FIXME: show more info about launch config?
				ILaunchConfiguration config = (ILaunchConfiguration) element;
				return config.getName();
			}
			
			public Image getImage(Object element)
			{
				return null;
			}
		});
		dialog.setTitle(OprofileLaunchMessages.getString("tab.claunch.launchConfigDialog.title")); //$NON-NLS-1$
		dialog.setMessage(OprofileLaunchMessages.getString("tab.claunch.launchConfigDialog.message")); //$NON-NLS-1$

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType cAppType = manager.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
		ILaunchConfiguration[] configs = null;
		try
		{
			configs = manager.getLaunchConfigurations(cAppType);
		}
		catch (CoreException ce)
		{
			return;
		}
		
		dialog.setElements(configs);
		if (dialog.open() == Window.OK)
		{
			_launch = (ILaunchConfiguration) dialog.getFirstResult();
			if (_launch != null)
				_launchConfigText.setText(_launch.getName());
		}
	}
}
