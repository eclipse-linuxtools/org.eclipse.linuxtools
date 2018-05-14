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
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.tests;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSession;

/*
 * A faked OpModelSession object where there is no image.
 * Note: technically this shouldn't really happen in normal operation
 *       of the plugin unless there is a major b0rk-up with the xml parsers..
 */
public class TestingOpModelEvent4 extends OpModelEvent {
    public TestingOpModelEvent4(OpModelSession session, String name) {
        super(session, name);
    }
    @Override
    protected OpModelImage getNewImage() {
        return null;
    }
}
