/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core.structures;

import java.util.Arrays;

public class GraphData {
    public int xSeries;
    public int[] ySeries;
    public String key;
    public String graphID;
    public String title;

    /**
     * Creates and returns a copy of this GraphData instance.
     * @return A copy of this GraphData.
     * @since 1.1
     */
    public GraphData getCopy() {
        GraphData gd = new GraphData();
        gd.xSeries = this.xSeries;
        gd.ySeries = this.ySeries == null ? null : this.ySeries.clone();
        gd.key = this.key == null ? null : this.key;
        gd.graphID = this.graphID == null ? null : this.graphID;
        gd.title = this.title == null ? null : this.title;
        return gd;
    }

    public boolean isCopyOf(GraphData gd) {
        if (graphID == null) {
            if (gd.graphID != null) {
                return false;
            }
        } else if (!graphID.equals(gd.graphID)) {
            return false;
        }
        if (key == null) {
            if (gd.key != null) {
                return false;
            }
        } else if (!key.equals(gd.key)) {
            return false;
        }
        if (title == null) {
            if (gd.title != null) {
                return false;
            }
        } else if (!title.equals(gd.title)) {
            return false;
        }
        if (xSeries != gd.xSeries) {
            return false;
        }
        if (!Arrays.equals(ySeries, gd.ySeries)) {
            return false;
        }
        return true;
    }

}
