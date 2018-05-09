/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alexander Kurtakov <akurtako@redhat.com> - migrate to junit 4 with annotations.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BasicMemcheckTest.class, DoubleClickTest.class,
        LaunchConfigTabTest.class, MarkerTest.class,
        LinkedResourceDoubleClickTest.class, LinkedResourceMarkerTest.class,
        MultiProcessTest.class, ExpandCollapseTest.class, ShortcutTest.class,
        SignalTest.class, MinVersionTest.class })
public class AllTests {
}
