/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import org.eclipse.swt.graphics.Color;

public interface IAnnotationProvider {
    /**
     * gets the background column of a editor line
     * 
     * @param line
     * @return
     */
    Color getColor(int line);

    /**
     * gets the ISTAnnotationColumn objects list
     * 
     * @return
     */
    ISTAnnotationColumn getColumn();

    /**
     * Dispose method
     */
    void dispose();

}
