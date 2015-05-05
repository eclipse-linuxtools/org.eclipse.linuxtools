/*******************************************************************************
 * Copyright (c) 2011-2015 STMicroelectronics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
