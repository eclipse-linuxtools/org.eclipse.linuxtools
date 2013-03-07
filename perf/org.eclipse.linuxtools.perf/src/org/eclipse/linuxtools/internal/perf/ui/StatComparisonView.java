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
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;
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
 * Perf Statistics Comparison view
 */
public class StatComparisonView extends ViewPart {

	// color values constasts
	private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
	private static final Color GREEN = new Color(Display.getDefault(), 0, 100, 0);

	// event occurrence reg-ex
	private static String OCCURRENCE = "\\s*(\\-?+" //$NON-NLS-1$
			+ PMStatEntry.DECIMAL + ").*"; //$NON-NLS-1$

	private StyledText text;
	private static int SECONDARY_ID = 0;

	public StatComparisonView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		text = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);

		StatComparisonData statsDiff = PerfPlugin.getDefault()
				.getStatDiffData();
		if (statsDiff != null) {
			setStyledText(statsDiff.getResult());
			setContentDescription(statsDiff.getTitle());
		}
	}

	@Override
	public void setFocus() {
		return;
	}

	/**
	 * Set String input in text display. Adapted from
	 * org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView.
	 *
	 * @param input text to display
	 */
	private void setStyledText(String input) {
		text.setText(input);
		text.setAlignment(SWT.LEFT);
		List<StyleRange> styles = new ArrayList<StyleRange>();
		int ptr = 0;

		// set default TextConsole font (monospaced).
		text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		String[] lines = input.split("\n");

		for(String line : lines){
			if (Pattern.matches(OCCURRENCE, line)) {
				Matcher m = Pattern.compile(OCCURRENCE).matcher(line);
				if (m.matches() && m.group(1) != null) {
					try {
						float occurrence = StatComparisonData.toFloat(m
								.group(1).trim());
						if (occurrence > 0) {
							styles.add(new StyleRange(ptr, line.length(), RED,
									null));
						} else if (occurrence < 0) {
							styles.add(new StyleRange(ptr, line.length(),
									GREEN, null));
						}
					} catch (NumberFormatException e) {
						// set no StyleRange
					}
				}
			}
			// + 1 to skip over the '\n' at EOL that the tokenizer eats
			ptr += line.length() + 1;
		}

		text.setStyleRanges(styles.toArray(new StyleRange[0]));
	}

	/**
	 * Show new view with provided input.
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
							.showView(PerfPlugin.STAT_DIFF_VIEW_ID,
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
