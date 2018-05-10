/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.tests;
import org.eclipse.linuxtools.changelog.core.formatters.tests.GNUFormatTest;
import org.eclipse.linuxtools.changelog.parsers.tests.CParserTest;
import org.eclipse.linuxtools.changelog.parsers.tests.JavaParserTest;
import org.eclipse.linuxtools.changelog.tests.fixtures.TestChangeLogTestProject;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Run this suite as JUnit plug-in test.
 */
@RunWith(Suite.class)
@SuiteClasses({
    ChangeLogWriterTest.class,
    GNUFormatTest.class,
    JavaParserTest.class,
    CParserTest.class,
    // A small test for the fixture
    TestChangeLogTestProject.class
    }
)

public class AllTests {
    // empty
}
