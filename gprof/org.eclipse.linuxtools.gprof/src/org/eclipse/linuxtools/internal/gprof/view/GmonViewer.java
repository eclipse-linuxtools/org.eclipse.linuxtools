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
package org.eclipse.linuxtools.internal.gprof.view;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.binutils.link2source.STLink2SourceSupport;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.internal.gprof.Activator;
import org.eclipse.linuxtools.internal.gprof.view.fields.CallsProfField;
import org.eclipse.linuxtools.internal.gprof.view.fields.NameProfField;
import org.eclipse.linuxtools.internal.gprof.view.fields.RatioProfField;
import org.eclipse.linuxtools.internal.gprof.view.fields.SamplePerCallField;
import org.eclipse.linuxtools.internal.gprof.view.fields.SampleProfField;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;
import org.eclipse.swt.widgets.Composite;

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

    @Override
    protected TreeViewer createViewer(Composite parent, int style) {
        TreeViewer tv = super.createViewer(parent, style);
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

    @Override
    protected void handleOpenEvent(OpenEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        TreeElement element = (TreeElement) selection.getFirstElement();
        if (element != null){
            String s = element.getSourcePath();
            if (s == null || "??".equals(s)) { //$NON-NLS-1$
                return; // nothing to do here.
            } else {
                int lineNumber = element.getSourceLine();
                IBinaryObject exec = ((HistRoot)element.getRoot()).decoder.getProgram();
                STLink2SourceSupport.openSourceFileAtLocation(exec, s, lineNumber);
            }
        }
    }

}
