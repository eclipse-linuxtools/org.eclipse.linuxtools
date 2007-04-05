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

package org.eclipse.linuxtools.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This class is used to retrieve and manage the RPM package proposals.
 * 
 */
public class RpmPackageProposalsList {
	private ArrayList list = new ArrayList();

	public RpmPackageProposalsList() {
		setPackagesList();
	}

	private void setPackagesList() {
		String rpmpkgsFile = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(rpmpkgsFile)));
			String line = reader.readLine();
			while (line != null) {
				list.add(line.trim());
				line = reader.readLine();
			}
		} catch (IOException e) {
			boolean isOk = showProposalsWarningDialog();
			if (isOk) {
				PreferenceDialog preferenceDialog = PreferencesUtil
				.createPreferenceDialogOn(
						Activator.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"org.eclipse.linuxtools.rpm.ui.editor.preferences.RpmProposalsPreferencePage",
						null, null);
				PreferencePage page = (PreferencePage) preferenceDialog.getSelectedPage();
				page.setErrorMessage(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_RPM_LIST_FILEPATH) + " file do not exist.\nplease click on the 'Build Proposals Now' button");
				page.setValid(false);
				preferenceDialog.open();
			}
		}
	}

	private boolean showProposalsWarningDialog() {
		boolean toogleState = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_RPM_LIST_HIDE_PROPOSALS_WARNING);
		boolean isOK = false;
		if (!toogleState)  {
			MessageDialogWithToggle dialog = MessageDialogWithToggle
			.openYesNoQuestion(
					Display.getCurrent().getActiveShell(),
					"No RPM package proposals available",
					"RPM packages proposals file does not exist.\nDo you want to open the RPM completion preference page\nwhere you can create this file?",
					"Do not show this warning anymore", toogleState, Activator.getDefault().getPreferenceStore(),
					PreferenceConstants.P_RPM_LIST_HIDE_PROPOSALS_WARNING);
			if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RPM_LIST_HIDE_PROPOSALS_WARNING, true);
				isOK = true;
			} else {
				isOK = false;
			}
		}
		return isOK;
	}

	public List getProposals(String prefix) {
		int rpmpkgsMaxProposals = Activator.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS);
		Iterator iterator = list.iterator();
		List proposalsList = new ArrayList(list.size());
		int i = 0;
		while (iterator.hasNext()) {
			String item[] = new String[2];
			item[0] = (String) iterator.next();
			String message = "RPM information is only available\nif the proposal list is less than "
					+ rpmpkgsMaxProposals
					+ " item(s).\n\nYou can change the item limit in the \nRPM proposals preferences page.";
			item[1] = message;
			if (item[0].startsWith(prefix)) {
				proposalsList.add(item);
			}
			i++;
		}
		/*
		 * Show RPM informations only if the proposal list is less than the
		 * limit set in the RPM proposals preference page.
		 */
		if (proposalsList.size() < rpmpkgsMaxProposals) {
			iterator = proposalsList.iterator();
			List proposalsListWithInfo = new ArrayList(proposalsList.size());
			while (iterator.hasNext()) {
				String item[] = new String[2];
				item = (String[]) iterator.next();
				item[1] = getRpmInfo(item[0]);
				proposalsListWithInfo.add(item);
			}
			return proposalsListWithInfo;
		} else {
			return proposalsList;
		}
	}

	public String getRpmInfo(String pkgName) {
		String ret = "";
		String[] cmd = { "rpm", "-q", pkgName, "--qf",
				getformattedRpmInformations() };
		try {
			Process child = Runtime.getRuntime().exec(cmd);
			InputStream in = child.getInputStream();
			int c;
			while ((c = in.read()) != -1) {
				ret += ((char) c);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "Cannot retrieve RPM information.\n\n"
					+ "Please adjust your preferences:\n\nSpecfile Editor-> Macro proposals-> Package Info";
		}
		// Create encoder and decoder
		CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder();
		/*
		 * TODO: Jcharset may be used to detect the inputstream encoding if it's required?
		 * http://jchardet.sourceforge.net
		 */
		CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
		try {
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(ret));
			CharBuffer cbuf = decoder.decode(bbuf);
			ret = cbuf.toString();
		} catch (CharacterCodingException e) {
			// If an error occurs when re-encoding the output, the original
			// output is returned.
		}
		return ret;
	}

	private String getformattedRpmInformations() {
		String formatedInfoString = "";
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_NAME))
			formatedInfoString += "<b>Name: </b>%{NAME}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_VERSION))
			formatedInfoString += "<b>Version: </b>%{VERSION}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_RELEASE))
			formatedInfoString += "<b>Release: </b>%{Release}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SUMMARY))
			formatedInfoString += "<b>Summary: </b>%{SUMMARY}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_LICENSE))
			formatedInfoString += "<b>License: </b>%{LICENSE}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_GROUP))
			formatedInfoString += "<b>Group: </b>%{GROUP}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_URL))
			formatedInfoString += "<b>URL: </b>%{URL}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_INSTALLTIME))
			formatedInfoString += "<b>Installation Date: </b>%{INSTALLTIME:date}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_DESCRIPTION))
			formatedInfoString += "<b>Description: </b>%{DESCRIPTION}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_PACKAGER))
			formatedInfoString += "<b>Packager: </b>%{PACKAGER}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_VENDOR))
			formatedInfoString += "<b>Vendor: </b>%{VENDOR}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SIZE))
			formatedInfoString += "<b>Size: </b>%{SIZE} bytes<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_BUILDTIME))
			formatedInfoString += "<b>Build Date: </b>%{BUILDTIME:date}<br>";
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SOURCERPM))
			formatedInfoString += "<b>SRPM: </b>%{SOURCERPM}<br>";
		return formatedInfoString;

	}

}
