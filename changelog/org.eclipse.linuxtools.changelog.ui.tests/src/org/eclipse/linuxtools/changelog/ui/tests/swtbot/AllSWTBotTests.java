/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Run as SWTBot test.
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
        AddChangelogEntrySWTBotTest.class,
        PrepareChangelogSWTBotTest.class,
        DisabledPrepareChangelogSWTBotTest.class,
        CreateChangeLogFromHistorySWTBotTest.class,
        FormatChangeLogSWTBotTest.class
    }
)
public class AllSWTBotTests {
    // empty
}
