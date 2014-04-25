/***********************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 * Elliott Baron <ebaron@redhat.com> - Modified implementation
 ***********************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.charting;

import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot.TimeUnit;
import org.eclipse.swt.widgets.Composite;

public class HeapChart {

    private static String[] byteUnits = { Messages.getString("HeapChart.B"), //$NON-NLS-1$
            Messages.getString("HeapChart.KiB"), //$NON-NLS-1$
            Messages.getString("HeapChart.MiB"), //$NON-NLS-1$
            Messages.getString("HeapChart.GiB"), //$NON-NLS-1$
            Messages.getString("HeapChart.TiB") //$NON-NLS-1$
    };
    private static String[] instrUnits = { Messages.getString("HeapChart.i"), //$NON-NLS-1$
            Messages.getString("HeapChart.Ki"), //$NON-NLS-1$
            Messages.getString("HeapChart.Mi"), //$NON-NLS-1$
            Messages.getString("HeapChart.Gi"), //$NON-NLS-1$
            Messages.getString("HeapChart.Ti") //$NON-NLS-1$
    };
    private static String[] secondUnits = { Messages.getString("HeapChart.ms"), //$NON-NLS-1$
            Messages.getString("HeapChart.s") //$NON-NLS-1$
    };

    protected static final int BYTE_MULT = 1024;
    protected static final int BYTE_LIMIT = byteUnits.length - 1;
    protected static final int INSTR_MULT = 1000;
    protected static final int INSTR_LIMIT = instrUnits.length - 1;
    protected static final int MS_MULT = 1000;
    protected static final int MS_LIMIT = secondUnits.length - 1;

    protected static final int SCALING_THRESHOLD = 20;

    protected String title;
    protected String xUnits;
    protected String yUnits;
    public double [] time, dataUseful, dataExtra, dataStacks, dataTotal;
    private Composite chartControl;

    public HeapChart(MassifSnapshot[] snapshots, String title) {
        TimeUnit timeUnit = snapshots[0].getUnit();
        long xScaling = getXScaling(snapshots, timeUnit);
        long yScaling = getYScaling(snapshots);

        this.title = title;
        time = new double[snapshots.length];
        dataUseful = new double[snapshots.length];
        dataExtra = new double[snapshots.length];
        dataStacks = null;

        boolean isStack = isStackProfiled(snapshots);
        if (isStack) {
            dataStacks = new double[snapshots.length];
        }
        dataTotal = new double[snapshots.length];
        for (int i = 0; i < snapshots.length; i++) {
            time[i] = snapshots[i].getTime() / (double) xScaling;
            dataUseful[i] = snapshots[i].getHeapBytes() / (double) yScaling;
            dataExtra[i] = snapshots[i].getHeapExtra() / (double) yScaling;
            dataTotal[i] = dataUseful[i] + dataExtra[i];
            if (isStack) {
                dataStacks[i] = snapshots[i].getStacks() / (double) yScaling;
            }
        }

    }

    private boolean isStackProfiled(MassifSnapshot[] snapshots) {
        return getMaxStack(snapshots) > 0;
    }

    private long getYScaling(MassifSnapshot[] snapshots) {
        long max = getMaxValue(snapshots);

        int count = 0;
        while (max > BYTE_MULT * SCALING_THRESHOLD && count < BYTE_LIMIT) {
            max /= BYTE_MULT;
            count++;
        }

        yUnits = byteUnits[count];

        return (long) Math.pow(BYTE_MULT, count);
    }

    private long getXScaling(MassifSnapshot[] snapshots, TimeUnit unit) {
        long max = snapshots[snapshots.length - 1].getTime();
        int mult, limit;
        String[] units;
        switch (unit) {
        case BYTES:
            mult = BYTE_MULT;
            limit = BYTE_LIMIT;
            units = byteUnits;
            break;
        case INSTRUCTIONS:
            mult = INSTR_MULT;
            limit = INSTR_LIMIT;
            units = instrUnits;
            break;
        default:
            mult = MS_MULT;
            limit = MS_LIMIT;
            units = secondUnits;
            break;
        }

        int count = 0;
        while (max > mult * SCALING_THRESHOLD && count < limit) {
            max /= mult;
            count++;
        }

        xUnits = units[count];

        return (long) Math.pow(mult, count);
    }

    private static long getMaxValue(MassifSnapshot[] snapshots) {
        long max = 0;
        for (MassifSnapshot snapshot : snapshots) {
            if (snapshot.getTotal() > max) {
                max = snapshot.getTotal();
            }
        }
        return max;
    }

    private static long getMaxStack(MassifSnapshot[] snapshots) {
        long max = 0;
        for (MassifSnapshot snapshot : snapshots) {
            if (snapshot.getTotal() > max) {
                max = snapshot.getStacks();
            }
        }
        return max;
    }

    public String getXUnits() {
        return xUnits;
    }

    public String getYUnits() {
        return yUnits;
    }

    public static String[] getByteUnits() {
        return byteUnits;
    }

    public static String[] getInstrUnits() {
        return instrUnits;
    }

    public static String[] getSecondUnits() {
        return secondUnits;
    }

    public void setChartControl(Composite control) {
        chartControl = control;
    }

    public Composite getChartControl (){
        return chartControl;
    }

}
