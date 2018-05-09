/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.linuxtools.internal.valgrind.ui.Messages;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * Generates possible quick-fixes for problems reported by Valgrind plug-in
 *
 * @author rafaelmt
 */
public class ValgrindResolutionGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker) {
        String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
        if(message.contains(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message"))){ //$NON-NLS-1$
            return new IMarkerResolution[]{new WrongDeallocationResolution()};
        } else {
            return new IMarkerResolution[0];
        }
    }

    @Override
    public boolean hasResolutions(IMarker marker) {
        String message = marker.getAttribute(IMarker.MESSAGE, "" ); //$NON-NLS-1$
        return message.contains(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message")) ? true: false; //$NON-NLS-1$
    }
}
