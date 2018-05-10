/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.callgraph.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.internal.callgraph.core.SystemTapTextView;
import org.eclipse.linuxtools.internal.callgraph.core.ViewFactory;
import org.junit.Test;

public class SystemTapGraphViewTest {
    private SystemTapTextView stapView = new SystemTapTextView();
    private String testText = "blah";

    //TODO: write some better tests here
    @Test
    public void test() {
        stapView = (SystemTapTextView)  ViewFactory.createView("org.eclipse.linuxtools.callgraph.core.staptextview");

        stapView.println(testText);
        assertEquals(stapView.getText(), testText);

        stapView.clearAll();
        assertEquals(stapView.getText(), "");
    }

}
