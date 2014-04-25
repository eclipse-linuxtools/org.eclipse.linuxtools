/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
