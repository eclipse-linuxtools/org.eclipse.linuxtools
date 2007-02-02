/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.cdt.oprofile.core.OprofileDaemonOptions;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This tab is used by the launcher to configure global oprofile run options.
 * @author keiths
 */
public class OprofileSetupTab extends AbstractLaunchConfigurationTab
{
	private Text _kernelImageFileText;
	//private Text _proccessIdFilterText;
	//private Text _processGroupFilterText;
	private Button _verboseButton;
	private Button _separateLibrariesButton;
	private Button _separateKernelButton;

	private static LaunchOptions _options = null;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return OprofileLaunchMessages.getString("tab.profileSetup.name"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config)
	{
		boolean b =  _options.isValid();
		//System.out.println("SetupTab isValid = " + b);
		return b;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config)
	{
		_options.saveConfiguration(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config)
	{
		_options.loadConfiguration(config);
		_updateDisplay();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config)
	{
		_options = new LaunchOptions();
		_options.saveConfiguration(config);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		_options = new LaunchOptions();

		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		GridData data;
		GridLayout layout;
		createVerticalSpacer(top, 1);
				
		// Create container for kernel image file selection
		Composite p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		p.setLayoutData(data);
		
		Label l = new Label(p, SWT.NONE);
		l.setText(OprofileLaunchMessages.getString("tab.profileSetup.kernelImage.label.text")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		l.setLayoutData(data);
		
		_kernelImageFileText = new Text(p, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		_kernelImageFileText.setLayoutData(data);
		_kernelImageFileText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent mev)
			{
				_handleTextModify(_kernelImageFileText);
			};
		});
		
		Button button = createPushButton(p, OprofileLaunchMessages.getString("tab.profileSetup.kernelImage.browse.button.text"), null); //$NON-NLS-1$
		final Shell shell = top.getShell();
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent sev)
			{
				_showFileDialog(shell);
			}
		});
		
		createVerticalSpacer(top, 1);
		
		// Create checkbox options container
		p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		p.setLayoutData(data);
		
		_verboseButton = _createCheckButton(p, OprofileLaunchMessages.getString("tab.profileSetup.verbose.check.text")); //$NON-NLS-1$
		_separateLibrariesButton = _createCheckButton(p, OprofileLaunchMessages.getString("tab.profileSetup.separateLibraries.check.text")); //$NON-NLS-1$
		_separateKernelButton = _createCheckButton(p, OprofileLaunchMessages.getString("tab.profileSetup.separateKernel.check.text")); //$NON-NLS-1$
	}
	
	// convenience method to create check buttons with the given label
	private Button _createCheckButton(Composite parent, String label)
	{
		final Button b = new Button(parent, SWT.CHECK);
		b.setText(label);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				_handleButtonSelected(b);
			}
		});
		
		return b;
	}
	
	// convenience method to create a text box and label with the given text
	private Text _createLabeledText(Composite parent, String text)
	{
		Label l = new Label(parent, SWT.NONE);
		l.setText(text);
		final Text t = new Text(parent, SWT.SINGLE | SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent mev)
			{
				_handleTextModify(t);
			}
		});
		t.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent ve)
			{
				_handleTextVerify(t, ve);
			}
		});
		return t;
	}
	
	// dispatches button selection events to appropriate handlers
	private void _handleButtonSelected(Button b)
	{
		if (b == _verboseButton)
			_options.setVerboseLogging(b.getSelection());
		else if (b == _separateLibrariesButton || b == _separateKernelButton)
		{
			if (_separateLibrariesButton.getSelection() && _separateKernelButton.getSelection())
				_options.setSeparateSamples(OprofileDaemonOptions.SEPARATE_ALL);
			else if (_separateLibrariesButton.getSelection())
				_options.setSeparateSamples(OprofileDaemonOptions.SEPARATE_LIBRARY);
			else if (_separateKernelButton.getSelection())
				_options.setSeparateSamples(OprofileDaemonOptions.SEPARATE_KERNEL);
			else
				_options.setSeparateSamples(OprofileDaemonOptions.SEPARATE_NONE);
		}
		
		updateLaunchConfigurationDialog();
	}
	
	// handles text modification events for all text boxes in this tab
	private void _handleTextModify(Text text)
	{
		if (text == _kernelImageFileText) {
			String errorMessage = null;
			String filename = text.getText();
			
			if (filename.length() > 0) {
				File file = new File (filename);
				if (file.exists() && file.isFile()) {
					_options.setKernelImageFile(filename);
				} else {
					String msg = OprofileLaunchMessages.getString("tab.profileSetup.kernelImage.kernel.nonexistent"); //$NON-NLS-1$
					Object[] args = new Object[] { filename };
					errorMessage = MessageFormat.format(msg, args);
				}
			} else {
				// no kernel image file
				_options.setKernelImageFile(new String());
			}

			// Update dialog and error message
			setErrorMessage(errorMessage);
			updateLaunchConfigurationDialog();
		}
	}
	
	// handles text verify events for all text boxes	
	private void _handleTextVerify(Text text, VerifyEvent ve)
	{
		if (text != _kernelImageFileText)
		{
				// Only allow numbers
				// SUCK FIXME: i18n?
				try
				{
					int count = Integer.parseInt(ve.text);
					if (count < 0)
						ve.doit = false;
				}
				catch (NumberFormatException e)
				{
					ve.doit = false;
				}			
		}
	}
	
	// Displays a file dialog to allow the user to select the kernel image file	
	private void _showFileDialog(Shell shell)
	{
		FileDialog d = new FileDialog(shell, SWT.OPEN);
		File kernel = new File(_options.getKernelImageFile());
		if (!kernel.exists())
		{
			// FIXME: linux-specific
			kernel = new File("/boot"); //$NON-NLS-1$
			if (!kernel.exists())
				kernel = new File("/"); //$NON-NLS-1$
		}
		d.setFileName(kernel.toString());
		d.setText(OprofileLaunchMessages.getString("tab.profileSetup.selectKernelDialog.text")); //$NON-NLS-1$
		String newKernel = d.open();
		if (newKernel != null)
		{
			kernel = new File (newKernel);
			if (!kernel.exists())
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				mb.setMessage(OprofileLaunchMessages.getString("tab.profileSetup.selectKernelDialog.error.kernelDoesNotExist.text")); //$NON-NLS-1$
				switch (mb.open())
				{
					case SWT.RETRY:
						// Ok, it's recursive, but it shouldn't matter
						_showFileDialog(shell);
						break;
						
					default:
					case SWT.CANCEL:
						break;
				}
			}
			else
			{
				_kernelImageFileText.setText(newKernel);
			}
		}
	}
	
	// updates the display for the current configuration of this object
	private void _updateDisplay()
	{
		_kernelImageFileText.setText(_options.getKernelImageFile());
		//_processIdFilterText.setText(_options.getProcessIdFilter());
		//_processGroupFilterText.setText(_options.getProcessGroupFilter());
		_verboseButton.setSelection(_options.getVerboseLogging());
		int how = _options.getSeparateSamples();
		boolean lib, kernel;
		switch (how)
		{
			case OprofileDaemonOptions.SEPARATE_LIBRARY:
				lib = true;	kernel = false;	break;
			case OprofileDaemonOptions.SEPARATE_KERNEL:
				lib = false;	kernel = true;		break;
			case OprofileDaemonOptions.SEPARATE_ALL:
				lib = true;	kernel = true;		break;
			default:
				lib = false;	kernel = false;	break;
		}
		_separateLibrariesButton.setSelection(lib);
		_separateKernelButton.setSelection(kernel);
	}
}
