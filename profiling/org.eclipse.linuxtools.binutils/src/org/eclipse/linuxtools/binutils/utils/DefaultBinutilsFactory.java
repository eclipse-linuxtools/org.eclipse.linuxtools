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
 * Default binutils factory
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class DefaultBinutilsFactory implements ISTBinutilsFactory {
	
	@Override
	public Addr2line getAddr2line(String path) throws IOException {
		return new Addr2line(path);
	}

	@Override
	public CPPFilt getCPPFilt() throws IOException {
		return new CPPFilt();
	}

	@Override
	public STNM getNM(String path, STNMSymbolsHandler handler) throws IOException {
		return new STNM("nm", null, path, handler);
	}
	
	/**
	 * No availability test for default binutils.
	 */
	@Override
	public boolean testAvailability() {
		return true;
	}
}
