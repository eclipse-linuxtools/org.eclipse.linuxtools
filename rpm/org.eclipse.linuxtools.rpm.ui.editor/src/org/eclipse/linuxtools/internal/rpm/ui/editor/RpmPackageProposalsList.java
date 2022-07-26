/*******************************************************************************
 * Copyright (c) 2007, 2018 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

/**
 * This class is used to retrieve and manage the RPM package proposals.
 *
 */
public class RpmPackageProposalsList {
	private final Set<String> list = new HashSet<>();
	private IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,
			FrameworkUtil.getBundle(RpmPackageProposalsList.class).getSymbolicName());

	public RpmPackageProposalsList() {
		String rpmpkgsFile = store.getString(PreferenceConstants.P_RPM_LIST_FILEPATH);
		if (Files.exists(Paths.get(rpmpkgsFile))) {
			try {
				Set<String> newList = RpmPackageBuildProposalsJob.getPackages();
				list.clear();
				list.addAll(newList);
			} catch (IOException e) {
				RpmPackageBuildProposalsJob.update(true);
				SpecfileLog.logError(e);
			} catch (InterruptedException e) {
				// ignore
			}
		} else {
			RpmPackageBuildProposalsJob.update(true);
		}
	}

	public List<String[]> getProposals(String prefix) {
		int rpmpkgsMaxProposals = store.getInt(PreferenceConstants.P_RPM_LIST_MAX_PROPOSALS);
		List<String[]> proposalsList = new ArrayList<>(list.size());
		for (String listValue : list) {
			String[] item = new String[2];
			item[0] = listValue;
			String message = Messages.RpmPackageProposalsList_0 + rpmpkgsMaxProposals
					+ Messages.RpmPackageProposalsList_1;
			item[1] = message;
			if (item[0].startsWith(prefix)) {
				proposalsList.add(item);
			}
		}
		/*
		 * Show RPM informations only if the proposal list is less than the limit set in
		 * the RPM proposals preference page.
		 */
		if (proposalsList.size() < rpmpkgsMaxProposals) {
			List<String[]> proposalsListWithInfo = new ArrayList<>(proposalsList.size());
			for (String[] proposals : proposalsList) {
				proposals[1] = getRpmInfo(proposals[0]);
				proposalsListWithInfo.add(proposals);
			}
			return proposalsListWithInfo;
		} else {
			return proposalsList;
		}
	}

	public String getValue(String key) {
		for (String item : list) {
			if (item.equals(key.trim())) {
				return getRpmInfo(item);
			}
		}
		return null;
	}

	public String getRpmInfo(String pkgName) {
		String ret = ""; //$NON-NLS-1$
		try {
			ret = Utils.runCommandToString("rpm", "-q", pkgName, "--qf", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					getformattedRpmInformations());
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return Messages.RpmPackageProposalsList_2 + Messages.RpmPackageProposalsList_3;
		}
		// Create encoder and decoder
		CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder(); //$NON-NLS-1$
		CharsetEncoder encoder = StandardCharsets.ISO_8859_1.newEncoder();
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

}
