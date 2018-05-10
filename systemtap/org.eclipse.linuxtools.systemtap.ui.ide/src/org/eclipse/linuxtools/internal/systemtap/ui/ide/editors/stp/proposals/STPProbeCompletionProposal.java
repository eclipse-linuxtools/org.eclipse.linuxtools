/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc. and others.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.proposals;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetItemType;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

public class STPProbeCompletionProposal extends STPCompletionProposal {

    public STPProbeCompletionProposal(TreeNode completionNode, int prefixLength,
            int replacementOffset) {
        super(completionNode, prefixLength, replacementOffset);
    }

    @Override
    protected TapsetItemType getType() {
        return TapsetItemType.PROBE;
    }

}
