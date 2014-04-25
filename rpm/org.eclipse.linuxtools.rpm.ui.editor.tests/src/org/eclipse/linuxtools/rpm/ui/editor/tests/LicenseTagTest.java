/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
public class LicenseTagTest extends FileTestCase {
    @Test
    public void testSingleLicenseTag() {
        String testText = "License: EPL";
        newFile(testText);
        assertEquals("EPL", specfile.getLicense());
    }
}
