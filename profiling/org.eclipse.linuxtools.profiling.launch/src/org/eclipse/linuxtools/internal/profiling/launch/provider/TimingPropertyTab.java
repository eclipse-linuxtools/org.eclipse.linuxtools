/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider;


public class TimingPropertyTab extends AbstractProviderPropertyTab {

    public TimingPropertyTab() {
        super();
    }

    @Override
    protected String getType() {
        return "timing"; //$NON-NLS-1$
    }

    @Override
    protected String getPrefPageId() {
        return "org.eclipse.linuxtools.profiling.provider.TimingPreferencePage"; //$NON-NLS-1$
    }

}
