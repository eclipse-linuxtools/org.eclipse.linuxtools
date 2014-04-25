/*******************************************************************************
 * Copyright (c) 2008, 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
