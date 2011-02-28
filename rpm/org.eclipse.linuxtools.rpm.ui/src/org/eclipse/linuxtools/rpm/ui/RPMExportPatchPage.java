/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

/**
 * @author pmuldoon
 * @version 1.0
 *
 * S/RPM  export page 2. Defines the patch page that is shown to the user when they choose
 * to export to an SRPM and patch. Defines the UI elements shown, and the basic validation (need to add to
 * this)
 */
package org.eclipse.linuxtools.rpm.ui;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class RPMExportPatchPage extends WizardPage implements Listener {
	// Checkbox Buttons
	private Button generatePatch;

	// Patch Fields
	private Text patchTag;

	private Text patchChangeLog;

	private Text patchChangeLogstamp;

	private final String valid_char_list = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-."; //$NON-NLS-1$

	/**
	 * @see java.lang.Object#Object()
	 * 
	 * Constructor for RPMExportPage class
	 */
	public RPMExportPatchPage() {
		super(
				Messages.getString("RPMExportPage.Export_SRPM"), //$NON-NLS-1$
				Messages.getString("RPMExportPage.Export_SRPM_from_project"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(Messages.getString("RPMExportPage_2.0")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 *
	 * Parent control. Creates the listbox, Destination box, and options box
	 *
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createPatchFields(composite);
		populatePatchInfo();
	}

	/**
	 * Method populatePatchInfo
	 *
	 * Populate the patch widgets with data
	 */
	private void populatePatchInfo() {

		String userName = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString(IRPMConstants.AUTHOR_NAME); //$NON-NLS-1$
		String userEmail = RPMCorePlugin.getDefault().getPreferenceStore()
				.getString(IRPMConstants.AUTHOR_EMAIL); //$NON-NLS-1$

		// Populate the changeLog
		Date today = new Date();
		SimpleDateFormat df = new SimpleDateFormat("E MMM dd yyyy"); //$NON-NLS-1$

		patchChangeLogstamp.setText("* " + df.format(today) + //$NON-NLS-1$
				" -- " + userName + " <" + userEmail + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		patchChangeLog.setText("- "); //$NON-NLS-1$
	}

	/**
	 * Method createGenPatchFields
	 *
	 * Create the patch generation widgets
	 */
	private void createPatchFields(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Patch_Options")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Composite composite = new Composite(group, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL));

		ModifyListener patchListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleEvent(null);
			}
		};
		KeyListener patchKeyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				handleEvent(null);
			}
		};

		GridData patchTagGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		new Label(composite, SWT.NONE).setText(Messages
				.getString("RPMExportPage.Patch_Name")); //$NON-NLS-1$
		patchTag = new Text(composite, SWT.BORDER);
		patchTag.setToolTipText(Messages
				.getString("RPMExportPage.toolTip_Patch_Name")); //$NON-NLS-1$
		patchTag.setLayoutData(patchTagGridData);
		patchTag.addModifyListener(patchListener);
		patchTag.addKeyListener(patchKeyListener);

		GridData pChangelogStampGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		new Label(composite, SWT.NONE).setText(Messages
				.getString("RPMExportPage.Patch_Changelog_Stamp")); //$NON-NLS-1$

		patchChangeLogstamp = new Text(composite, SWT.BORDER);
		patchChangeLogstamp.setLayoutData(pChangelogStampGridData);
		//patchTag.addModifyListener(trapTag);
		patchChangeLogstamp.addModifyListener(patchListener);
		patchChangeLogstamp.setToolTipText(Messages
				.getString("RPMExportPage.toolTip_Changelog_Stamp")); //$NON-NLS-1$

		new Label(composite, SWT.NONE).setText(Messages
				.getString("RPMExportPage.Patch_Changelog")); //$NON-NLS-1$

		KeyListener patchChangelogListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				handleEvent(null);
			}

			public void keyReleased(KeyEvent e) {
				handleEvent(null);
				if (e.keyCode == 13) {
					if (patchChangeLog.getCaretPosition() == patchChangeLog
							.getCharCount())
						patchChangeLog.append("- "); //$NON-NLS-1$
					else if (patchChangeLog.getText(
							patchChangeLog.getCaretPosition() - 1,
							patchChangeLog.getCaretPosition() - 1).equals("\n")) //$NON-NLS-1$
						patchChangeLog.insert("- "); //$NON-NLS-1$
				}
			}
		};

		GridData pChangelogGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);

		patchChangeLog = new Text(composite, SWT.BORDER | SWT.MULTI);
		pChangelogGridData.heightHint = 7 * patchChangeLog.getLineHeight();
		patchChangeLog.setLayoutData(pChangelogGridData);
		patchChangeLog.addKeyListener(patchChangelogListener);
		patchChangeLog.addModifyListener(patchListener);
		patchChangeLog.setToolTipText(Messages
				.getString("RPMExportPage.toolTip_Changelog")); //$NON-NLS-1$

	}

	/**
	 * canFinish()
	 * 
	 * Hot validation. Called to determine whether Finish
	 * button can be set to true
	 * @return boolean. true if finish can be activated
	 */
	public boolean canFinish() {
		// Is the patch tag empty
		if (patchTag.getText().equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setDescription(Messages.getString("RPMExportPage_2.5")); //$NON-NLS-1$
			return false;
		}

		//Check for invalid character in patch name
		char[] chars = patchTag.getText().toCharArray();
		for(int i=0; i < chars.length; i++) {
			if(valid_char_list.indexOf(chars[i]) == -1) {
				setErrorMessage(Messages.getString("RPMExportPage_2.1"));
				return false;
			}
		}
		
		// Is the Changelog fields empty?
		if (patchChangeLog.getText().equals("- ") | patchChangeLog.getText().equals("") | //$NON-NLS-1$ //$NON-NLS-2$
				patchChangeLog.getText().equals("-")) {
			setErrorMessage(null);
			setDescription(Messages.getString("RPMExportPage_2.4")); //$NON-NLS-1$
			return false;
		} else if (patchTag.getText().equals("")) {
			setErrorMessage(null);
			setDescription(Messages.getString("RPMExportPage_2.5")); //$NON-NLS-1$
			return false;
		}

		// Is the time stamp empty?
		if (patchChangeLogstamp.getText().equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setDescription(Messages.getString("RPMExportPage_2.4")); //$NON-NLS-1$
			return false;
		}

		setDescription(null);
		setErrorMessage(null);
		return true;
	}
	
	public String getSelectedPatchName() {
		return patchTag.getText();
	}
	
	public String getSelectedChangelog() {
		return patchChangeLogstamp.getText() + IRPMUIConstants.LINE_SEP + 
			patchChangeLog.getText() + IRPMUIConstants.LINE_SEP + 
			IRPMUIConstants.LINE_SEP;
	}

	public void handleEvent(Event e) {
		setPageComplete(canFinish());
	}

	private String getHostName() {
		String hostname;
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return ""; //$NON-NLS-1$
		}
		// Trim off superflous stuff from the hostname
		int firstdot = hostname.indexOf("."); //$NON-NLS-1$
		int lastdot = hostname.lastIndexOf("."); //$NON-NLS-1$
		// If the two are equal, no need to trim name
		if (firstdot == lastdot) {
			return hostname;
		}
		String hosttemp = ""; //$NON-NLS-1$
		String hosttemp2 = hostname;
		while (firstdot != lastdot) {
			hosttemp = hosttemp2.substring(lastdot) + hosttemp;
			hosttemp2 = hostname.substring(0, lastdot);
			lastdot = hosttemp2.lastIndexOf("."); //$NON-NLS-1$
		}
		return hosttemp.substring(1);
	}
}
