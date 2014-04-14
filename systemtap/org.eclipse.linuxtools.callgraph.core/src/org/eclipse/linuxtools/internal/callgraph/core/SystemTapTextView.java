/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class SystemTapTextView extends SystemTapView {
	private StyledText viewer;

	private Display display;
	private int previousEnd;


	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.isDisposed()) {
			viewer.setFocus();
		}
	}

	private void createViewer(Composite parent) {
		viewer = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP);

		viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Font font = new Font(parent.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
		viewer.setFont(font);
		masterComposite = parent;
		display = masterComposite.getDisplay();
	}

	/**
	 * Print with colour codes. Colour codes accepted in the form of ~(R,G,B)~,
	 * and apply for the rest of the line or until another code is encountered
	 * @param text
	 */
	private void prettyPrintln(String text) {
		List<StyleRange> styles = new ArrayList<>();
		String[] txt = text.split("\\n"); //$NON-NLS-1$
		int lineOffset = 0;
		int inLineOffset;

		// txt[] contains text, with one entry for each new line
		for (String line: txt) {

			// Skip blank strings
			if (line.isEmpty()) {
				viewer.append(PluginConstants.NEW_LINE);
				continue;
			}

			// Search for colour codes, if none exist then continue
			String[] split_txt = line.split("~\\("); //$NON-NLS-1$
			if (split_txt.length == 1) {
				viewer.append(split_txt[0]);
				viewer.append(PluginConstants.NEW_LINE);
				continue;
			}

			inLineOffset = 0;
			for (String split: split_txt) {
				// Skip blank substrings
				if (split.isEmpty()) {
					continue;
				}

				// Split for the number codes
				String[] coloursAndText = split.split("\\)~"); //$NON-NLS-1$

				// If the string is properly formatted, colours should be length
				// 2
				// If it is not properly formatted, don't colour (just print)
				if (coloursAndText.length != 2) {
					for (String colourAndText: coloursAndText) {
						viewer.append(colourAndText);
						inLineOffset += colourAndText.length();
					}
					continue;
				}

				// The first element in the array should contain the colours
				String[] colours = coloursAndText[0].split(","); //$NON-NLS-1$
				if (colours.length < 3) {
					continue;
				}

				// The second element in the array should contain the text
				viewer.append(coloursAndText[1]);

				// Create a colour based on the 3 integers (if there are any
				// more integers, just ignore)
				int R = new Integer(colours[0].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
				int G = new Integer(colours[1].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
				int B = new Integer(colours[2].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$

				if (R > 255) R = 255;
				if (G > 255) G = 255;
				if (B > 255) B = 255;

				if (R < 0) R = 0;
				if (G < 0) G = 0;
				if (B < 0) B = 0;

				Color newColor = new Color(display, R, G, B);

				// Find the offset of the current line
				lineOffset = viewer.getOffsetAtLine(viewer.getLineCount() - 1);

				// Create a new style that lasts no further than the length of
				// the line
				StyleRange newStyle = new StyleRange(lineOffset + inLineOffset,
						coloursAndText[1].length(), newColor, null);
				styles.add(newStyle);

				inLineOffset += coloursAndText[1].length();
			}

			viewer.append(PluginConstants.NEW_LINE);
		}

		// Create a new style range
		StyleRange[] s = new StyleRange[styles.size()];
		styles.toArray(s);

		int cnt = viewer.getCharCount();

		// Using replaceStyleRanges with previousEnd, etc, effectively adds
		// the StyleRange to the existing set of Style Ranges (so we don't
		// waste time fudging with old style ranges that haven't changed)
		viewer.replaceStyleRanges(previousEnd, cnt - previousEnd, s);
		previousEnd = cnt;

		// Change focus and update
		viewer.setTopIndex(viewer.getLineCount() - 1);
		viewer.update();
	}

	/**
	 * Default print, just dumps text into the viewer.
	 * @param text
	 */
	public void println(String text) {
		if (viewer != null && !viewer.isDisposed()) {
			viewer.append(text);
			viewer.setTopIndex(viewer.getLineCount() - 1);
			viewer.update();
		}
	}

	public void clearAll() {
		if (viewer != null && !viewer.isDisposed()) {
			previousEnd = 0;
			viewer.setText(""); //$NON-NLS-1$
			viewer.update();
		}
	}

	/**
	 * Testing convenience method to see what was printed
	 *
	 * @return viewer text
	 */
	public String getText() {
		return viewer.getText();
	}


	@Override
	public IStatus initializeView(Display targetDisplay, IProgressMonitor monitor) {
		previousEnd = 0;
		viewer.setText(""); //$NON-NLS-1$
		viewer.update();
		return Status.OK_STATUS;
	}

	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);

		addKillButton();
		addFileMenu();
		addHelpMenu();
		ViewFactory.addView(this);
	}

	@Override
	public void updateMethod() {
		if (getParser().getData() instanceof String) {
			String data = (String) getParser().getData();
			if (data.length() > 0) {
				prettyPrintln((String) getParser().getData());
			}
		}
	}

	@Override
	public void setViewID() {
		viewID = "org.eclipse.linuxtools.callgraph.core.staptextview";		 //$NON-NLS-1$
	}

	@Override
	protected boolean createOpenAction() {
		return false;
	}

	@Override
	protected boolean createOpenDefaultAction() {
		return false;
	}
}
