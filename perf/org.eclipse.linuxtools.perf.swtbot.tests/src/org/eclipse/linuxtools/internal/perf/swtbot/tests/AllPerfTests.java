/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	StatViewTest.class,
	SourceDisassemblyViewTest.class,
	StatComparisonViewTest.class,
	ReportComparisonViewTest.class})
public class AllPerfTests {
}
