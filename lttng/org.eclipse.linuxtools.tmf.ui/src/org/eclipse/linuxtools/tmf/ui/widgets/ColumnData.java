package org.eclipse.linuxtools.tmf.ui.widgets;

public class ColumnData {
    public final String header;
    public final int    width;
    public final int    alignment;

    public ColumnData(String h, int w, int a) {
        header = h;
        width = w;
        alignment = a;
    }

}
