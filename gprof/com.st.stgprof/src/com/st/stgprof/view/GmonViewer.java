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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import com.st.dataviewers.abstractviewers.AbstractSTTreeViewer;
import com.st.dataviewers.abstractviewers.ISTDataViewersField;
import com.st.flexperf.binutils.link2source.STLink2SourceSupport;
import com.st.stgprof.Activator;
import com.st.stgprof.view.fields.CallsProfField;
import com.st.stgprof.view.fields.NameProfField;
import com.st.stgprof.view.fields.RatioProfField;
import com.st.stgprof.view.fields.SamplePerCallField;
import com.st.stgprof.view.fields.SampleProfField;
import com.st.stgprof.view.histogram.HistRoot;
import com.st.stgprof.view.histogram.TreeElement;

/**
 * TreeViewer 
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class GmonViewer extends AbstractSTTreeViewer {

	private ISTDataViewersField[] fields;

	/**
	 * Constructor
	 * @param parent
	 */
	public GmonViewer(Composite parent) {
		super(parent);
	}



	/* (non-Javadoc)
	 * @see com.st.dataviewers.abstractviewers.AbstractSTTreeViewer#createViewer(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected ColumnViewer createViewer(Composite parent, int style) {
		TreeViewer tv = (TreeViewer) super.createViewer(parent, style);
		tv.setAutoExpandLevel(2);
		return tv;
	}



	@Override
	protected ITreeContentProvider createContentProvider() {
		return FileHistogramContentProvider.sharedInstance;
	}

	@Override
	public ISTDataViewersField[] getAllFields() {
		if (fields == null) {
			fields = new ISTDataViewersField[] {
					new NameProfField(),
					new SampleProfField(this),
					new CallsProfField(),
					new SamplePerCallField(this),
					new RatioProfField()
			};
		}
		return fields;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		return Activator.getDefault().getDialogSettings();
	}


	/* (non-Javadoc)
	 * @see com.st.fp3.viewers.abstractview.AbstractSTProfViewer#handleOpenEvent(org.eclipse.jface.viewers.OpenEvent)
	 */
	@Override
	protected void handleOpenEvent(OpenEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		TreeElement element = (TreeElement) selection.getFirstElement();
		if (element != null){
			String s = element.getSourcePath();
			if (s == null || "??".equals(s)) {
				return; // nothing to do here.
			}
			else {
				int lineNumber             = element.getSourceLine();
				IBinaryObject exec = ((HistRoot)element.getRoot()).decoder.getProgram();
				STLink2SourceSupport.sharedInstance.openSourceFileAtLocation(exec, s, lineNumber);
			}
		}
	}

}
