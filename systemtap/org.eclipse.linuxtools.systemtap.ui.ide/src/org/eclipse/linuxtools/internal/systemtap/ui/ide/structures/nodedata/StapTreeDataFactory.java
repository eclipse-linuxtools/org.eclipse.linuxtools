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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;


public class StapTreeDataFactory {
    public final static String NON_NODE_ID = "Object"; //$NON-NLS-1$

    public static String getDataObjectID(Object dataObject) {
        if (dataObject == null) {
            return null;
        }
        if (dataObject instanceof ProbeNodeData) {
            return ProbeNodeData.ID;
        }
        if (dataObject instanceof ProbevarNodeData) {
            return ProbevarNodeData.ID;
        }
        if (dataObject instanceof FunctionNodeData) {
            return FunctionNodeData.ID;
        }
        if (dataObject instanceof FuncparamNodeData) {
            return FuncparamNodeData.ID;
        }
        return NON_NODE_ID;
    }

    public static Object createObjectFromString(String stringOf, String objectID) {
        if (stringOf == null || objectID == null) {
            return null;
        }
        if (objectID.equals(ProbeNodeData.ID)) {
            return new ProbeNodeData(stringOf);
        }
        if (objectID.equals(ProbevarNodeData.ID)) {
            return new ProbevarNodeData(stringOf);
        }
        if (objectID.equals(FunctionNodeData.ID)) {
            return new FunctionNodeData(stringOf);
        }
        if (objectID.equals(FuncparamNodeData.ID)) {
            return new FuncparamNodeData(stringOf);
        }
        return stringOf;
    }

}
