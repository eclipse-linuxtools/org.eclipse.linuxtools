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
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.memcheck.MemcheckPlugin;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public abstract class AbstractMemcheckTest extends AbstractValgrindTest {

	@Override
	public String getToolID() {
		return MemcheckPlugin.TOOL_ID;
	}

	/**
	 * Check messages appear as expected for the specified test.
	 *
	 * @param messages IValgrindMessage messages
	 * @param testName test name
	 */
	public void checkTestMessages(IValgrindMessage[] messages, String testName) {
		assertTrue(messages.length > 0);
		String lostBytesMsg = "10 bytes in 1 blocks are definitely lost in loss record 1 of 1"; //$NON-NLS-1$
		String invalidReadMsg = "Invalid read of size 1"; //$NON-NLS-1$
		String invalidWriteMsg = "Invalid write of size 1"; //$NON-NLS-1$

		for (IValgrindMessage message : messages) {
			for (IValgrindMessage child : message.getChildren()) {
				if (child instanceof ValgrindStackFrame) {
					ValgrindStackFrame stackFrameMsg = (ValgrindStackFrame) child;

					// check expected error messages exist for basicTest (child process in multiProcTest)
					if (("testNumErrors".equals(testName) || "testExec".equals(testName)) //$NON-NLS-1$ //$NON-NLS-2$
							&& "test.c".equals(stackFrameMsg.getFile())) { //$NON-NLS-1$
						assertTrue(stackFrameMsg.getLine() >= 15);
						switch (stackFrameMsg.getLine()) {
							case 15:
								assertTrue(message.getText().contains(lostBytesMsg));
								break;
							case 16:
								assertTrue(message.getText().contains(invalidReadMsg));
								break;
							case 17:
								assertTrue(message.getText().contains(invalidWriteMsg));
								break;
							default:
								break;
						}
					}

					// check expected error messages exist for parent process in multiProcTest
					if (("testNoExec".equals(testName) || "testExec".equals(testName)) //$NON-NLS-1$ //$NON-NLS-2$
							&& "parent.c".equals(stackFrameMsg.getFile())) { //$NON-NLS-1$
						assertEquals(8, stackFrameMsg.getLine());
						assertTrue(child.getParent().getText().contains(lostBytesMsg));
					}
				}
			}
		}
	}
}
