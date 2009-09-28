package org.eclipse.linuxtools.callgraph.core;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;

public abstract class SystemTapView extends ViewPart {
	public static Composite masterComposite;
	private static SystemTapView stapview;
	private static boolean isInitialized;

	private static StyledText viewer;

	private static String text;
	private static StyleRange[] sr;

	private static Display display;
	private static int previousEnd;

	/**
	 * The constructor.
	 * 
	 * @return
	 */
	public SystemTapView() {
		isInitialized = true;
		previousEnd = 0;
	}

	public abstract IStatus initialize(Display targetDisplay,
			IProgressMonitor monitor);

	public static SystemTapView getSingleInstance() {
		if (isInitialized) {
			return stapview;
		}
		return null;
	}

	/**
	 * @param doMaximize
	 *            : true && view minimized will maximize the view, otherwise it
	 *            will just 'refresh'
	 */
	public static void maximizeOrRefresh(boolean doMaximize) {
		IWorkbenchPage page = SystemTapView.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (doMaximize
				&& page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(SystemTapView.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		} else {
			SystemTapView.layout();
		}
	}

	public static void layout() {
		masterComposite.layout();
	}

	/**
	 * If view is not maximized it will be maximized
	 */
	public static void maximizeIfUnmaximized() {
		IWorkbenchPage page = SystemTapView.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(SystemTapView.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		}

	}

	protected void setView(SystemTapView view) {
		stapview = view;
	}

	public abstract boolean setParser(SystemTapParser parser);

	protected static void cleanViewer() {
		if (viewer != null && !viewer.isDisposed()) {
			text = viewer.getText();
			sr = viewer.getStyleRanges();
			viewer.dispose();
		}
		previousEnd = 0;

	}

	protected static void restoreViewerContents() {
		if (text != null) {
			viewer.setText(text);
			viewer.setStyleRanges(sr);
		}
		previousEnd = 0;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (viewer != null && !viewer.isDisposed())
			viewer.setFocus();
	}

	public static void createViewer(Composite parent) {
		viewer = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP);

		viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Font font = new Font(parent.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
		viewer.setFont(font);
		display = masterComposite.getDisplay();
	}

	public void prettyPrintln(String text) {
		Vector<StyleRange> styles = new Vector<StyleRange>();
		String[] txt = text.split("\\n"); //$NON-NLS-1$
		int lineOffset = 0;
		int inLineOffset;

		// txt[] contains text, with one entry for each new line
		for (int i = 0; i < txt.length; i++) {

			// Skip blank strings
			if (txt[i].length() == 0) {
				viewer.append(PluginConstants.NEW_LINE);
				continue;
			}

			// Search for colour codes, if none exist then continue
			String[] split_txt = txt[i].split("~\\("); //$NON-NLS-1$
			if (split_txt.length == 1) {
				viewer.append(split_txt[0]);
				viewer.append(PluginConstants.NEW_LINE);
				continue;
			}

			inLineOffset = 0;
			for (int k = 0; k < split_txt.length; k++) {
				// Skip blank substrings
				if (split_txt[k].length() == 0)
					continue;

				// Split for the number codes
				String[] coloursAndText = split_txt[k].split("\\)~"); //$NON-NLS-1$

				// If the string is properly formatted, colours should be length
				// 2
				// If it is not properly formatted, don't colour (just print)
				if (coloursAndText.length != 2) {
					for (int j = 0; j < coloursAndText.length; j++) {
						viewer.append(coloursAndText[j]);
						inLineOffset += coloursAndText[j].length();
					}
					continue;
				}

				// The first element in the array should contain the colours
				String[] colours = coloursAndText[0].split(","); //$NON-NLS-1$
				if (colours.length < 3)
					continue;

				// The second element in the array should contain the text
				viewer.append(coloursAndText[1]);

				// Create a colour based on the 3 integers (if there are any
				// more integers, just ignore)
				int R = new Integer(colours[0].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
				int G = new Integer(colours[1].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
				int B = new Integer(colours[2].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$

				if (R > 255)
					R = 255;
				if (G > 255)
					G = 255;
				if (B > 255)
					B = 255;

				if (R < 0)
					R = 0;
				if (G < 0)
					G = 0;
				if (B < 0)
					B = 0;

				Color newColor = new Color(display, R, G, B);

				// Find the offset of the current line
				lineOffset = viewer.getOffsetAtLine(viewer.getLineCount() - 1);

				// Create a new style that lasts no further than the length of
				// the line
				StyleRange newStyle = new StyleRange(lineOffset + inLineOffset,
						coloursAndText[1].length(), newColor, null);
				styles.addElement(newStyle);

				inLineOffset += coloursAndText[1].length();
			}

			viewer.append(PluginConstants.NEW_LINE);
		}

		// Create a new style range
		StyleRange[] s = new StyleRange[styles.size()];
		styles.copyInto(s);

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

	public static void disposeView() {
		if (viewer != null && !viewer.isDisposed()) {
			String tmp = viewer.getText();
			StyleRange[] tempRange = viewer.getStyleRanges();
			viewer.dispose();
			createViewer(masterComposite);
			viewer.setText(tmp);
			viewer.setStyleRanges(tempRange);
		}
	}
}
