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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.SourceDisassemblyData;
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
 * A ViewPart to display the output from perf's source disassembly.
 */
public class SourceDisassemblyView extends ViewPart {

	private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
	private static final Color ORANGE = new Color(Display.getDefault(), 150, 100, 0);
	private static final Color GREEN = new Color(Display.getDefault(), 0, 100, 0);
	private static String ASM = "\\s+([0-9]+\\.[0-9]+ )?:\\s+[0-9a-f]+:\\s+[0-9a-z]+\\s+.*"; //$NON-NLS-1$
	private static String CODE = "\\s+:\\s+.*"; //$NON-NLS-1$
	private StyledText text;
	private static int SECONDARY_ID = 0;

	public SourceDisassemblyView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		text = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);

		SourceDisassemblyData data = PerfPlugin.getDefault().getSourceDisassemblyData();
		if (data != null) {
			setStyledText(data.getSourceDisassemblyText());
			setContentDescription(data.getTitle());
		}
	}

	@Override
	public void setFocus() {
		return;
	}

	public StyledText getStyledText () {
		return text;
	}

	public void setStyledText (String text) {
		List<StyleRange> styles = new ArrayList<StyleRange> ();
		int ptr = 0;

		getStyledText().setText(text);

		StringTokenizer tok = new StringTokenizer(text, "\n"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String line = tok.nextToken();
			if (Pattern.matches(ASM, line)) {
				Matcher m = Pattern.compile(ASM).matcher(line);
				if (m.matches() && m.group(1) != null) {
					try {
						float percent = Float.parseFloat(m.group(1).trim());
						if (percent >= 20) {
							styles.add(new StyleRange(ptr, line.length(), RED, null));
						} else if  (percent >= 5) {
							styles.add(new StyleRange(ptr, line.length(), ORANGE, null));
						}
					} catch (NumberFormatException e) {
						// set no StyleRange
					}
				}
			} else if (Pattern.matches(CODE, line)) {
				styles.add(new StyleRange(ptr, line.length(), GREEN, null));
			}

			// + 1 to skip over the '\n' at EOL that the tokenizer eats
			ptr += line.length() + 1;
		}
		getStyledText().setStyleRanges(styles.toArray(new StyleRange [0]));
	}

	public static void RefreshView () {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					// A new view is created every time
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(PerfPlugin.SOURCE_DISASSEMBLY_VIEW_ID,
									Integer.toString(SECONDARY_ID++),
									IWorkbenchPage.VIEW_CREATE);
				} catch (PartInitException e) {
					IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, e.getMessage(), e);
					PerfPlugin.getDefault().getLog().log(status);
				}
			}
		});
	}

}
