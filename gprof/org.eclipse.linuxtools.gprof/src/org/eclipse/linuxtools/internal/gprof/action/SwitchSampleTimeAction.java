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
package org.eclipse.linuxtools.internal.gprof.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.internal.gprof.Activator;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.view.fields.SampleProfField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;


/**
 * This action changes the content provider of
 * the {@link org.eclipse.linuxtools.internal.gprof.view.GmonView}
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class SwitchSampleTimeAction extends Action {

	private final AbstractSTDataView view;
	
	/**
	 * Constructor
	 * @param name name of the action
	 * @param view the Gmon viewer
	 */
	public SwitchSampleTimeAction(AbstractSTDataView view) {
		super(Messages.SwitchSampleTimeAction_SWITCH_SAMPLE_TIME, SWT.TOGGLE);
		this.setImageDescriptor(Activator.getImageDescriptor("icons/datetime_obj.gif")); //$NON-NLS-1$
		this.setToolTipText(Messages.SwitchSampleTimeAction_SWITCH_SAMPLE_TIME);
		this.view = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		AbstractSTTreeViewer gmonViewer = (AbstractSTTreeViewer)view.getSTViewer();
		GmonDecoder decoder = (GmonDecoder) gmonViewer.getInput();
		if(decoder != null){
			int prof_rate = decoder.getHistogramDecoder().getProf_rate();

			if (prof_rate == 0) {
				MessageDialog.openError(view.getSite().getShell(),
						Messages.SwitchSampleTimeAction_GMON_PROF_RATE_IS_NULL,
				Messages.SwitchSampleTimeAction_GMON_PROF_RATE_IS_NULL_LONG_MSG);
				return;
			}

			TreeColumn tc = gmonViewer.getViewer().getTree().getColumn(1);
			SampleProfField spf = (SampleProfField) tc.getData();
			spf.toggle();
			tc.setText(spf.getColumnHeaderText());
			gmonViewer.getViewer().refresh();
		}
	}
	
	
}
