/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

/**
 * Identifiers for tapset content types (probes, functions, etc).
 * Useful for when saving/loading data associated with tapset contents
 * (such as manpages) and it is important to know what type of tapset
 * item provided the data.
 */
public enum TapsetItemType {
    PROBE {
        @Override
        public String toString() {
            return "probe"; //$NON-NLS-1$
        }
    },
    PROBEVAR {
        @Override
        public String toString() {
            return "variable"; //$NON-NLS-1$
        }
    },
    FUNCTION {
        @Override
        public String toString() {
            return "function"; //$NON-NLS-1$
        }
    },
    TAPSET {
        @Override
        public String toString() {
            return "tapset"; //$NON-NLS-1$
        }
    };
}
