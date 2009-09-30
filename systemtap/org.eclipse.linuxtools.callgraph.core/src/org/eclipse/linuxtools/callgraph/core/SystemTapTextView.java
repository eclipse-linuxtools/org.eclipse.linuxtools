package org.eclipse.linuxtools.callgraph.core;

import java.util.Vector;

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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class SystemTapTextView extends SystemTapView {
	private static StyledText viewer;

	private static String text;
	private static StyleRange[] sr;

	private static Display display;
	private static int previousEnd;

	public static SystemTapParser parser;

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
		masterComposite = parent;
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

	@Override
	public IStatus initialize(Display targetDisplay, IProgressMonitor monitor) {
		previousEnd = 0;
		forceDisplay();
		return Status.OK_STATUS;
	}

	@Override
	public boolean setParser(SystemTapParser p) {
		parser = p;
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
	}

	/**
	 * Force the CallgraphView to initialize
	 */
	public static void forceDisplay() {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			window
					.getActivePage()
					.showView(
							"org.eclipse.linuxtools.callgraph.core.staptextview").setFocus(); //$NON-NLS-1$
		} catch (PartInitException e2) {
			e2.printStackTrace();
		}
	}

	protected class RunTimeJob extends UIJob {

		public RunTimeJob(String name) {
			super(name);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			return Status.CANCEL_STATUS;
		}
	}

	@Override
	public void updateMethod() {
		if (parser.getData() instanceof String) {
			String data = (String) parser.getData();
			if (data.length() > 0)
				prettyPrintln((String) parser.getData());
		}
	}

}
