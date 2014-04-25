/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.test;

import org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures.ConsoleStreamDaemonTest;
import org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures.ErrorStreamDaemonTest;
import org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures.ScriptConsoleTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConsoleStreamDaemonTest.class,
        ErrorStreamDaemonTest.class, ScriptConsoleTest.class })
public class AllTests {
}
