/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
