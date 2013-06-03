/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;

public class STAnnotatedCSourceEditor extends CEditor implements LineBackgroundListener {
    /**
     * @since 5.0
     */
    public final static String ST_RULER = "STRuler"; //$NON-NLS-1$

    private STColumnSupport fColumnSupport;

    private STContributedRulerColumn fColumn;

    private IAnnotationProvider fAnnotationProvider;

    private STChangeRulerColumn fSTChangeRulerColumn;

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        if (fAnnotationProvider == null) {
            return;
        }

        STColumnSupport columnSupport = getSTColumnSupport();
        RulerColumnRegistry registry = RulerColumnRegistry.getDefault();

        RulerColumnDescriptor abstractSTColumnDescriptor = registry.getColumnDescriptor(STContributedRulerColumn.ID);
        columnSupport.addSTColumn((CompositeRuler) getVerticalRuler(), abstractSTColumnDescriptor,
                fAnnotationProvider.getColumn());

        CompositeRuler vr = (CompositeRuler) super.getVerticalRuler();
        for (Iterator<?> iter = vr.getDecoratorIterator(); iter.hasNext();) {
            IVerticalRulerColumn column = (IVerticalRulerColumn) iter.next();
            if (column instanceof STContributedRulerColumn) {
                STContributedRulerColumn fSTColumn = (STContributedRulerColumn) column;
                if (fSTColumn.isShowingSTRuler()) {
                    ToolTipSupport.enableFor(fSTColumn);
                }
            }
        }

        showLinesColored();
        if (getSourceViewer() != null) {
            ISourceViewer sv = getSourceViewer();
            if (sv.getTextWidget() != null) {
                sv.getTextWidget().addLineBackgroundListener(this);
            }
        }
    }

    private STColumnSupport getSTColumnSupport() {
        if (fColumnSupport == null) {
            fColumnSupport = new STColumnSupport(this, RulerColumnRegistry.getDefault());
        }
        return fColumnSupport;
    }

    protected class STColumnSupport extends AbstractDecoratedTextEditor.ColumnSupport {
        private final STAnnotatedCSourceEditor fEditor;
        private final RulerColumnRegistry fRegistry;
        private final ArrayList<ISTAnnotationColumn> fColumns;

        public STColumnSupport(STAnnotatedCSourceEditor editor, RulerColumnRegistry registry) {
            super(editor, registry);
            fEditor = editor;
            fRegistry = registry;
            fColumns = new ArrayList<ISTAnnotationColumn>();
        }

        private int computeIndex(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
            int index = 0;
            List<?> all = fRegistry.getColumnDescriptors();
            int newPos = all.indexOf(descriptor);
            for (Iterator<?> it = ruler.getDecoratorIterator(); it.hasNext();) {
                IVerticalRulerColumn column = (IVerticalRulerColumn) it.next();
                if (column instanceof IContributedRulerColumn) {
                    RulerColumnDescriptor rcd = ((IContributedRulerColumn) column).getDescriptor();
                    if (rcd != null && all.indexOf(rcd) > newPos)
                        break;
                } else if ("org.eclipse.jface.text.source.projection.ProjectionRulerColumn".equals(column.getClass().getName())) { //$NON-NLS-1$
                    // projection column is always the rightmost column
                    break;
                }
                index++;
            }
            return index;
        }

        public void addSTColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor,
                final ISTAnnotationColumn annotationColumn) {

            final int idx = computeIndex(ruler, descriptor);

            SafeRunnable runnable = new SafeRunnable() {
                @Override
                public void run() throws Exception {
                    IContributedRulerColumn column = descriptor.createColumn(fEditor);
                    fColumns.add(annotationColumn);
                    initializeColumn(column);
                    ruler.addDecorator(idx, column);
                }
            };
            SafeRunner.run(runnable);
        }

        @Override
        protected void initializeColumn(IContributedRulerColumn column) {
            super.initializeColumn(column);
            RulerColumnDescriptor descriptor = column.getDescriptor();
            IVerticalRuler ruler = getVerticalRuler();
            if (ruler instanceof CompositeRuler) {
                if (STContributedRulerColumn.ID.equals(descriptor.getId())) {
                    fColumn = ((STContributedRulerColumn) column);
                    // this is a workaround...
                    STChangeRulerColumn fDelegate = null;
                    if (fColumns != null && fColumns.size() > 0) {
                        fDelegate = createSTRulerColumn(fColumns.get(fColumns.size() - 1));
                    }
                    fColumn.setSTColumn(fDelegate);
                }
            }
        }
    }

    /**
	 * @since 5.0
	 */
    protected STChangeRulerColumn createSTRulerColumn(ISTAnnotationColumn annotationColumn) {
        fSTChangeRulerColumn = new STChangeRulerColumn(getSharedColors(), annotationColumn);
        ((IChangeRulerColumn) fSTChangeRulerColumn).setHover(createChangeHover());
        initializeLineNumberRulerColumn(fLineNumberRulerColumn);

        return fSTChangeRulerColumn;
    }

    @Override
    public void lineGetBackground(LineBackgroundEvent event) {
        if (fAnnotationProvider != null) {
            StyledTextContent c = (StyledTextContent) event.data;
            int line = c.getLineAtOffset(event.lineOffset);
            event.lineBackground = fAnnotationProvider.getColor(line);
        }
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        if (input != null && input instanceof IEditorInputWithAnnotations) {
            fAnnotationProvider = ((IEditorInputWithAnnotations) input).createAnnotationProvider();
        }
    }

    private static class ToolTipSupport extends DefaultToolTip {
        private final STContributedRulerColumn control;

        static class ToolTipArea {
            final int line;
            final ISTAnnotationColumn ac;

            ToolTipArea(int line, ISTAnnotationColumn ac) {
                this.line = line;
                this.ac = ac;
            }

            public String getToolTip() {
                return ac.getTooltip(line);
            }

        }

        protected ToolTipSupport(STContributedRulerColumn control, int style, boolean manualActivation) {
            super(control.getControl(), style, manualActivation);
            this.control = control;
        }

        @Override
        protected Object getToolTipArea(Event event) {
            int line = control.toDocumentLineNumber(event.y);
            return new ToolTipArea(line, control.getAnnotationColumn(line));
        }

        @Override
        protected Composite createToolTipContentArea(Event event, Composite parent) {
            Composite comp = new Composite(parent, SWT.NONE);
            comp.setLayout(new FillLayout());
            Label b = new Label(comp, SWT.NONE);
            ToolTipArea area = (ToolTipArea) getToolTipArea(event);
            if (area != null && area.getToolTip().trim().length() > 0) {
                b.setText(area.getToolTip());
            }
            return comp;
        }

        public static void enableFor(STContributedRulerColumn control) {
            new ToolTipSupport(control, ToolTip.NO_RECREATE, false);
        }
    }

    @Override
    protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
        return new STOverviewRuler(getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors);

    }

    private void showLinesColored() {
        STOverviewRuler or = (STOverviewRuler) getOverviewRuler();
        IAnnotationModel am = or.getModel();
        IDocument doc = getSourceViewer().getDocument();
        int lines = doc.getNumberOfLines();

        for (int i = 0; i < lines; i++) {
            try {
                Color color = fAnnotationProvider.getColor(i);
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                if (r != 255 || g != 255 || b != 255) {
                    int offset = doc.getLineOffset(i);
                    String type = STAnnotatedSourceEditorActivator.ANNOTATION_TYPE;
                    Annotation annotation = new Annotation(type, true, ""); //$NON-NLS-1$
                    or.setAnnotationColor(annotation, color);
                    am.addAnnotation(annotation, new Position(offset));
                }
            } catch (BadLocationException e) {
                Status s = new Status(IStatus.ERROR, STAnnotatedSourceEditorActivator.PLUGIN_ID, IStatus.ERROR,
                        e.getMessage(), e);
                STAnnotatedSourceEditorActivator.getDefault().getLog().log(s);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.ui.editor.CEditor#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (fAnnotationProvider != null)
            fAnnotationProvider.dispose();
    }

}
