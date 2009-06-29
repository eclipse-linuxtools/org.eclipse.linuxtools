/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.utils;

import java.io.IOException;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;

/**
 * binutils factory (especially used for cross-compile tools)
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public interface ISTBinutilsFactory {

	Addr2line getAddr2line(String path) throws IOException;

	CPPFilt getCPPFilt() throws IOException;

	STNM getNM(String path, STNMSymbolsHandler handler) throws IOException;
	
	boolean testAvailability();
}
