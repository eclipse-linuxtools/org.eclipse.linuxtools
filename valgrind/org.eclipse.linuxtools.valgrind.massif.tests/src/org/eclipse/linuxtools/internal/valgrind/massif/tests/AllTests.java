/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
