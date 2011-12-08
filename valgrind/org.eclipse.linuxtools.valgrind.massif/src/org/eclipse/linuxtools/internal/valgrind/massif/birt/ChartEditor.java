/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.birt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.Chart;

public class ChartEditor extends EditorPart {
	protected Chart control;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof ChartEditorInput) {
			setInput(input);
			setSite(site);
			setPartName(NLS.bind(Messages.getString("ChartEditor.Heap_Chart"), input.getName())); //$NON-NLS-1$
		}
		else {
			throw new PartInitException(NLS.bind(Messages.getString("ChartEditor.Editor_input_must_be"), ChartEditorInput.class.getName())); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		ChartEditorInput input = (ChartEditorInput) getEditorInput();
		control = new Chart(parent, SWT.FILL);
	}

	public Chart getControl() {
		return control;
	}
	
	@Override
	public void setFocus() {
		if (control != null) {
			control.setFocus();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		control.dispose();
	}

}
