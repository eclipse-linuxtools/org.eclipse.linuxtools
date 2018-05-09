/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *   Ingenico  - Vincent Guignot <vincent.guignot@ingenico.com> - Add binutils strings
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.utils;

import java.io.IOException;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.resources.IProject;

/**
 * Binutils factory (especially used for cross-compile tools)
 *
 */
public interface ISTBinutilsFactory {

    Addr2line getAddr2line(String path, IProject project) throws IOException;

    CPPFilt getCPPFilt(IProject project) throws IOException;

    STNM getNM(String path, STNMSymbolsHandler handler, IProject project) throws IOException;

    /**
	 * @param project
     * @return an instance of strings for the given program
     * @throws IOException
     * @since 6.0
	 */
    STStrings getSTRINGS(IProject project) throws IOException;

    boolean testAvailability();
}
