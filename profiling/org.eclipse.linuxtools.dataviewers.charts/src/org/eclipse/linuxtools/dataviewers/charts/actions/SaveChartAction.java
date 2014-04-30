/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *    Red Hat (various) - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.actions;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;

/**
 * An action to save any {@link Composite} (typically a {@link Chart}) as an image (jpeg/jpg, bmp, png).
 *
 * @since 6.0
 */
public class SaveChartAction extends Action {

    private static final String[] EXTENSIONS =
        { "*.png", "*.bmp", "*.jpg", "*.jpeg", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String DEFAULT_EXT = "png"; //$NON-NLS-1$
    private static final String DEFAULT_TITLE = "newChart"; //$NON-NLS-1$
    private static final Map<String, Integer> EXTENSION_MAP = new HashMap<>();

    private Composite contents = null;
    private String title = null;

    static {
        EXTENSION_MAP.put("png", SWT.IMAGE_PNG); //$NON-NLS-1$
        EXTENSION_MAP.put("bmp", SWT.IMAGE_BMP); //$NON-NLS-1$
        EXTENSION_MAP.put("jpeg", SWT.IMAGE_JPEG); //$NON-NLS-1$
        EXTENSION_MAP.put("jpg", SWT.IMAGE_JPEG); //$NON-NLS-1$
    }

    public SaveChartAction() {
        super(Messages.ChartConstants_SAVE_CHART_AS, Activator.getImageDescriptor("icons/chart-save.png")); //$NON-NLS-1$
        this.setEnabled(false);
    }

    /**
     * Sets the image plugin on the contents and enables the action if contents are not null.
     * Also, a default title for the file to be saved is generated.
     * @param contents The image contents to be saved.
     */
    public void setChart(Composite contents) {
        setChart(contents, null);
    }

    /**
     * The same as {@link #setChart(Composite)}, but allows specification of a custom default
     * title for the image file to be saved.
     * @param contents The image contents to be saved.
     * @param title The default title of the image file when it is saved. Set this to <code>null</code>
     * if a title should be generated from the {@link #contents}.
     */
    public void setChart(Composite contents, String title) {
        this.contents = contents;
        if (contents != null) {
            this.title = title != null ? title : getDefaultName();
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    private String getDefaultName() {
        if (contents instanceof Chart) {
            return ((Chart) contents).getTitle().getText().replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return DEFAULT_TITLE;
        }
    }

    /**
     * Open a dialog with which to save the contents at a user-specified path.
     */
    @Override
    public void run() {
        if (problemExists()) {
            return;
        }
        File file = askForAndPrepareFile();
        if (file == null) {
            return; // Cancelled
        }
        generateImageFile(file);
    }

    /**
     * Save the previously-set contents as an image without the need for user input.
     * @param path The path to save the image to.
     */
    public void run(String path) {
        if (problemExists()) {
            return;
        }
        File file = new File(makePathWithVerifiedExt(path));
        if (shouldOverwrite(file, null)) {
            generateImageFile(new File(path));
        }
    }

    private boolean problemExists() {
        IStatus status = null;
        if (!isEnabled()) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    Messages.ChartConstants_ERROR_CHART_CLOSED);
        } else if (contents.isDisposed()) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    Messages.ChartConstants_ERROR_CHART_DISPOSED);
        }

        if (status != null) {
            ErrorDialog.openError(getWorkbenchShell(),
                    Messages.ChartConstants_ERROR_SAVING_CHART,
                    Messages.ChartConstants_ERROR_SAVING_CHART_MESSAGE, status);
            return true;
        }
        return false;
    }

    /**
     * Ask the user for the path to save the file at, and check if this path overwrites any existing file.
     * (Note that using dialog.setOverwrite(true) is insufficient, as the path name may be appended with a
     * file extension after the standard overwrite checks occur.)
     * @return A file with the specified pathname, appended with an appropriate extension.
     */
    private File askForAndPrepareFile() {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(EXTENSIONS);
        dialog.setText(Messages.ChartConstants_SAVE_CHART_DIALOG_TEXT);
        dialog.setFileName(title);

        do {
            String path = dialog.open();
            if (path == null) {
                return null; // Cancelled
            }

            path = makePathWithVerifiedExt(path);

            File file = new File(path);
            if (shouldOverwrite(file, shell)) {
                return file;
            }
            // If not overwriting, bring up dialog again (loop)
            dialog.setFileName(file.getName());
        } while (true);
    }

    private boolean shouldOverwrite(File file, Shell shell) {
        if (!file.exists()) {
            return true;
        }
        if (MessageDialog.openQuestion(shell != null ? shell :
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                Messages.ChartConstants_CONFIRM_OVERWRITE_TITLE,
                MessageFormat.format(Messages.ChartConstants_CONFIRM_OVERWRITE_MSG, file))) {
            file.delete();
            return true;
        }
        return false;
    }

    /**
     * Checks if the provided path has a valid file extension supported by {@link ImageLoader#save(String, int)}.
     * If not, a copy of the path is returned, with its extension replaced with a default one.
     */
    private String makePathWithVerifiedExt(String path) {
        String pathExt = Path.fromOSString(path).getFileExtension();
        if (pathExt == null) {
            return path.concat('.' + DEFAULT_EXT);
        }
        if (EXTENSION_MAP.containsKey(pathExt)) {
            return path;
        }
        return path.replaceAll(pathExt.concat("$"), DEFAULT_EXT); //$NON-NLS-1$
    }

    private void generateImageFile(File file) {
        // Extension is chosen based on the file name, not the dialog filter selection.
        int extension = EXTENSION_MAP.get(Path.fromOSString(file.getName()).getFileExtension());

        Display dsp = Display.getCurrent();
        GC gc = new GC(contents);
        Image img = new Image(dsp, contents.getSize().x, contents.getSize().y);
        gc.copyArea(img, 0, 0);
        gc.dispose();
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { img.getImageData() };
        imageLoader.save(file.getAbsolutePath(), extension);
    }

    private Shell getWorkbenchShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
