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
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DoubleClickTest.class, ChartTests.class, TreeTest.class,
        BasicMassifTest.class, LaunchConfigTabTest.class,
        ExportWizardTest.class, MultiProcessTest.class,
        ExpandCollapseTest.class, SortTest.class, ChartExportTest.class, ShortcutTest.class })
public class AllTests {
}
