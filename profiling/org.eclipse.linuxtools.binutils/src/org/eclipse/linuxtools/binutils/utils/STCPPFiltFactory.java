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

import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.resources.IProject;

/**
 * c++filt factory for all toolsets.
 * 
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STCPPFiltFactory {

    /**
     * @param cpuType
     * @return an instance of cppfile for the given cpu type
     * @throws IOException
     */
    public static CPPFilt getCPPFilt(String cpuType) throws IOException {
        return getCPPFilt(cpuType, null);
    }

    /**
     * @param cpuType
     * @param project
     *            The project to get the path to run cppfilt
     * @return an instance of cppfile for the given cpu type
     * @throws IOException
     */
    public static CPPFilt getCPPFilt(String cpuType, IProject project) throws IOException {
        ISTBinutilsFactory factory = STBinutilsFactoryManager.sharedInstance.getBinutilsFactory(cpuType);
        return factory.getCPPFilt(project);
    }

}
