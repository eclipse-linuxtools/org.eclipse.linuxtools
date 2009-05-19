/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.stgprof.view;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.st.dataviewers.abstractview.AbstractSTDataView;
import com.st.dataviewers.abstractviewers.AbstractSTViewer;
import com.st.dataviewers.actions.STExportToCSVAction;
import com.st.flexperf.binutils.utils.STSymbolManager;
import com.st.stgprof.Activator;
import com.st.stgprof.action.SwitchContentProviderAction;
import com.st.stgprof.action.SwitchSampleTimeAction;
import com.st.stgprof.parser.GmonDecoder;
import com.st.stgprof.parser.HistogramDecoder;
import com.st.stgprof.utils.STGprofProgramChecker;
import com.st.stgprof.view.fields.SampleProfField;
import com.st.stgprof.view.histogram.CGArc;
import com.st.stgprof.view.histogram.CGCategory;

/**
 * The view where gmon file is displayed
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GmonView extends AbstractSTDataView {

	/** WHITE color */
	public static final Color WHITE = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE);
	/** GREEN1 color : for children category */
	public static final Color GREEN1 = new Color(PlatformUI.getWorkbench().getDisplay(), 207,255,207);
	/** GREEN2 color : for children */
	public static final Color GREEN2 = new Color(PlatformUI.getWorkbench().getDisplay(), 175,255,175);
	/** BLUE1 color : for parent category */
	public static final Color BLUE1 = new Color(PlatformUI.getWorkbench().getDisplay(), 207,207,255);
	/** BLUE2 color : for parents */
	public static final Color BLUE2 = new Color(PlatformUI.getWorkbench().getDisplay(), 175,175,255);

	public static final int CALL_GRAPH_MODE = 0;
	public static final int SAMPLE_MODE = 1;

	private Label label;
	private Action action1;
	private Action action2;
	private Action action3;
	private Action action4;
	private Action switchSampleTime;


	protected void createTitle(Composite parent) {
		label = new Label(parent, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1);
		label.setLayoutData(data);
	}

	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.AbstractSTProfViewer#contributeToToolbar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void contributeToToolbar(IToolBarManager manager) {
		super.contributeToToolbar(manager);
		manager.add(new Separator());		
		manager.add(action2);
		action2.setChecked(true);
		manager.add(action3);
		manager.add(action4);
		manager.add(action1);
		manager.add(new Separator());
		manager.add(switchSampleTime); 
		manager.add(new Separator());
	}


	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.AbstractSTProfViewer#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();
		action1 = new SwitchContentProviderAction("Display function call graph", "icons/ch_callees.png", getSTViewer().getViewer(), CallGraphContentProvider.sharedInstance);
		action2 = new SwitchContentProviderAction("Sort samples per file", "icons/c_file_obj.gif", getSTViewer().getViewer(), FileHistogramContentProvider.sharedInstance);
		action3 = new SwitchContentProviderAction("Sort samples per function", "icons/function_obj.gif", getSTViewer().getViewer(), FunctionHistogramContentProvider.sharedInstance);
		action4 = new SwitchContentProviderAction("Sort samples per line", "icons/line_obj.gif", getSTViewer().getViewer(), FlatHistogramContentProvider.sharedInstance);
		switchSampleTime = new SwitchSampleTimeAction(this);
	}



	/*
	 * I do not know where to put this static method.
	 * It is used by all ProfFields
	 */
	public static Color getBackground(Object element) {
		if (element instanceof CGCategory) {
			CGCategory cat = (CGCategory) element;
			if (CGCategory.CHILDREN.equals(cat.category)) {
				return GmonView.BLUE1;
			} else {
				return GmonView.GREEN1;
			}
		} else if (element instanceof CGArc) {
			CGArc arc = (CGArc) element;
			CGCategory cat = (CGCategory) arc.getParent();
			if (CGCategory.CHILDREN.equals(cat.category)) {
				return GmonView.BLUE2;
			} else {
				return GmonView.GREEN2;
			}
		}
		return GmonView.WHITE;
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
	 * @param decoder the gmon decoder
	 * @param titleLabel the title label
	 */
	public static void setHistTitle(GmonDecoder decoder, Label titleLabel) {
		String title = " gmon file: "
			+ decoder.getGmonFile()
			+ "\n program file: "
			+ decoder.getProgram().getPath();
		HistogramDecoder histo = decoder.getHistogramDecoder();
		if (histo.hasValues()) {
			double prof_rate = histo.getProf_rate();
			String period = "";
			if (prof_rate != 0){
				char tUnit = histo.getTimeDimension();
				switch (tUnit) {
				case 's': prof_rate /= 1000000000; break;
				case 'm': prof_rate /= 1000000; break;
				case 'u': prof_rate /= 1000; break;
				}
				period = ", each sample counts as " + SampleProfField.getValue(1, prof_rate);
			}
			title += "\n " +histo.getBucketSize()
			+ " bytes per bucket" + period;
		}
		titleLabel.setText(title);
		titleLabel.getParent().layout(true);
	}

	/**
	 * Display gmon results in the GProf View.
	 * NOTE: this method has to be called from within the UI thread.
	 * @param binaryPath
	 * @param gmonPath
	 * @param instanceName
	 */
	public static GmonView displayGprofView(String binaryPath, String gmonPath, String instanceName){
		IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));
		if (binary == null) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Invalid binary file",
					binaryPath + " is not a valid binary file.");
			return null;
		}

		GmonDecoder decoder = new GmonDecoder(binary);
		try {
			decoder.read(gmonPath);
		} catch(Exception e) {
			Status status = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					IStatus.ERROR,
					e.getMessage(),
					e
			);
			Activator.getDefault().getLog().log(status);
		}
		return displayGprofView(decoder, gmonPath, instanceName);
	}

	/**
	 * Display gmon results in the GProf View.
	 * NOTE: this method has to be called from within the UI thread.
	 * @param decoder
	 * @param gmonPath
	 * @param instanceName
	 */
	public static GmonView displayGprofView(GmonDecoder decoder, String gmonPath, String instanceName){
		GmonView gmonview = null;
		try{
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			if (instanceName != null)
				gmonview = (GmonView) page.showView("com.st.stgprof.view",instanceName, IWorkbenchPage.VIEW_ACTIVATE);
			else gmonview = (GmonView) page.showView("com.st.stgprof.view");
			if (decoder.getHistogramDecoder().getProf_rate() == 0){
				gmonview.switchSampleTime.setToolTipText("Unable to display time, because profiling rate is null");
				gmonview.switchSampleTime.setEnabled(false);
			}
			gmonview.setInput(decoder);
			GmonView.setHistTitle(decoder, gmonview.label);
			if (!decoder.getHistogramDecoder().hasValues()) {
				gmonview.action1.setChecked(true);
				gmonview.action2.setChecked(false);
				gmonview.action1.run();
			}
			if (decoder.isDCache() || decoder.isICache()) {
				TreeViewer tv = (TreeViewer) gmonview.getSTViewer().getViewer();
				TreeColumn tc = tv.getTree().getColumn(1);
				SampleProfField spf = (SampleProfField) tc.getData();
				tc.setText(spf.getColumnHeaderText());
				tc.setToolTipText(spf.getColumnHeaderTooltip());
				tv.refresh();
			}
		} catch(Exception e) {
			Status status = new Status(
					Status.ERROR,
					Activator.PLUGIN_ID,
					IStatus.ERROR,
					e.getMessage(),
					e
			);
			Activator.getDefault().getLog().log(status);
		}
		return gmonview;
	}

	/* (non-Javadoc)
	 * @see com.st.dataviewers.abstractview.AbstractSTDataView#createExportToCSVAction()
	 */
	@Override
	protected IAction createExportToCSVAction() {
		IAction action = new STExportToCSVAction(this.getSTViewer()) {
			public void run() {
				Object o = getSTViewer().getInput();
				if (o instanceof GmonDecoder) {
					GmonDecoder gd = (GmonDecoder) o;
					getExporter().setFilePath(gd.getGmonFile() + ".csv");
				}
				super.run();
			}

		};
		return action;
	}

}
