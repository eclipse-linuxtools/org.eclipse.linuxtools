/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A structure for containing extra information of SystemTap probe variables.
 * @since 3.0
 */
public class ProbevarNodeData implements IMultiTypedNode, ICompletable {
    static final String ID = "ProbevarNodeData"; //$NON-NLS-1$
    private String text;
    private String name;
    private List<String> types;

    /**
     * @return The original line of text passed to this object, which contains the
     * variable's name and its full type.
     */
    @Override
    public String toString() {
        return text;
    }

    @Override
    public String getCompletionText() {
        return name;
    }

    /**
     * @return A list of all tokens used to describe the variable's type.
     */
    @Override
    public List<String> getTypes() {
        return types;
    }

    /**
     * Create a new instance of probe variable node information.
     * @param info A <code>String</code> formatted as "(name):(type)", which provides
     * all information pertaining to the probe variable.
     */
    public ProbevarNodeData(String info) {
        text = info.trim();
        int colonIndex = text.indexOf(':');
        if (colonIndex == -1) {
            name = text;
            types = Collections.emptyList();
        } else {
            name = text.substring(0, colonIndex);
            types = Arrays.asList(text.substring(colonIndex+1).split(" ")); //$NON-NLS-1$
        }
    }
}
