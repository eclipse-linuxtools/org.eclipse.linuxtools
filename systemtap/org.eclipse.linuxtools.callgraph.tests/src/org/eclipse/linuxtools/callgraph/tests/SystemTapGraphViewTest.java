/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
