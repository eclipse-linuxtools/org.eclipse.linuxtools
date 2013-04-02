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

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;

/**
 * This class is used to retrieve and manage the RPM package proposals.
 *
 */
public class RpmPackageProposalsList {
	private HashSet<String> list = new HashSet<String>();

	public RpmPackageProposalsList() {
		setPackagesList();
	}

	private void setPackagesList() {
		String rpmpkgsFile = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
		BufferedReader reader = null;
		try {
			if (Utils.fileExist(rpmpkgsFile)) {
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(rpmpkgsFile)));
				String line = reader.readLine();
				while (line != null) {
					list.add(line.trim());
					line = reader.readLine();
				}
			} else {
				RpmPackageBuildProposalsJob.update();
			}
		} catch (IOException e) {
			RpmPackageBuildProposalsJob.update();
			SpecfileLog.logError(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
 		}
	}

	public List<String[]> getProposals(String prefix) {
		this.waitForUpdates();
		int rpmpkgsMaxProposals = Activator.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS);
		List<String[]> proposalsList = new ArrayList<String[]>(list.size());
		for (String listValue:list){
			String item[] = new String[2];
			item[0] = listValue;
			String message = Messages.RpmPackageProposalsList_0
					+ rpmpkgsMaxProposals
					+ Messages.RpmPackageProposalsList_1;
			item[1] = message;
			if (item[0].startsWith(prefix)) {
				proposalsList.add(item);
			}
		}
		/*
		 * Show RPM informations only if the proposal list is less than the
		 * limit set in the RPM proposals preference page.
		 */
		if (proposalsList.size() < rpmpkgsMaxProposals) {
			List<String[]> proposalsListWithInfo = new ArrayList<String[]>(proposalsList.size());
			for (String[]  proposals: proposalsList){
				proposals[1] = getRpmInfo(proposals[0]);
				proposalsListWithInfo.add(proposals);
			}
			return proposalsListWithInfo;
		} else {
			return proposalsList;
		}
	}

	public String getValue(String key) {
		for (String item :list){
			if (item.equals(key.trim())) {
				return getRpmInfo(item);
			}
		}
		return null;

	}

	public String getRpmInfo(String pkgName) {
		String ret = ""; //$NON-NLS-1$
		try {
			ret = org.eclipse.linuxtools.rpm.core.utils.Utils.runCommandToString("rpm", "-q", pkgName, "--qf",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					getformattedRpmInformations());
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return Messages.RpmPackageProposalsList_2
					+ Messages.RpmPackageProposalsList_3;
		}
		// Create encoder and decoder
		CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder(); //$NON-NLS-1$
		/*
		 * TODO: Jcharset may be used to detect the inputstream encoding if it's required?
		 * http://jchardet.sourceforge.net
		 */
		CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder(); //$NON-NLS-1$
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
		StringBuilder formatedInfoString = new StringBuilder();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_NAME)) {
			formatedInfoString.append("<b>Name: </b>%{NAME}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_VERSION)) {
			formatedInfoString.append("<b>Version: </b>%{VERSION}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_RELEASE)) {
			formatedInfoString.append("<b>Release: </b>%{Release}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SUMMARY)) {
			formatedInfoString.append("<b>Summary: </b>%{SUMMARY}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_LICENSE)) {
			formatedInfoString.append("<b>License: </b>%{LICENSE}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_GROUP)) {
			formatedInfoString.append("<b>Group: </b>%{GROUP}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_URL)) {
			formatedInfoString.append("<b>URL: </b>%{URL}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_INSTALLTIME)) {
			formatedInfoString.append("<b>Installation Date: </b>%{INSTALLTIME:date}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_DESCRIPTION)) {
			formatedInfoString.append("<b>Description: </b>%{DESCRIPTION}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_PACKAGER)) {
			formatedInfoString.append("<b>Packager: </b>%{PACKAGER}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_VENDOR)) {
			formatedInfoString.append("<b>Vendor: </b>%{VENDOR}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SIZE)) {
			formatedInfoString.append("<b>Size: </b>%{SIZE} bytes<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_BUILDTIME)) {
			formatedInfoString.append("<b>Build Date: </b>%{BUILDTIME:date}<br>"); //$NON-NLS-1$
		}
		if (store.getBoolean(PreferenceConstants.P_RPMINFO_SOURCERPM)) {
			formatedInfoString.append("<b>SRPM: </b>%{SOURCERPM}<br>"); //$NON-NLS-1$
		}
		return formatedInfoString.toString();

	}

	/**
	 * If the rpm package proposals list is being updated
	 * block this thread
	 */
	private void waitForUpdates() {
		RpmPackageBuildProposalsJob.waitForUpdates();
	}

}
