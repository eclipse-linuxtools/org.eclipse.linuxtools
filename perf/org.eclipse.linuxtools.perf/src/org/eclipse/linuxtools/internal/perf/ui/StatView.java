/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * A ViewPart to display the output from perf's stat command.
 */
public class StatView extends ViewPart {

	private StyledText text;

	public StatView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		text = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);

		StatData data = PerfPlugin.getDefault().getStatData();
		if (data != null) {
			setStyledText(data.getPerfData());
			setContentDescription(data.getTitle());
		}
	}

	@Override
	public void setFocus() {
		return;
	}

	private void setStyledText (String input) {
		text.setText(input);

		// the default TextConsole font (we want monospaced)
		text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
	}
	/**
	 * Update to most recent statistics data.
	 */
	public void updateData(){
		StatData data = PerfPlugin.getDefault().getStatData();
		if (data != null) {
			setStyledText(data.getPerfData());
			setContentDescription(data.getTitle());
		}
	}

	/**
	 * Refresh perf statistics view.
	 */
	public static void refreshView () {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					// A new view is created every time
					StatView view = (StatView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(PerfPlugin.STAT_VIEW_ID);
					view.updateData();
				} catch (PartInitException e) {
					IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, e.getMessage(), e);
					PerfPlugin.getDefault().getLog().log(status);
				}
			}
		});
	}

}
