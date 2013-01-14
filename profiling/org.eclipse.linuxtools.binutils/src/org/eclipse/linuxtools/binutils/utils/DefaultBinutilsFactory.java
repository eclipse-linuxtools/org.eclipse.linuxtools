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
import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

/**
 * Default binutils factory
 * 
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class DefaultBinutilsFactory implements ISTBinutilsFactory {
    private static final String CPPFILT_CMD = "c++filt"; //$NON-NLS-1$
    private static final String ADDR2LINE_CMD = "addr2line"; //$NON-NLS-1$

    @Override
    public Addr2line getAddr2line(String path, IProject project) throws IOException {
        String addr2line = RuntimeProcessFactory.getFactory().whichCommand(ADDR2LINE_CMD, project);
        return new Addr2line(addr2line, path);
    }

    @Override
    public CPPFilt getCPPFilt(IProject project) throws IOException {
        return new CPPFilt(RuntimeProcessFactory.getFactory().whichCommand(CPPFILT_CMD, project));
    }

    @Override
    public STNM getNM(String path, STNMSymbolsHandler handler, IProject project) throws IOException {
        return new STNM("nm", null, path, handler, project);
    }

    /**
     * No availability test for default binutils.
     */
    @Override
    public boolean testAvailability() {
        return true;
    }
}
