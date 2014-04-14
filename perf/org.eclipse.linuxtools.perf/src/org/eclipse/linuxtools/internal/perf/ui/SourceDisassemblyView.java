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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.FindReplaceAction;

/**
 * A ViewPart to display the output from perf's source disassembly.
 */
public class SourceDisassemblyView extends ViewPart implements IFindReplaceTarget{

	private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
	private static final Color ORANGE = new Color(Display.getDefault(), 150, 100, 0);
	private static final Color GREEN = new Color(Display.getDefault(), 0, 100, 0);
	private static String ASM = "\\s+([0-9]+\\.[0-9]+ )?:\\s+[0-9a-f]+:\\s+[0-9a-z]+\\s+.*"; //$NON-NLS-1$
	private static String CODE = "\\s+:\\s+.*"; //$NON-NLS-1$
	private static String WORD_BOUNDARY = "\\b"; //$NON-NLS-1$'
	private static int SECONDARY_ID = 0;
	private StyledText text;
	public SourceDisassemblyView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		text = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);

		IPerfData data = PerfPlugin.getDefault().getSourceDisassemblyData();
		if (data != null) {
			setStyledText(data.getPerfData());
			setContentDescription(data.getTitle());
			setupFindDialog();
		}
	}

	@Override
	public void setFocus() {
		return;
	}

	/**
	 * Set styled text field (only used for testing).
	 *
	 * @param txt StyledText to set.
	 */
	protected void setStyledText(StyledText txt) {
		text = txt;
	}

	/**
	 * Get the text content of this view.
	 *
	 * @return String content of this view
	 */
	public String getContent() {
		return (text == null) ? "" : text.getText(); //$NON-NLS-1$
	}

	/**
	 * Set styled text field based on the specified string, which is parsed in
	 * order to set appropriate styles to be used for rendering the widget
	 * content.
	 *
	 * @param input text content of widget.
	 */
	private void setStyledText (String input) {
		List<StyleRange> styles = new ArrayList<> ();
		int ptr = 0;

		text.setText(input);

		StringTokenizer tok = new StringTokenizer(input, "\n"); //$NON-NLS-1$
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
		text.setStyleRanges(styles.toArray(new StyleRange [0]));
	}

	public static void refreshView () {
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

	/**
	 * Create find dialog and set is as a toolbar action.
	 */
	private void setupFindDialog() {
		FindReplaceAction findAction = new FindReplaceAction(
				Platform.getResourceBundle(PerfPlugin.getDefault().getBundle()),
				null, text.getShell(), this);
		findAction.setImageDescriptor(PerfPlugin
				.getImageDescriptor("icons/search.gif"));//$NON-NLS-1$
		findAction.setToolTipText(PerfPlugin.STRINGS_SearchSourceDisassembly);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(findAction);
		bars.setGlobalActionHandler(ActionFactory.FIND.getId(), findAction);
	}

	@Override
	public boolean canPerformFind() {
		return text != null && !text.getText().isEmpty();
	}

	@Override
	public int findAndSelect(int widgetOffset, String findString,
			boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		int matchIndex = -1;
		String searchString = text.getText();
		String findRegex = findString;

		// offset is -1 when text boundaries are reached during a wrapped search
		if (widgetOffset < 0) {
			widgetOffset = searchForward ? 0 : searchString.length();
		}

		if (wholeWord) {
			findRegex = WORD_BOUNDARY + findRegex + WORD_BOUNDARY;
		}

		int caseFlag = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		Pattern pattern = Pattern.compile(findRegex, caseFlag);
		Matcher matcher = pattern.matcher(searchString);

		if (searchForward) {
			matchIndex = matcher.find(widgetOffset) ? matcher.start() : -1;
		} else {
			// backward search from 0 to offset (exclusive)
			matcher.region(0, widgetOffset);

			// get start index of last match
			while (matcher.find()) {
				matchIndex = matcher.start();
			}
		}

		// only select when a match has been found
		if (matchIndex != -1) {
			text.setSelection(matchIndex, matchIndex + findString.length());
		}
		return matchIndex;
	}

	@Override
	public Point getSelection() {
		Point selection = text.getSelection();
		// selection point consists of starting point x and lenght y - x.
		return new Point(selection.x, selection.y - selection.x);
	}

	@Override
	public String getSelectionText() {
		return text.getSelectionText();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void replaceSelection(String text) {
	}

}
