/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import org.eclipse.linuxtools.rpmstubby.InputType;

/**
 * Handler for the feature.xml stubify command.
 *
 */
public class StubifyFeatureHandler extends StubifyHandler {

    @Override
    protected InputType getInputType() {
        return InputType.ECLIPSE_FEATURE;
    }

}
