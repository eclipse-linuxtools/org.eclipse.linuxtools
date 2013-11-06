/*******************************************************************************
 * Copyright (c) 2009, 2013 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *   Red Hat Inc. - fix for bug 418264
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.TreeColumnViewerFilter;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.charts.actions.ChartAction;
import org.eclipse.linuxtools.internal.gprof.Activator;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.action.SwitchContentProviderAction;
import org.eclipse.linuxtools.internal.gprof.action.SwitchSampleTimeAction;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.parser.HistogramDecoder;
import org.eclipse.linuxtools.internal.gprof.utils.PPC64ElfBinaryObjectWrapper;
import org.eclipse.linuxtools.internal.gprof.view.fields.SampleProfField;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGArc;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGCategory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * The view where gmon file is displayed
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GmonView extends AbstractSTDataView {

	public static final String ID = "org.eclipse.linuxtools.gprof.view"; //$NON-NLS-1$

	/** WHITE color */
	public static final Color DEFAULT_BG = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	/** GREEN1 color : for children category */
	public static final Color GREEN1 = new Color(PlatformUI.getWorkbench().getDisplay(), 207, 255, 207);
	/** GREEN2 color : for children */
	public static final Color GREEN2 = new Color(PlatformUI.getWorkbench().getDisplay(), 175, 255, 175);
	/** BLUE1 color : for parent category */
	public static final Color BLUE1 = new Color(PlatformUI.getWorkbench().getDisplay(), 207, 207, 255);
	/** BLUE2 color : for parents */
	public static final Color BLUE2 = new Color(PlatformUI.getWorkbench().getDisplay(), 175, 175, 255);

	public static final int CALL_GRAPH_MODE = 0;
	public static final int SAMPLE_MODE = 1;

    private Label label;
    private Text fFilterText;
    private TreeColumnViewerFilter fViewerFilter;
    private Action action1;
    private Action action2;
    private Action action3;
    private Action action4;
    private Action switchSampleTime;

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        GridLayout l = (GridLayout) parent.getLayout();
        l.horizontalSpacing = 0;
        l.verticalSpacing = 0;
        l.marginHeight = 0;
        l.marginWidth = 0;
        fViewerFilter = new TreeColumnViewerFilter((TreeViewer) getSTViewer().getViewer(),
                getSTViewer().getAllFields()[0], true);
        getSTViewer().getViewer().addFilter(fViewerFilter);
    }

    @Override
    protected void createTitle(Composite parent) {
    	ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
    	Color background = colorRegistry.get("org.eclipse.ui.workbench.INACTIVE_TAB_BG_START"); //$NON-NLS-1$
    	label = new Label(parent, SWT.WRAP);
    	if (background != null)
    		label.setBackground(background);
    	GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1);
    	label.setLayoutData(data);
    	fFilterText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
    	fFilterText.setMessage(Messages.GmonView_type_filter_text);
    	fFilterText.setToolTipText(Messages.GmonView_filter_by_name);
    	fFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    	fFilterText.addModifyListener(new ModifyListener() {
    		@Override
    		public void modifyText(ModifyEvent e) {
    			String text = fFilterText.getText();
    			fViewerFilter.setMatchingText(text);
    		}
    	});
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#contributeToToolbar(org.eclipse.jface.action
     * .IToolBarManager)
     */
    @Override
    protected void contributeToToolbar(IToolBarManager manager) {
        manager.add(new Separator());
        manager.add(action2);
        action2.setChecked(true);
        manager.add(action3);
        manager.add(action4);
        manager.add(action1);
        manager.add(new Separator());
        manager.add(switchSampleTime);
        manager.add(new Separator());
        manager.add(new ChartAction(getViewSite().getShell(), getSTViewer()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#createActions()
     */
    @Override
    protected void createActions() {
        super.createActions();
        action1 = new SwitchContentProviderAction(
                "Display function call graph", "icons/ch_callees.png", getSTViewer().getViewer(), CallGraphContentProvider.sharedInstance); //$NON-NLS-1$ //$NON-NLS-2$
        action2 = new SwitchContentProviderAction(
                "Sort samples per file", "icons/c_file_obj.gif", getSTViewer().getViewer(), FileHistogramContentProvider.sharedInstance); //$NON-NLS-1$ //$NON-NLS-2$
        action3 = new SwitchContentProviderAction(
                "Sort samples per function", "icons/function_obj.gif", getSTViewer().getViewer(), FunctionHistogramContentProvider.sharedInstance); //$NON-NLS-1$ //$NON-NLS-2$
        action4 = new SwitchContentProviderAction(
                "Sort samples per line", "icons/line_obj.gif", getSTViewer().getViewer(), FlatHistogramContentProvider.sharedInstance); //$NON-NLS-1$ //$NON-NLS-2$
        switchSampleTime = new SwitchSampleTimeAction(this);
    }

    /*
     * I do not know where to put this static method. It is used by all ProfFields
     */
    public static Color getBackground(Object element) {
    	ColorRegistry colorRegistry = JFaceResources.getColorRegistry();

    	// FIXME: Not sure if color1-color4 are ever used...picked colors found in JFacesResources ColorRegistry
    	// not tied to any particular language (e.g. didn't choose CDT or Java colors)
    	// Color5 seems to work ok as bg in the one dark theme I tried (Nissl-Adwaita-dark-4) and as well
    	// in default light adwaita, but it is much simpler to just return null and let the table color default
    	// appropriately.
    	Color color1 = colorRegistry.get("org.eclipse.ui.editors.currentLineColor"); //$NON-NLS-1$
    	Color color2 = colorRegistry.get("org.eclipse.ui.workbench.INACTIVE_TAB_BG_START"); //$NON-NLS-1$
    	Color color3 = colorRegistry.get("org.eclipse.ui.workbench.ACTIVE_NOFOCUS_TAB_BG_END"); //$NON-NLS-1$
    	Color color4 = colorRegistry.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
    	//        Color color5 = colorRegistry.get("org.eclipse.ui.workbench.INACTIVE_TAB_BG_START"); //$NON-NLS-1$

    	if (element instanceof CGCategory) {
    		CGCategory cat = (CGCategory) element;
    		if (CGCategory.CHILDREN.equals(cat.category)) {
    			return color1 == null ? BLUE1 : color1;
    		} else {
    			return color2 == null ? GREEN1 : color2;
    		}
    	} else if (element instanceof CGArc) {
    		CGArc arc = (CGArc) element;
    		CGCategory cat = (CGCategory) arc.getParent();
    		if (CGCategory.CHILDREN.equals(cat.category)) {
    			return color3 == null ? BLUE2 : color3;
    		} else {
    			return color4 == null ? GREEN2 : color4;
    		}
    	}
    	return null; // default background
    	//        return color5 == null ? DEFAULT_BG : color5;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    protected AbstractSTViewer createAbstractSTViewer(Composite parent) {
        return new GmonViewer(parent);
    }

    /**
     * set the gprof view title
     *
     * @param decoder
     *            the gmon decoder
     * @param titleLabel
     *            the title label
     */
    public static void setHistTitle(GmonDecoder decoder, Label titleLabel) {
        String title = " gmon file: " //$NON-NLS-1$
                + decoder.getGmonFile() + "\n program file: " //$NON-NLS-1$
                + decoder.getProgram().getPath() + "\n" //$NON-NLS-1$
                + " timestamp: " + decoder.getGmonFileTimeStamp(); //$NON-NLS-1$
        HistogramDecoder histo = decoder.getHistogramDecoder();
        if (histo.hasValues()) {
            double prof_rate = histo.getProf_rate();
            String period = ""; //$NON-NLS-1$
            if (prof_rate != 0) {
                char tUnit = histo.getTimeDimension();
                switch (tUnit) {
                case 's':
                    prof_rate /= 1000000000;
                    break;
                case 'm':
                    prof_rate /= 1000000;
                    break;
                case 'u':
                    prof_rate /= 1000;
                    break;
                }
                period = ", each sample counts as " + SampleProfField.getValue(1, prof_rate); //$NON-NLS-1$
            }
            title += "\n " + histo.getBucketSize() //$NON-NLS-1$
                    + " bytes per bucket" + period; //$NON-NLS-1$
        }
        titleLabel.setText(title);
        titleLabel.getParent().layout(true);
    }

    /**
     * Display gmon results in the GProf View. NOTE: this method has to be called from within the UI thread.
     *
     * @param binaryPath
     * @param gmonPath
     * @param instanceName
     */
    public static GmonView displayGprofView(String binaryPath, String gmonPath, IProject project) {
        IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));
        if (binary == null) {
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Invalid binary file", //$NON-NLS-1$
                    binaryPath + " is not a valid binary file."); //$NON-NLS-1$
            return null;
        } else if (binary.getCPU().equals("ppc64")) //$NON-NLS-1$
            binary = new PPC64ElfBinaryObjectWrapper(binary.getBinaryParser(), binary.getPath(), binary.getType());

        GmonDecoder decoder = new GmonDecoder(binary, project);
        try {
            decoder.read(gmonPath);
        } catch (IOException e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
            Activator.getDefault().getLog().log(status);
        }
        return displayGprofView(decoder, gmonPath);
    }

    /**
     * Display gmon results in the GProf View. NOTE: this method has to be called from within the UI thread.
     *
     * @param decoder
     * @param secondary_id_usually_path_to_gmon_file
     */
    public static GmonView displayGprofView(GmonDecoder decoder, String secondary_id_usually_path_to_gmon_file) {
        GmonView gmonview = null;
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            if (secondary_id_usually_path_to_gmon_file != null) {
                secondary_id_usually_path_to_gmon_file = secondary_id_usually_path_to_gmon_file.replace('.', '_');
                secondary_id_usually_path_to_gmon_file = secondary_id_usually_path_to_gmon_file.replace(':', '_');
            }
            gmonview = (GmonView) page.showView(ID, secondary_id_usually_path_to_gmon_file,
                    IWorkbenchPage.VIEW_ACTIVATE);
            if (decoder.getHistogramDecoder().getProf_rate() == 0) {
                gmonview.switchSampleTime.setToolTipText("Unable to display time, because profiling rate is null"); //$NON-NLS-1$
                gmonview.switchSampleTime.setEnabled(false);
            }
            gmonview.setInput(decoder);
            GmonView.setHistTitle(decoder, gmonview.label);
            if (!decoder.getHistogramDecoder().hasValues()) {
                gmonview.action1.setChecked(true);
                gmonview.action2.setChecked(false);
                gmonview.action1.run();
            }
        } catch (CoreException e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
            Activator.getDefault().getLog().log(status);
        }
        return gmonview;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#createExportToCSVAction()
     */
    @Override
    protected IAction createExportToCSVAction() {
        IAction action = new STExportToCSVAction(this.getSTViewer()) {
            @Override
            public void run() {
                Object o = getSTViewer().getInput();
                if (o instanceof GmonDecoder) {
                    GmonDecoder gd = (GmonDecoder) o;
                    getExporter().setFilePath(gd.getGmonFile() + ".csv"); //$NON-NLS-1$
                }
                super.run();
            }

        };
        return action;
    }

}
