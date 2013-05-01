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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * A view part to display perf comparison reports.
 */
public class ReportComparisonView extends ViewPart {

	// Color values constants
	private static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
	private static final Color GREEN = new Color(Display.getDefault(), 0, 50, 0);
	private static final Color LIGHT_GREEN = new Color(Display.getDefault(), 0, 105, 0);
	private static final Color ORANGE = new Color(Display.getDefault(), 150, 100, 0);

	// Regex for a generic entry in a perf comparison report.
	private static final String DIFF_ENTRY = "\\s+(\\d+(\\.\\d+)?)\\%\\s+([\\+\\-]?\\d+(\\.\\d+)?)\\%.*";

	// Secondary view id.
	private static int SECONDARY_ID = 0;

	// Comparison result.
	private StyledText result;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		result = new StyledText(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		result.setEditable(false);

		IPerfData data = PerfPlugin.getDefault().getReportDiffData();
		if (data != null) {
			//setStyledText(data.getPerfData());
			setStyledText(data.getPerfData());
			setContentDescription(data.getTitle());
		}
	}

	/**
	 * Set properties for StlyedText widget.
	 * @param input String StyledText content.
	 */
	private void setStyledText(String input) {
		result.setText(input);
		result.setJustify(true);
		result.setAlignment(SWT.LEFT);

		result.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		List<StyleRange> styles = new ArrayList<StyleRange>();
		int ptr = 0;
		String[] lines = input.split("\n");

		for(String line : lines){
			if (Pattern.matches(DIFF_ENTRY, line)) {
				Matcher m = Pattern.compile(DIFF_ENTRY).matcher(line);
				if (m.matches() && m.group(1) != null && m.group(3) != null) {
					try {
						float baseline = Float.parseFloat(m.group(1).trim());
						float delta = Float.parseFloat(m.group(3).trim());
						if (baseline > 1 && Math.abs(delta) > 1) {
							StyleRange curStyleRange =  new StyleRange(ptr, line.length(), BLACK, null);
							if (delta < 0 ) {
								curStyleRange = delta < -5 ? new StyleRange(ptr, line.length(), LIGHT_GREEN, null) :
									new StyleRange(ptr, line.length(), GREEN, null);
							} else {
								curStyleRange = delta < 5 ? new StyleRange(ptr, line .length(), ORANGE, null) :
									new StyleRange(ptr, line.length(), RED, null);
							}
							styles.add(curStyleRange);
						}
					} catch (NumberFormatException e) {
						// set no StyleRange
					}
				}
			}
			// + 1 to skip over the '\n' at EOL that the tokenizer eats
			ptr += line.length() + 1;
		}

		result.setStyleRanges(styles.toArray(new StyleRange[0]));
	}

	@Override
	public void setFocus() {
		return;

	}

	/**
	 * Refresh this view.
	 */
	public static void refreshView() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(PerfPlugin.REPORT_DIFF_VIEW_ID,
									Integer.toString(SECONDARY_ID++),
									IWorkbenchPage.VIEW_CREATE);
				} catch (PartInitException e) {
					IStatus status = new Status(IStatus.ERROR,
							PerfPlugin.PLUGIN_ID, e.getMessage(), e);
					PerfPlugin.getDefault().getLog().log(status);
				}
			}
		});
	}
}
