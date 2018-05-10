/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.model;

import java.io.Serializable;
import java.util.LinkedList;

public interface TreeElement extends Serializable{
    TreeElement getParent();
    LinkedList<? extends TreeElement> getChildren();
    boolean hasChildren();
    TreeElement getRoot();

    String getName();
    int getExecutedLines();
    int getInstrumentedLines();
    int getTotalLines();
    float getCoveragePercentage();
}
