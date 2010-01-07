/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.stgcov.test.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.st.stgcov.test.STgcovGCDARetrieverTest;
import com.st.stgcov.test.STgcovParserTest;
import com.st.stgcov.test.STgcovViewTest;

public class RootGcovTests {

	public static Test suite() {
		TestSuite ats = new TestSuite("STGCov");
		//$JUnit-BEGIN$
		ats.addTest(STgcovGCDARetrieverTest.suite());
		ats.addTest(STgcovParserTest.suite());
		ats.addTest(STgcovViewTest.suite());
		//$JUnit-END$
		return ats;		
	}

}