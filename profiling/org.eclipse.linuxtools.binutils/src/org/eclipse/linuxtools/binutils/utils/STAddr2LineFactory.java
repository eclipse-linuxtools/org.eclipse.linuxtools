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
import org.eclipse.core.resources.IProject;

/**
 * addr2line factory for all toolsets.
 * 
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STAddr2LineFactory {

    /**
     * @param cpuType
     * @param programPath
     * @return an instance of addr2line for the given program
     * @throws IOException
     */
    public static Addr2line getAddr2line(String cpuType, String programPath) throws IOException {
        return getAddr2line(cpuType, programPath, null);
    }

    /**
     * @param cpuType
     * @param programPath
     * @param project
     *            The project to get the path to run addr2line
     * @return an instance of addr2line for the given program
     * @throws IOException
     */
    public static Addr2line getAddr2line(String cpuType, String programPath, IProject project) throws IOException {
        ISTBinutilsFactory factory = STBinutilsFactoryManager.sharedInstance.getBinutilsFactory(cpuType);
        return factory.getAddr2line(programPath, project);
    }

}
