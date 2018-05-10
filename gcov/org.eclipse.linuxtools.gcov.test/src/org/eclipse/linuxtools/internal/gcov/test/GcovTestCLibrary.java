/*******************************************************************************
 * Copyright (c) 2011, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

public class GcovTestCLibrary extends GcovTest {

    @Override
    protected String getTestProjectName() {
        return "Gcov_C_library_test";
    }

    @Override
    protected String getBinName() {
        return "libtestgcovlib.so";
    }

    @Override
    protected boolean getTestProducedReference() {
        return true;
    }

    @Override
    protected boolean useDefaultBin() {
        return false;
    }

    @Override
    public void testGcovSummaryByLaunch() {
    }

}
