/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - perl implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import org.eclipse.linuxtools.rpmstubby.InputType;

/**
 * Handler for the Makefile.PL stubify command.
 *
 */
public class StubifyMakefilePLHandler extends StubifyHandler {

    @Override
    protected InputType getInputType() {
        return InputType.PERL_MAKEFILE;
    }

}
