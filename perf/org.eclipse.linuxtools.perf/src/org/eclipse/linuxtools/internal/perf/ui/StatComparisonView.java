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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.handlers.PerfStatDiffMenuAction;
import org.eclipse.linuxtools.internal.perf.handlers.PerfStatDiffMenuAction.Type;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
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
	private IPerfData diffData;
	private String timestamp;
	private static int SECONDARY_ID = 0;

	public StatComparisonView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridLayout(1, true));

		text = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		timestamp = getTimestamp();

		IPerfData statsDiff = PerfPlugin.getDefault()
				.getStatDiffData();
		if (statsDiff != null) {
			diffData = statsDiff;
			updateData(statsDiff, true);
		}

		fillToolbarActions();
	}

	@Override
	public void setFocus() {
		return;
	}

	/**
	 * Get perf data associated with the current view.
	 * @return IPerfData data associated with this view.
	 */
	public IPerfData getDiffData(){
		return diffData;
	}

	/**
	 * Set String input in text display. Adapted from
	 * org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView.
	 *
	 * @param input text to display
	 */
	private void setStyledText(String input) {
		setBasicStyledText(input);
		List<StyleRange> styles = new ArrayList<StyleRange>();
		int ptr = 0;

		String[] lines = input.split("\n"); //$NON-NLS-1$

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

	private void setBasicStyledText(String input){
		text.setText(input);
		text.setAlignment(SWT.LEFT);
		// set default TextConsole font (monospaced).
		text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
	}

	/**
	 * Create toolbar actions associated with this view.
	 */
	private void fillToolbarActions() {
		// create [Old] [New] [Diff] actions.
		IActionBars bars = getViewSite().getActionBars();
		for (Type type : PerfStatDiffMenuAction.Type.values()) {
			bars.getToolBarManager().add(
					new PerfStatDiffMenuAction(type, getViewSite().getSecondaryId()));
		}
	}

	/**
	 * Update contents of current view, replacing the containing data and text styling.
	 *
	 * @param data IPerfData data replacement.
	 * @param style boolean true if styling is to be applied, false otherwise.
	 */
	public void updateData(IPerfData data, boolean style){
		if(data != null){
			if (style) {
				setStyledText(data.getPerfData());
			} else {
				setBasicStyledText(data.getPerfData());
			}
			setContentDescription(data.getTitle() + timestamp);
		}
	}

	/**
	 * Get current timestamp.
	 * @return String current timestamp.
	 */
	public String getTimestamp(){
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		return " [" + timestamp.toString() + "]";  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Utility method to get an instance of a {@link StatComparisonView} by
	 * providing the secondary identifier.
	 *
	 * @param sID String secondary identifier.
	 * @return IViewPart {@link StatComparisonView} associated with the
	 *         specified secondary identifier.
	 */
	public static IViewPart getView(final String sID) {
		final AtomicReference<IViewPart> viewRef = new AtomicReference<IViewPart>();

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IViewPart view = PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(PerfPlugin.STAT_DIFF_VIEW_ID,
									sID,
									IWorkbenchPage.VIEW_CREATE);
					viewRef.set(view);
				} catch (PartInitException e) {
					IStatus status = new Status(IStatus.ERROR,
							PerfPlugin.PLUGIN_ID, e.getMessage(), e);
					PerfPlugin.getDefault().getLog().log(status);
				}
			}
		});
		return viewRef.get();
	}

	/**
	 * Create new view.
	 */
	public static void refreshView() {
		getView(Integer.toString(SECONDARY_ID++));
	}

	@Override
	public void dispose() {
		super.dispose();
		// remove file contents from cache
		if (diffData instanceof StatComparisonData) {
			((StatComparisonData) diffData).clearCachedData();
		}
	}
}
