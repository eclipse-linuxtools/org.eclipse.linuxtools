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
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * Generator for the rpmlint marker resolutions.
 *
 */
public class RpmlintMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	/**
	 * Rpmlint error id atribute name.
	 */
	public static final String RPMLINT_ERROR_ID = "rpmlintErrorId"; //$NON-NLS-1$

	/**
	 * Rpmlint refered text
	 */
	public static final String RPMLINT_REFFERED_CONTENT = "rpmlintrefferedContent"; //$NON-NLS-1$

	@Override
	public boolean hasResolutions(IMarker marker) {
		String rpmlintErrorId = getRpmlintErrorId(marker);
		return switch (rpmlintErrorId) {
		case SetupNotQuiet.ID, PatchNotApplied.ID, NoBuildSection.ID, MacroInChangelog.ID, RpmBuildrootUsage.ID,
				HardcodedPrefixTag.ID, HardcodedPackagerTag.ID, NoPrepSection.ID, NoInstallSection.ID ->
			true;
		default -> false;
		};
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		List<IMarkerResolution> resolutions = new ArrayList<>();
		String rpmlintErrorId = getRpmlintErrorId(marker);
		if (SetupNotQuiet.ID.equals(rpmlintErrorId)) {
			resolutions.add(new SetupNotQuiet());
		} else if (PatchNotApplied.ID.equals(rpmlintErrorId)) {
			resolutions.add(new PatchNotApplied());
		} else if (NoBuildSection.ID.equals(rpmlintErrorId)) {
			resolutions.add(new NoBuildSection());
		} else if (MacroInChangelog.ID.equals(rpmlintErrorId)) {
			resolutions.add(new MacroInChangelog());
		} else if (RpmBuildrootUsage.ID.equals(rpmlintErrorId)) {
			resolutions.add(new RpmBuildrootUsage());
		} else if (HardcodedPrefixTag.ID.equals(rpmlintErrorId)) {
			resolutions.add(new HardcodedPrefixTag());
		} else if (HardcodedPackagerTag.ID.equals(rpmlintErrorId)) {
			resolutions.add(new HardcodedPackagerTag());
		} else if (NoPrepSection.ID.equals(rpmlintErrorId)) {
			resolutions.add(new NoPrepSection());
		} else if (NoInstallSection.ID.equals(rpmlintErrorId)) {
			resolutions.add(new NoInstallSection());
		}

		return resolutions.toArray(IMarkerResolution[]::new);
	}

	/**
	 * Return the rpmlint error id attribute for the specified marker.
	 *
	 * @param marker The marker to check.
	 * @return The rpmlint error id or <code>""</code> if none.
	 */
	private static String getRpmlintErrorId(IMarker marker) {
		return marker.getAttribute(RPMLINT_ERROR_ID, ""); //$NON-NLS-1$
	}

}
