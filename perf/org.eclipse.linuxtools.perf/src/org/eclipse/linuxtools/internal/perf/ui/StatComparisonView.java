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

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.handlers.Messages;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;
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
 * Perf Statistics Comparison view
 */
public class StatComparisonView extends Viewer {

    // color values constasts
    private static final Color RED = new Color(Display.getDefault(), 150, 0, 0);
    private static final Color GREEN = new Color(Display.getDefault(), 0, 100, 0);

    // event occurrence reg-ex
    private static String OCCURRENCE = "\\s*(\\-?+" //$NON-NLS-1$
            + PMStatEntry.DECIMAL + ").*"; //$NON-NLS-1$

    private Composite fComposite;
    private ICompareInput fInput;
    private StyledText text;
    private Label reverseLabel;
    private boolean reverse;

    public StatComparisonView(Composite parent) {
        fComposite = new Composite(parent, SWT.NONE);
        fComposite.setLayout(new GridLayout(2, false));
        fComposite.setData(CompareUI.COMPARE_VIEWER_TITLE, Messages.StatComparisonView_label);

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

        text = new StyledText(fComposite, SWT.V_SCROLL | SWT.H_SCROLL);
        text.setAlwaysShowScrollBars(false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);
        text.setEditable(false);

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
        // set default TextConsole font (monospaced).
        text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        List<StyleRange> styles = new ArrayList<>();
        int ptr = 0;

        String[] lines = input.split("\n"); //$NON-NLS-1$

        for (String line : lines) {
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
     * Update contents of current view, replacing the containing data and text styling.
     *
     * @param data IPerfData data replacement.
     */
    private void updateData(IPerfData data) {
        if (data != null) {
            setStyledText(data.getPerfData());
        }
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

                if (fInput.getLeft() instanceof ResourceNode) {
                    ResourceNode left = (ResourceNode) fInput.getLeft();
                    oldDatum = left.getResource().getLocation();
                } else {
                    IEncodedStreamContentAccessor lStream = (IEncodedStreamContentAccessor) fInput.getLeft();
                    oldDatum = generateTempFile(lStream);
                }

                if (fInput.getRight() instanceof ResourceNode) {
                    ResourceNode right = (ResourceNode) fInput.getRight();
                    newDatum = right.getResource().getLocation();
                } else {
                    IEncodedStreamContentAccessor rStream = (IEncodedStreamContentAccessor) fInput.getRight();
                    newDatum = generateTempFile(rStream);
                }

                String title = MessageFormat.format(Messages.ContentDescription_0,
                        new Object[] { oldDatum.toFile().getName(), newDatum.toFile().getName() });

                // create comparison data and run comparison.
                StatComparisonData diffData;
                if (reverse) {
                    diffData = new StatComparisonData(title, newDatum, oldDatum);
                } else {
                    diffData = new StatComparisonData(title, oldDatum, newDatum);
                }
                diffData.runComparison();
                updateData(diffData);
            }

        }

        fComposite.layout();
    }

    private IPath generateTempFile(IEncodedStreamContentAccessor stream) {
        try {
            Path tmpFile = Files.createTempFile("perf-stat-", ".stat"); //$NON-NLS-1$ //$NON-NLS-2$
            tmpFile.toFile().delete();
            Files.copy(stream.getContents(), tmpFile);
            return new org.eclipse.core.runtime.Path(tmpFile.toString());
        } catch (IOException|CoreException e) {
            return null;
        }
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
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
