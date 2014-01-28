/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.tests;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;

public class TestingOpModelRoot extends OpModelRoot {
	public static final String ROOT_OUTPUT = "Session: testSession1\n\tEvent: testEvent1e1\n\t\tImage: /test/path/for/image, Count: 205000, Dependent Count: 5000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\t\t\tDependent Image: /no-vmlinux, Count: 4400\n\t\t\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\t\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\t\t\tSample: Line #: 0, Count: 299\n\t\t\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\t\t\tSample: Line #: 0, Count: 1\n\t\t\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\t\t\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\t\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\t\t\tSample: Line #: 0, Count: 100\n\t\t\t\tSymbols: bcopy, File: , Count: 40\n\t\t\t\t\tSample: Line #: 0, Count: 40\nSession: testSession2\n\tEvent: testEvent1e2\n\t\tImage: /test/path/for/image, Count: 205000, Dependent Count: 5000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\t\t\tDependent Image: /no-vmlinux, Count: 4400\n\t\t\tDependent Image: /lib64/ld-2.9.so, Count: 300\n\t\t\t\tSymbols: do_lookup_x, File: dl-lookup.c, Count: 299\n\t\t\t\t\tSample: Line #: 0, Count: 299\n\t\t\t\tSymbols: _dl_unload_cache, File: rawmemchr.c, Count: 1\n\t\t\t\t\tSample: Line #: 0, Count: 1\n\t\t\tDependent Image: /usr/lib64/libstdc++.so.6.0.10, Count: 160\n\t\t\tDependent Image: /lib64/libc-2.9.so, Count: 140\n\t\t\t\tSymbols: _IO_new_file_seekoff, File: , Count: 100\n\t\t\t\t\tSample: Line #: 0, Count: 100\n\t\t\t\tSymbols: bcopy, File: , Count: 40\n\t\t\t\t\tSample: Line #: 0, Count: 40\n\tEvent: testEvent2e2\n\t\tImage: /test/path/for/image, Count: 200000\n\t\t\tSymbols: TestFunction1(int), File: /test/path/for/src/image.cpp, Count: 180000\n\t\t\t\tSample: Line #: 42, Count: 130000\n\t\t\t\tSample: Line #: 36, Count: 40000\n\t\t\t\tSample: Line #: 31, Count: 9999\n\t\t\t\tSample: Line #: 39, Count: 1\n\t\t\tSymbols: TestFunction2(int, int), File: /test/path/for/src/image2.cpp, Count: 20000\n\t\t\t\tSample: Line #: 94, Count: 19998\n\t\t\t\tSample: Line #: 12, Count: 1\n\t\t\t\tSample: Line #: 55, Count: 1\n\tEvent: testEvent3e2\n\t\tImage: , Count: -1\n\tEvent: testEvent4e2\n"; //$NON-NLS-1$
	public static final String NAME_E1 = "testSession1"; //$NON-NLS-1$
	public static final String NAME_E2 = "testSession2"; //$NON-NLS-1$
	public static final String NAME_E1_S1 = "testEvent1e1"; //$NON-NLS-1$
	public static final String NAME_E2_S1 = "testEvent1e2"; //$NON-NLS-1$
	public static final String NAME_E2_S2 = "testEvent2e2"; //$NON-NLS-1$
	public static final String NAME_E2_S3 = "testEvent3e2"; //$NON-NLS-1$
	public static final String NAME_E2_S4 = "testEvent4e2"; //$NON-NLS-1$
	@Override
	protected OpModelSession[] getNewSessions() {
		//fake running opxml and simply return hand-made events
		OpModelSession[] e = {new OpModelSession(NAME_E1), new OpModelSession(NAME_E2), null};
		e[0].setEvents(new TestingOpModelEvent[] {new TestingOpModelEvent(e[0], NAME_E1_S1)});
		e[1].setEvents(new OpModelEvent[] {
				new TestingOpModelEvent(e[1], NAME_E2_S1),
				new TestingOpModelEvent2(e[1], NAME_E2_S2),
				new TestingOpModelEvent3(e[1], NAME_E2_S3),
				new TestingOpModelEvent4(e[1], NAME_E2_S4)});




		return e;
	}
}
