/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.IAnnotationProvider;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.ISTAnnotationColumn;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.linuxtools.internal.gcov.parser.Line;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.linuxtools.internal.gcov.preferences.ColorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class GcovAnnotationProvider implements IAnnotationProvider {

    private final ISTAnnotationColumn column;

    private final SourceFile sourceFile;
    private final int lineCount;

    private boolean initialized = false;
    private boolean COLORIZE_CODE;
    private boolean USE_GRADIENT;
    private Color GREEN_MAX;
    private Color GREEN_MIN;
    private Color RED;
    private final Color[] GREENCOLORS = new Color[129];

    public GcovAnnotationProvider(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
        this.lineCount = sourceFile.getLines().size();
        this.column = new CoverageAnnotationColumn(sourceFile);
    }

    private void initColors() {
        for (int i = 0; i < GREENCOLORS.length; i++) {
            GREENCOLORS[i] = null;
        }

        Display d = PlatformUI.getWorkbench().getDisplay();
        /* Recover the preferences database. */
        IPreferenceStore prefs_store = Activator.getDefault().getPreferenceStore();

        COLORIZE_CODE = prefs_store.getBoolean(ColorPreferencePage.PREFKEY_COV_USE_COLORS);
        USE_GRADIENT = prefs_store.getBoolean(ColorPreferencePage.PREFKEY_COV_USE_GRADIENT);

        /* Recover and build the colors from the preferences database. */
        GREEN_MAX = new Color(d,
                StringConverter.asRGB(prefs_store.getString(ColorPreferencePage.PREFKEY_COV_MAX_COLOR)));
        GREEN_MIN = new Color(d,
                StringConverter.asRGB(prefs_store.getString(ColorPreferencePage.PREFKEY_COV_MIN_COLOR)));
        RED = new Color(d, StringConverter.asRGB(prefs_store.getString(ColorPreferencePage.PREFKEY_COV_0_COLOR)));

        /* Colors are now allocated. */
        initialized = true;
    }

    @Override
    public Color getColor(int ln) {
        if (!initialized) {
            initColors();
        }
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (COLORIZE_CODE) {
            final int index = ln + 1;
            if (index < lineCount) {
                ArrayList<Line> lines = sourceFile.getLines();
                Line line = lines.get(index);
                if (line.exists()) {
                    long count = line.getCount();
                    if (count == 0) {
                        return RED;
                    }
                    if (!USE_GRADIENT || count == sourceFile.getmaxLineCount()) {
                        return GREEN_MAX;
                    }
                    int colorIndex = 128 - (int) ((128 * count) / sourceFile.getmaxLineCount());
                    if (GREENCOLORS[colorIndex] == null) {
                        int r = GREEN_MIN.getRed() + (GREEN_MAX.getRed() - GREEN_MIN.getRed()) / colorIndex;
                        int v = GREEN_MIN.getGreen() + (GREEN_MAX.getGreen() - GREEN_MIN.getGreen()) / colorIndex;
                        int b = GREEN_MIN.getBlue() + (GREEN_MAX.getBlue() - GREEN_MIN.getBlue()) / colorIndex;
                        GREENCOLORS[colorIndex] = new Color(display, r, v, b);
                    }
                    return GREENCOLORS[colorIndex];
                }
            }
        }
        return display.getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
    public ISTAnnotationColumn getColumn() {
        return column;
    }

    @Override
    public void dispose() {
        if (GREEN_MAX != null) {
            GREEN_MAX.dispose();
        }
        if (GREEN_MIN != null) {
            GREEN_MIN.dispose();
        }
        if (RED != null) {
            RED.dispose();
        }
        for (Color c : GREENCOLORS) {
            if (c != null) {
                c.dispose();
            }
        }
    }

}
