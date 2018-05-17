/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import org.eclipse.linuxtools.rpm.core.utils.tests.RPMQueryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RPMProjectTest.class, RPMProjectNatureTest.class, RPMProjectCreatorTest.class,
		DownloadPrepareSourcesTest.class, RPMQueryTest.class })
public class AllTests {
}
