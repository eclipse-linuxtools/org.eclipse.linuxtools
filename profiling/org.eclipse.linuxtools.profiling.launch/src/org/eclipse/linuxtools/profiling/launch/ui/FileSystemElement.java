/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui;

/**
 * @since 2.0
 */
public class FileSystemElement {

    private String scheme;
    private String schemeLabel;
    private boolean isDefault;
    private IRemoteResourceSelectorProxy selectorProxy;

    public FileSystemElement(String scheme, String schemeLabel, boolean isDefault, IRemoteResourceSelectorProxy selectorProxy) {
        this.schemeLabel = schemeLabel;
        this.scheme = scheme;
        this.isDefault = isDefault;
        this.selectorProxy = selectorProxy;
    }

    public String getSchemeLabel() {
        return schemeLabel;
    }

    public String getScheme() {
        return scheme;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public IRemoteResourceSelectorProxy getSelectorProxy() {
        return selectorProxy;
    }

}
