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
package org.eclipse.linuxtools.internal.dataviewers.charts.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.linuxtools.internal.dataviewers.charts.view.ChartView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.swtchart.Chart;

/**
 * An action to save a chart as an image (jpeg, gif, png)
 * 
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 */
public class SaveChartAction extends Action {

    /** The section name of the "save chart as image" action dialog settings */
    private static final String TAG_SECTION_CHARTS_SAVEACTION_STATE = "charts_saveasimg_section"; //$NON-NLS-1$
    /** The key used by the file dialog to save its file name */
    private static final String TAG_IMG_FILE_NAME = "IMG_FILE_NAME"; //$NON-NLS-1$
    /** The key used by the file dialog to save its filter path */
    private static final String TAG_IMG_FILTER_PATH = "IMG_FILTER_PATH"; //$NON-NLS-1$

    /** Image extension for jpg format */
    private static final String EXT_JPG = "*.jpg"; //$NON-NLS-1$
    /** Image extension for jpeg format */
    private static final String EXT_JPEG = "*.jpeg"; //$NON-NLS-1$
    /** Image extension for png format */
    private static final String EXT_PNG = "*.png"; //$NON-NLS-1$
    /** Image extension for gif format */
    private static final String EXT_GIF = "*.gif"; //$NON-NLS-1$
    /** The file extensions provided by the "save chart as image" file dialog */
    private static final String[] saveAsImageExt = { EXT_PNG, EXT_GIF, EXT_JPG, EXT_JPEG, "*.*" }; //$NON-NLS-1$
    /** The names associated to the files extensions provided by the "save chart as image" file dialog */
    private static final String[] saveAsImageExtNames = new String[] {
            "PNG (*.png)", "GIF (*.gif)", "JPEG (*.jpg)", "JPEG (*.jpeg)", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            Messages.ChartConstants_ALL_FILES };

    private final FileDialog dialog;
    private final Shell shell;
    private Chart cm;

    /**
     * Constructor
     * 
     * @param shell
     *            the shell used by the dialogs
     */
    public SaveChartAction(Shell shell, ChartView cview) {
        super(Messages.ChartConstants_SAVE_CHART_AS + "...", Activator.getImageDescriptor("icons/save_chart.gif")); //$NON-NLS-1$ //$NON-NLS-2$
        this.setEnabled(false);
        this.shell = shell;
        this.dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(saveAsImageExt);
        dialog.setFilterNames(saveAsImageExtNames);
        dialog.setText(Messages.ChartConstants_SAVE_CHART_DIALOG_TEXT);
        // restore state if there is one saved
        restoreState();
    }

    /**
     * Sets the image plugins on the chart and enables the action if chart is not null.
     * 
     * @param chart
     */
    public void setChart(Chart chart) {
        if (chart != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        cm = chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        String path = dialog.open();
        if (path == null) {
            // cancel pressed
            return;
        }
        final File file = new File(path);
        if (file.exists()) {
            boolean overwrite = MessageDialog.openQuestion(shell, Messages.ChartConstants_CONFIRM_OVERWRITE_TITLE,
                    Messages.ChartConstants_CONFIRM_OVERWRITE_MSG);
            if (overwrite) {
                file.delete();
            } else {
                return;
            }
        }

        final String ext = dialog.getFilterNames()[dialog.getFilterIndex()];

        UIJob saveAsImage = new UIJob(Messages.ChartConstants_SAVE_CHART_AS + " " + file.getName()) { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                int extention;
                if (EXT_GIF.equals(ext)) {
                    extention = SWT.IMAGE_GIF;
                } else if (EXT_JPEG.equals(ext) || EXT_JPG.equals(ext)) {
                    extention = SWT.IMAGE_JPEG;
                } else {
                    extention = SWT.IMAGE_PNG;
                }

                try {
                    monitor.beginTask(Messages.ChartConstants_SAVE_CHART_AS + " " + file.getName() + "...", //$NON-NLS-1$//$NON-NLS-2$
                            IProgressMonitor.UNKNOWN);
                    file.createNewFile();
                    generateImageFile(file, extention);
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ChartConstants_ERROR_SAVING_CHART
                            + " (" + file.getAbsolutePath() + "):\n" + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        };
        saveAsImage.setUser(true);
        saveAsImage.schedule();

        // save the state of the dialog
        saveState();
    }

    /**
     * Restores the state of this action (file dialog)
     */
    protected void restoreState() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings()
                .getSection(TAG_SECTION_CHARTS_SAVEACTION_STATE);
        if (settings == null) {
            settings = Activator.getDefault().getDialogSettings().addNewSection(TAG_SECTION_CHARTS_SAVEACTION_STATE);
            return;
        }

        dialog.setFileName(settings.get(TAG_IMG_FILE_NAME));
        dialog.setFilterPath(settings.get(TAG_IMG_FILTER_PATH));
    }

    /**
     * Saves the state of this action (file dialog)
     */
    protected void saveState() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings()
                .getSection(TAG_SECTION_CHARTS_SAVEACTION_STATE);
        if (settings == null) {
            settings = Activator.getDefault().getDialogSettings().addNewSection(TAG_SECTION_CHARTS_SAVEACTION_STATE);
        }
        settings.put(TAG_IMG_FILE_NAME, dialog.getFileName());
        settings.put(TAG_IMG_FILTER_PATH, dialog.getFilterPath());
    }

    protected void generateImageFile(File file, int extention) {
        Display dsp = Display.getCurrent();
        GC gc = new GC(cm);
        Image img = new Image(dsp, cm.getSize().x, cm.getSize().y);
        gc.copyArea(img, 0, 0);
        gc.dispose();
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { img.getImageData() };
        imageLoader.save(file.getAbsolutePath(), extention);
    }

}
