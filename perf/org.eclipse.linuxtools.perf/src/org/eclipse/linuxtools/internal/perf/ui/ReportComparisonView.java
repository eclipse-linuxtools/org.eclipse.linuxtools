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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.perf.ReportComparisonData;
import org.eclipse.linuxtools.internal.perf.handlers.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A view part to display perf comparison reports.
 */
public class ReportComparisonView extends Viewer {

	// Color values constants
	private static final Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
	private static final Color GREEN = new Color(Display.getDefault(), 0, 50, 0);
	private static final Color LIGHT_GREEN = new Color(Display.getDefault(), 0, 105, 0);
	private static final Color ORANGE = new Color(Display.getDefault(), 150, 100, 0);

	// Regex for a generic entry in a perf comparison report.
	private static final String DIFF_ENTRY = "\\s+(\\d+(\\.\\d+)?)\\%\\s+([\\+\\-]?\\d+(\\.\\d+)?)\\%.*"; //$NON-NLS-1$

	private Composite fComposite;
	private ICompareInput fInput;

	// Comparison result.
	private StyledText result;
	private Label reverseLabel;
	private boolean reverse;

	public ReportComparisonView (Composite parent, CompareConfiguration config) {
		fComposite = new Composite (parent, SWT.NONE);
		fComposite.setLayout(new GridLayout(2, false));
		fComposite.setData(CompareUI.COMPARE_VIEWER_TITLE, Messages.ReportComparisonView_label);

		reverseLabel = new Label(fComposite, SWT.NONE);
		reverseLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		reverseLabel.setText(Messages.StatComparisonView_reversedLabel);
		reverseLabel.setVisible(false);

		final Button reverse = new Button(fComposite, SWT.TOGGLE);
		reverse.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED));
		reverse.setToolTipText(Messages.StatComparisonView_reverseToolTip);
		reverse.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		reverse.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleReverse();
				setInput(fInput);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		result = new StyledText(fComposite, SWT.V_SCROLL | SWT.H_SCROLL);
		result.setAlwaysShowScrollBars(false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		result.setLayoutData(gd);
		result.setEditable(false);
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

		List<StyleRange> styles = new ArrayList<>();
		int ptr = 0;
		String[] lines = input.split("\n"); //$NON-NLS-1$

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
	public Control getControl() {
		return fComposite;
	}

	@Override
	public Object getInput() {
		return fInput;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ICompareInput) {
			fInput = (ICompareInput) input;

			if (fInput.getAncestor() != null ||
					(fInput.getKind() & Differencer.DIRECTION_MASK) != 0) {
				setStyledText(Messages.CompUnsupported);
			} else {
				// get corresponding files
				IPath oldDatum;
				IPath newDatum;
				IProject proj = null;

				if (fInput.getLeft() instanceof ResourceNode) {
					ResourceNode left = (ResourceNode) fInput.getLeft();
					IResource oldData = left.getResource();
					oldDatum = oldData.getLocation();
					proj = oldData.getProject();
				} else {
					IEncodedStreamContentAccessor lStream = (IEncodedStreamContentAccessor) fInput.getLeft();
					oldDatum = generateTempFile(lStream);
				}

				if (fInput.getRight() instanceof ResourceNode) {
					ResourceNode right = (ResourceNode) fInput.getRight();
					IResource newData = right.getResource();
					newDatum = newData.getLocation();
					proj = newData.getProject();
				} else {
					IEncodedStreamContentAccessor rStream = (IEncodedStreamContentAccessor) fInput.getRight();
					newDatum = generateTempFile(rStream);
				}

				String title = MessageFormat.format(Messages.ContentDescription_0,
						new Object[] { oldDatum.toFile().getName(), newDatum.toFile().getName() });

				// create comparison data and run comparison.
				ReportComparisonData diffData;
				if (reverse) {
					diffData = new ReportComparisonData(title, oldDatum, newDatum, proj);
				} else {
					diffData = new ReportComparisonData(title, newDatum, oldDatum, proj);
				}
				diffData.parse();

				setStyledText(diffData.getPerfData());
			}
		}

		fComposite.layout();
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
	}

	private IPath generateTempFile(IEncodedStreamContentAccessor stream) {
		try {
			Path tmpFile = Files.createTempFile("perf-report-", ".data"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpFile.toFile().delete();
			Files.copy(stream.getContents(), tmpFile);
			return new org.eclipse.core.runtime.Path(tmpFile.toString());
		} catch (IOException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}

	private void toggleReverse () {
		if (reverse) {
			reverse = false;
			reverseLabel.setVisible(false);
		} else {
			reverse = true;
			reverseLabel.setVisible(true);
		}
	}
}
