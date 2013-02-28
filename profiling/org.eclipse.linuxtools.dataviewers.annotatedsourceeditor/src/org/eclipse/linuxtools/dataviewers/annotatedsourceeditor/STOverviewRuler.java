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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class STOverviewRuler implements IOverviewRuler {

    /**
     * Internal listener class.
     */
    class InternalListener implements ITextListener, IAnnotationModelListener {

        /*
         * @see ITextListener#textChanged
         */
        @Override
        public void textChanged(TextEvent e) {
            if (fTextViewer != null && e.getDocumentEvent() == null && e.getViewerRedrawState()) {
                // handle only changes of visible document
                redraw();
            }
        }

        /*
         * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
         */
        @Override
        public void modelChanged(IAnnotationModel model) {
            update();
        }
    }

    /**
     * Enumerates the annotations of a specified type and characteristics of the associated annotation model.
     */
    class FilterIterator implements Iterator<Annotation> {

        final static int TEMPORARY = 1 << 1;
        final static int PERSISTENT = 1 << 2;
        final static int IGNORE_BAGS = 1 << 3;

        private Iterator<?> fIterator;
        private Object fType;
        private Annotation fNext;
        private int fStyle;

        /**
         * Creates a new filter iterator with the given specification.
         *
         * @param annotationType
         *            the annotation type
         * @param style
         *            the style
         */
        public FilterIterator(Object annotationType, int style) {
            fType = annotationType;
            fStyle = style;
            if (fModel != null) {
                fIterator = fModel.getAnnotationIterator();
                skip();
            }
        }

        /**
         * Creates a new filter iterator with the given specification.
         *
         * @param annotationType
         *            the annotation type
         * @param style
         *            the style
         * @param iterator
         *            the iterator
         */
        public FilterIterator(Object annotationType, int style, Iterator<?> iterator) {
            fType = annotationType;
            fStyle = style;
            fIterator = iterator;
            skip();
        }

        private void skip() {

            boolean temp = (fStyle & TEMPORARY) != 0;
            boolean pers = (fStyle & PERSISTENT) != 0;
            boolean ignr = (fStyle & IGNORE_BAGS) != 0;

            while (fIterator.hasNext()) {
                Annotation next = (Annotation) fIterator.next();
                if (next.isMarkedDeleted())
                    continue;
                if (ignr && (next instanceof AnnotationBag))
                    continue;
                fNext = next;
                Object annotationType = next.getType();
                if (fType == null || isSubtype(annotationType)) {
                    if (temp && pers)
                        return;
                    if (pers && next.isPersistent())
                        return;
                    if (temp && !next.isPersistent())
                        return;
                }
            }
            fNext = null;
        }

        private boolean isSubtype(Object annotationType) {
            if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
                IAnnotationAccessExtension extension = (IAnnotationAccessExtension) fAnnotationAccess;
                return extension.isSubtype(annotationType, fType);
            }
            return fType.equals(annotationType);
        }

        /*
         * @see Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return fNext != null;
        }

        /*
         * @see Iterator#next()
         */
        @Override
        public Annotation next() {
            try {
                return fNext;
            } finally {
                if (fIterator != null)
                    skip();
            }
        }

        /*
         * @see Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }



    private static final int INSET = 2;
    private static final int ANNOTATION_HEIGHT = 1;
    private static boolean ANNOTATION_HEIGHT_SCALABLE = true;

    /** The model of the overview ruler */
    private IAnnotationModel fModel;
    /** The view to which this ruler is connected */
    private ITextViewer fTextViewer;
    /** The ruler's canvas */
    private Canvas fCanvas;
    /** The ruler's header */
    private Canvas fHeader;
    /** The buffer for double buffering */
    private Image fBuffer;
    /** The internal listener */
    private InternalListener fInternalListener = new InternalListener();
    /** The width of this vertical ruler */
    private int fWidth;
    /** The hit detection cursor */
    private Cursor fHitDetectionCursor;
    /** The last cursor */
    private Cursor fLastCursor;
    /** The line of the last mouse button activity */
    private int fLastMouseButtonActivityLine = -1;
    /** The actual annotation height */
    private int fAnnotationHeight = -1;
    /** The annotation access */
    private IAnnotationAccess fAnnotationAccess;
    /**
     * The list of annotation types to be shown in this ruler.
     *
     * @since 3.0
     */
    private Set<Object> fConfiguredAnnotationTypes = new HashSet<Object>();
    /**
     * The list of annotation types to be shown in the header of this ruler.
     *
     * @since 3.0
     */
    private Set<Object> fConfiguredHeaderAnnotationTypes = new HashSet<Object>();
    /** The mapping between annotation types and colors */
    private Map<Object, Color> fAnnotationTypes2Colors = new HashMap<Object, Color>();

    /** The mapping between annotation and colors */
    private Map<Annotation, Color> fAnnotationColors = new HashMap<Annotation, Color>();

    /** The color manager */
    private ISharedTextColors fSharedTextColors;
    /**
     * All available annotation types sorted by layer.
     *
     * @since 3.0
     */
    private List<Object> fAnnotationsSortedByLayer = new ArrayList<Object>();
    /**
     * All available layers sorted by layer. This list may contain duplicates.
     *
     * @since 3.0
     */
    private List<Integer> fLayersSortedByLayer = new ArrayList<Integer>();
    /**
     * Map of allowed annotation types. An allowed annotation type maps to <code>true</code>, a disallowed to
     * <code>false</code>.
     *
     * @since 3.0
     */
    private Map<Object, Boolean> fAllowedAnnotationTypes = new HashMap<Object, Boolean>();
    /**
     * Map of allowed header annotation types. An allowed annotation type maps to <code>true</code>, a disallowed to
     * <code>false</code>.
     *
     * @since 3.0
     */
    private Map<Object, Boolean> fAllowedHeaderAnnotationTypes = new HashMap<Object, Boolean>();
    /**
     * The cached annotations.
     *
     * @since 3.0
     */
    private List<Annotation> fCachedAnnotations = new ArrayList<Annotation>();

    /**
     * Constructs a overview ruler of the given width using the given annotation access and the given color manager.
     *
     * @param annotationAccess
     *            the annotation access
     * @param width
     *            the width of the vertical ruler
     * @param sharedColors
     *            the color manager
     */
    public STOverviewRuler(IAnnotationAccess annotationAccess, int width, ISharedTextColors sharedColors) {
        fAnnotationAccess = annotationAccess;
        fWidth = width;
        fSharedTextColors = sharedColors;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getControl()
     */
    @Override
    public Control getControl() {
        return fCanvas;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getWidth()
     */
    @Override
    public int getWidth() {
        return fWidth;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRuler#setModel(org.eclipse.jface.text.source.IAnnotationModel)
     */
    @Override
    public void setModel(IAnnotationModel model) {
        if (model != fModel || model != null) {
            if (fModel != null)
                fModel.removeAnnotationModelListener(fInternalListener);
            fModel = model;
            if (fModel != null)
                fModel.addAnnotationModelListener(fInternalListener);
            update();
        }
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRuler#createControl(org.eclipse.swt.widgets.Composite,
     * org.eclipse.jface.text.ITextViewer)
     */
    @Override
    public Control createControl(Composite parent, ITextViewer textViewer) {
        fTextViewer = textViewer;
        fHitDetectionCursor = parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
        fHeader = new Canvas(parent, SWT.NONE);
        fCanvas = new Canvas(parent, SWT.NO_BACKGROUND);
        fCanvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent event) {
                if (fTextViewer != null)
                    doubleBufferPaint(event.gc);
            }
        });
        fCanvas.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                handleDispose();
                fTextViewer = null;
            }
        });
        fCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                handleMouseDown(event);
            }
        });
        fCanvas.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent event) {
                handleMouseMove(event);
            }
        });
        if (fTextViewer != null)
            fTextViewer.addTextListener(fInternalListener);
        return fCanvas;
    }

    /**
     * Disposes the ruler's resources.
     */
    private void handleDispose() {
        if (fTextViewer != null) {
            fTextViewer.removeTextListener(fInternalListener);
            fTextViewer = null;
        }
        if (fModel != null)
            fModel.removeAnnotationModelListener(fInternalListener);
        if (fBuffer != null) {
            fBuffer.dispose();
            fBuffer = null;
        }
        fConfiguredAnnotationTypes.clear();
        fAllowedAnnotationTypes.clear();
        fConfiguredHeaderAnnotationTypes.clear();
        fAllowedHeaderAnnotationTypes.clear();
        fAnnotationTypes2Colors.clear();
        fAnnotationsSortedByLayer.clear();
        fLayersSortedByLayer.clear();
    }

    /**
     * Double buffer drawing.
     *
     * @param dest
     *            the GC to draw into
     */
    private void doubleBufferPaint(GC dest) {
        Point size = fCanvas.getSize();
        if (size.x <= 0 || size.y <= 0)
            return;

        if (fBuffer != null) {
            Rectangle r = fBuffer.getBounds();
            if (r.width != size.x || r.height != size.y) {
                fBuffer.dispose();
                fBuffer = null;
            }
        }
        if (fBuffer == null)
            fBuffer = new Image(fCanvas.getDisplay(), size.x, size.y);

        GC gc = new GC(fBuffer);
        try {
            gc.setBackground(fCanvas.getBackground());
            gc.fillRectangle(0, 0, size.x, size.y);
            doPaint(gc);
        } finally {
            gc.dispose();
        }
        dest.drawImage(fBuffer, 0, 0);
    }

    /**
     * Draws this overview ruler. Uses <code>ITextViewerExtension5</code> for its implementation.
     * @param gc
     *            the GC to draw into
     */
    private void doPaint(GC gc) {
        Rectangle r = new Rectangle(0, 0, 0, 0);
        int yy, hh = ANNOTATION_HEIGHT;

        ITextViewerExtension5 extension = (ITextViewerExtension5) fTextViewer;
        IDocument document = fTextViewer.getDocument();
        StyledText textWidget = fTextViewer.getTextWidget();

        int maxLines = textWidget.getLineCount();
        Point size = fCanvas.getSize();

        fCachedAnnotations.clear();
        if (fModel != null) {
            Iterator<?> iter = fModel.getAnnotationIterator();
            while (iter.hasNext()) {
                Annotation annotation = (Annotation) iter.next();

                if (annotation.isMarkedDeleted() || skip(annotation.getType()))
                    continue;

                fCachedAnnotations.add(annotation);
            }
        }

        for (Iterator<Object> iterator = fAnnotationsSortedByLayer.iterator(); iterator.hasNext();) {
            Object annotationType = iterator.next();

            if (skip(annotationType))
                continue;

            int[] style = new int[] { FilterIterator.PERSISTENT, FilterIterator.TEMPORARY };
            for (int t = 0; t < style.length; t++) {

                FilterIterator e = new FilterIterator(annotationType, style[t], fCachedAnnotations.iterator());
                Color fill = null;
                Color stroke = null;
                if (annotationType.toString().compareTo(STAnnotatedSourceEditorActivator.ANNOTATION_TYPE) != 0) {
                    fill = getFillColor(annotationType, style[t] == FilterIterator.TEMPORARY);
                    stroke = getStrokeColor(annotationType, style[t] == FilterIterator.TEMPORARY);
                }

                while (e.hasNext()) {
                    Annotation a = e.next();
                    Position p = fModel.getPosition(a);
                    if (p == null)
                        continue;

                    IRegion widgetRegion = extension.modelRange2WidgetRange(new Region(p.getOffset(), p.getLength()));
                    if (widgetRegion == null)
                        continue;

                    if (a.getType().compareTo(STAnnotatedSourceEditorActivator.ANNOTATION_TYPE) == 0) {
                        fill = getFPFillColor(a, true);
                        stroke = getFPStrokeColor(a, false);
                    }

                    try {
                        if (ANNOTATION_HEIGHT_SCALABLE) {
                            int numbersOfLines = document.getNumberOfLines(p.getOffset(), p.getLength());
                            // don't count empty trailing lines
                            IRegion lastLine = document.getLineInformationOfOffset(p.getOffset() + p.getLength());
                            if (lastLine.getOffset() == p.getOffset() + p.getLength()) {
                                numbersOfLines -= 2;
                                hh = (numbersOfLines * size.y) / maxLines + ANNOTATION_HEIGHT;
                                if (hh < ANNOTATION_HEIGHT)
                                    hh = ANNOTATION_HEIGHT;
                            } else
                                hh = ANNOTATION_HEIGHT;
                        }
                        fAnnotationHeight = hh;

                        int startLine = textWidget.getLineAtOffset(widgetRegion.getOffset());
                        yy = Math.min((startLine * size.y) / maxLines, size.y - hh);

                        if (fill != null) {
                            gc.setBackground(fill);
                            gc.fillRectangle(INSET, yy, size.x - (2 * INSET), hh);
                        }

                        if (stroke != null) {
                            gc.setForeground(stroke);
                            r.x = INSET;
                            r.y = yy;
                            r.width = size.x - (2 * INSET);
                            r.height = hh;
                            gc.setLineWidth(1);
                            gc.drawRectangle(r);
                        }
                    } catch (BadLocationException x) {
                    }
                }
            }
        }
        fCachedAnnotations.clear();
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRuler#update()
     */
    @Override
    public void update() {
        if (fCanvas != null && !fCanvas.isDisposed()) {
            Display d = fCanvas.getDisplay();
            if (d != null) {
                d.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        redraw();
                    }
                });
            }
        }
    }

    /**
     * Redraws the overview ruler.
     */
    private void redraw() {
        if (fTextViewer == null || fModel == null)
            return;

        if (fCanvas != null && !fCanvas.isDisposed()) {
            GC gc = new GC(fCanvas);
            doubleBufferPaint(gc);
            gc.dispose();
        }
    }

    /**
     * Translates a given y-coordinate of this ruler into the corresponding document lines. The number of lines depends
     * on the concrete scaling given as the ration between the height of this ruler and the length of the document.
     *
     * @param y_coordinate
     *            the y-coordinate
     * @return the corresponding document lines
     */
    private int[] toLineNumbers(int y_coordinate) {
        StyledText textWidget = fTextViewer.getTextWidget();
        int maxLines = textWidget.getContent().getLineCount();

        int rulerLength = fCanvas.getSize().y;
        int writable = JFaceTextUtil.computeLineHeight(textWidget, 0, maxLines, maxLines);

        if (y_coordinate >= writable || y_coordinate >= rulerLength)
            return new int[] { -1, -1 };

        int[] lines = new int[2];

        int pixel0 = Math.max(y_coordinate - 1, 0);
        int pixel1 = Math.min(rulerLength, y_coordinate + 1);
        rulerLength = Math.max(rulerLength, 1);

        lines[0] = (pixel0 * maxLines) / rulerLength;
        lines[1] = (pixel1 * maxLines) / rulerLength;

        if (fTextViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) fTextViewer;
            lines[0] = extension.widgetLine2ModelLine(lines[0]);
            lines[1] = extension.widgetLine2ModelLine(lines[1]);
        } else {
            try {
                IRegion visible = fTextViewer.getVisibleRegion();
                int lineNumber = fTextViewer.getDocument().getLineOfOffset(visible.getOffset());
                lines[0] += lineNumber;
                lines[1] += lineNumber;
            } catch (BadLocationException x) {
            }
        }

        return lines;
    }

    /**
     * Returns the position of the first annotation found in the given line range.
     *
     * @param lineNumbers
     *            the line range
     * @param ignoreSelectedAnnotation
     *            whether to ignore the current selection
     * @return the position of the first found annotation
     */
    private Position getAnnotationPosition(int[] lineNumbers, boolean ignoreSelectedAnnotation) {
        if (lineNumbers[0] == -1)
            return null;
        Position found = null;

        try {
            IDocument d = fTextViewer.getDocument();
            IRegion line = d.getLineInformation(lineNumbers[0]);
            Point currentSelection = fTextViewer.getSelectedRange();
            int start = line.getOffset();
            line = d.getLineInformation(lineNumbers[lineNumbers.length - 1]);
            int end = line.getOffset() + line.getLength();

            for (int i = fAnnotationsSortedByLayer.size() - 1; i >= 0; i--) {

                Object annotationType = fAnnotationsSortedByLayer.get(i);

                FilterIterator e = new FilterIterator(annotationType, FilterIterator.PERSISTENT
                        | FilterIterator.TEMPORARY);
                while (e.hasNext() && found == null) {
                    Annotation a = e.next();
                    if (a.isMarkedDeleted() || skip(a.getType()))
                        continue;

                    Position p = fModel.getPosition(a);
                    if (p == null)
                        continue;

                    int posOffset = p.getOffset();
                    int posEnd = posOffset + p.getLength();
                    IRegion region = d.getLineInformationOfOffset(posEnd);
                    // trailing empty lines don't count
                    if (posEnd > posOffset && region.getOffset() == posEnd) {
                        posEnd--;
                        region = d.getLineInformationOfOffset(posEnd);
                    }

                    if (posOffset <= end && posEnd >= start) {
                        if (ignoreSelectedAnnotation || currentSelection.x != posOffset
                                || currentSelection.y != p.getLength())
                            found = p;
                    }
                }
            }
        } catch (BadLocationException x) {
        }

        return found;
    }

    /**
     * Returns the line which corresponds best to one of the underlying annotations at the given y-coordinate.
     *
     * @param lineNumbers
     *            the line numbers
     * @return the best matching line or <code>-1</code> if no such line can be found
     */
    private int findBestMatchingLineNumber(int[] lineNumbers) {
        if (lineNumbers == null || lineNumbers.length < 1)
            return -1;

        try {
            Position pos = getAnnotationPosition(lineNumbers, true);
            if (pos == null)
                return -1;
            return fTextViewer.getDocument().getLineOfOffset(pos.getOffset());
        } catch (BadLocationException ex) {
            return -1;
        }
    }

    /**
     * Handles mouse clicks.
     *
     * @param event
     *            the mouse button down event
     */
    private void handleMouseDown(MouseEvent event) {
        if (fTextViewer != null) {
            int[] lines = toLineNumbers(event.y);
            Position p = getAnnotationPosition(lines, false);
            if (p != null) {
                fTextViewer.revealRange(p.getOffset(), p.getLength());
                fTextViewer.setSelectedRange(p.getOffset(), p.getLength());
            }
            fTextViewer.getTextWidget().setFocus();
        }
        fLastMouseButtonActivityLine = toDocumentLineNumber(event.y);
    }

    /**
     * Handles mouse moves.
     *
     * @param event
     *            the mouse move event
     */
    private void handleMouseMove(MouseEvent event) {
        if (fTextViewer != null) {
            int[] lines = toLineNumbers(event.y);
            Position p = getAnnotationPosition(lines, true);
            Cursor cursor = (p != null ? fHitDetectionCursor : null);
            if (cursor != fLastCursor) {
                fCanvas.setCursor(cursor);
                fLastCursor = cursor;
            }
        }
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#addAnnotationType(java.lang.Object)
     */
    @Override
    public void addAnnotationType(Object annotationType) {
        fConfiguredAnnotationTypes.add(annotationType);
        fAllowedAnnotationTypes.clear();
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#removeAnnotationType(java.lang.Object)
     */
    @Override
    public void removeAnnotationType(Object annotationType) {
        fConfiguredAnnotationTypes.remove(annotationType);
        fAllowedAnnotationTypes.clear();
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#setAnnotationTypeLayer(java.lang.Object, int)
     */
    @Override
    public void setAnnotationTypeLayer(Object annotationType, int layer) {
        int j = fAnnotationsSortedByLayer.indexOf(annotationType);
        if (j != -1) {
            fAnnotationsSortedByLayer.remove(j);
            fLayersSortedByLayer.remove(j);
        }

        if (layer >= 0) {
            int i = 0;
            int size = fLayersSortedByLayer.size();
            while (i < size && layer >= fLayersSortedByLayer.get(i))
                i++;
            fLayersSortedByLayer.add(i, layer);
            fAnnotationsSortedByLayer.add(i, annotationType);
        }
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#setAnnotationTypeColor(java.lang.Object,
     * org.eclipse.swt.graphics.Color)
     */

    public void setAnnotationColor(Annotation annotation, Color color) {
        if (color != null)
            fAnnotationColors.put(annotation, color);
        else
            fAnnotationColors.remove(annotation);
    }

    @Override
    public void setAnnotationTypeColor(Object annotationType, Color color) {
        if (color != null)
            fAnnotationTypes2Colors.put(annotationType, color);
        else
            fAnnotationTypes2Colors.remove(annotationType);
    }

    /**
     * Returns whether the given annotation type should be skipped by the drawing routine.
     *
     * @param annotationType
     *            the annotation type
     * @return <code>true</code> if annotation of the given type should be skipped
     */
    private boolean skip(Object annotationType) {
        return !contains(annotationType, fAllowedAnnotationTypes, fConfiguredAnnotationTypes);
    }

    /**
     * Returns whether the given annotation type is mapped to <code>true</code> in the given <code>allowed</code> map or
     * covered by the <code>configured</code> set.
     *
     * @param annotationType
     *            the annotation type
     * @param allowed
     *            the map with allowed annotation types mapped to booleans
     * @param configured
     *            the set with configured annotation types
     * @return <code>true</code> if annotation is contained, <code>false</code> otherwise
     * @since 3.0
     */
    private boolean contains(Object annotationType, Map<Object, Boolean> allowed, Set<Object> configured) {
        Boolean cached = allowed.get(annotationType);
        if (cached != null)
            return cached.booleanValue();

        boolean covered = isCovered(annotationType, configured);
        allowed.put(annotationType, covered ? Boolean.TRUE : Boolean.FALSE);
        return covered;
    }

    /**
     * Computes whether the annotations of the given type are covered by the given <code>configured</code> set. This is
     * the case if either the type of the annotation or any of its super types is contained in the
     * <code>configured</code> set.
     *
     * @param annotationType
     *            the annotation type
     * @param configured
     *            the set with configured annotation types
     * @return <code>true</code> if annotation is covered, <code>false</code> otherwise
     * @since 3.0
     */
    private boolean isCovered(Object annotationType, Set<Object> configured) {
        if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
            IAnnotationAccessExtension extension = (IAnnotationAccessExtension) fAnnotationAccess;
            Iterator<?> e = configured.iterator();
            while (e.hasNext()) {
                if (extension.isSubtype(annotationType, e.next()))
                    return true;
            }
            return false;
        }
        return configured.contains(annotationType);
    }

    /**
     * Returns a specification of a color that lies between the given foreground and background color using the given
     * scale factor.
     *
     * @param fg
     *            the foreground color
     * @param bg
     *            the background color
     * @param scale
     *            the scale factor
     * @return the interpolated color
     */
    private static RGB interpolate(RGB fg, RGB bg, double scale) {
        return new RGB((int) ((1.0 - scale) * fg.red + scale * bg.red), (int) ((1.0 - scale) * fg.green + scale
                * bg.green), (int) ((1.0 - scale) * fg.blue + scale * bg.blue));
    }

    /**
     * Returns the grey value in which the given color would be drawn in grey-scale.
     *
     * @param rgb
     *            the color
     * @return the grey-scale value
     */
    private static double greyLevel(RGB rgb) {
        if (rgb.red == rgb.green && rgb.green == rgb.blue)
            return rgb.red;
        return (0.299 * rgb.red + 0.587 * rgb.green + 0.114 * rgb.blue + 0.5);
    }

    /**
     * Returns whether the given color is dark or light depending on the colors grey-scale level.
     *
     * @param rgb
     *            the color
     * @return <code>true</code> if the color is dark, <code>false</code> if it is light
     */
    private static boolean isDark(RGB rgb) {
        return greyLevel(rgb) > 128;
    }

    /**
     * Returns a color based on the color configured for the given annotation type and the given scale factor.
     *
     * @param annotationType
     *            the annotation type
     * @param scale
     *            the scale factor
     * @return the computed color
     */
    private Color getColor(Object annotationType, double scale) {
        Color base = findColor(annotationType);
        return getInterpolatedColor(base, scale);
    }

    private Color getFPColor(Object annotation, double scale) {
        Color base = fAnnotationColors.get(annotation);
        return getInterpolatedColor(base, scale);
    }

    private Color getInterpolatedColor(Color base, double scale) {
        if (base == null)
            return null;

        RGB baseRGB = base.getRGB();
        RGB background = fCanvas.getBackground().getRGB();

        boolean darkBase = isDark(baseRGB);
        boolean darkBackground = isDark(background);
        if (darkBase && darkBackground)
            background = new RGB(255, 255, 255);
        else if (!darkBase && !darkBackground)
            background = new RGB(0, 0, 0);

        return fSharedTextColors.getColor(interpolate(baseRGB, background, scale));
    }

    /**
     * Returns the color for the given annotation type
     *
     * @param annotationType
     *            the annotation type
     * @return the color
     * @since 3.0
     */
    private Color findColor(Object annotationType) {
        Color color = fAnnotationTypes2Colors.get(annotationType);
        if (color != null)
            return color;

        if (fAnnotationAccess instanceof IAnnotationAccessExtension) {
            IAnnotationAccessExtension extension = (IAnnotationAccessExtension) fAnnotationAccess;
            Object[] superTypes = extension.getSupertypes(annotationType);
            if (superTypes != null) {
                for (int i = 0; i < superTypes.length; i++) {
                    color = fAnnotationTypes2Colors.get(superTypes[i]);
                    if (color != null)
                        return color;
                }
            }
        }

        return null;
    }

    /**
     * Returns the stroke color for the given annotation type and characteristics.
     *
     * @param annotationType
     *            the annotation type
     * @param temporary
     *            <code>true</code> if for temporary annotations
     * @return the stroke color
     */
    private Color getStrokeColor(Object annotationType, boolean temporary) {
        return getColor(annotationType, temporary ? 0.5 : 0.2);
    }

    private Color getFPStrokeColor(Object annotation, boolean temporary) {
        return getFPColor(annotation, temporary ? 0.5 : 0.2);
    }

    /**
     * Returns the fill color for the given annotation type and characteristics.
     *
     * @param annotationType
     *            the annotation type
     * @param temporary
     *            <code>true</code> if for temporary annotations
     * @return the fill color
     */
    private Color getFillColor(Object annotationType, boolean temporary) {
        return getColor(annotationType, temporary ? 0.9 : 0.6);
    }

    private Color getFPFillColor(Object annotation, boolean temporary) {
        return getFPColor(annotation, temporary ? 0.9 : 0.6);
    }

    /*
     * @see IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
     */
    @Override
    public int getLineOfLastMouseButtonActivity() {
        return fLastMouseButtonActivityLine;
    }

    /*
     * @see IVerticalRulerInfo#toDocumentLineNumber(int)
     */
    @Override
    public int toDocumentLineNumber(int y_coordinate) {
        if (fTextViewer == null || y_coordinate == -1)
            return -1;

        int[] lineNumbers = toLineNumbers(y_coordinate);
        int bestLine = findBestMatchingLineNumber(lineNumbers);
        if (bestLine == -1 && lineNumbers.length > 0)
            return lineNumbers[0];
        return bestLine;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRuler#getModel()
     */
    @Override
    public IAnnotationModel getModel() {
        return fModel;
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#getAnnotationHeight()
     */
    @Override
    public int getAnnotationHeight() {
        return fAnnotationHeight;
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#hasAnnotation(int)
     */
    @Override
    public boolean hasAnnotation(int y) {
        return findBestMatchingLineNumber(toLineNumbers(y)) != -1;
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#getHeaderControl()
     */
    @Override
    public Control getHeaderControl() {
        return fHeader;
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#addHeaderAnnotationType(java.lang.Object)
     */
    @Override
    public void addHeaderAnnotationType(Object annotationType) {
        fConfiguredHeaderAnnotationTypes.add(annotationType);
        fAllowedHeaderAnnotationTypes.clear();
    }

    /*
     * @see org.eclipse.jface.text.source.IOverviewRuler#removeHeaderAnnotationType(java.lang.Object)
     */
    @Override
    public void removeHeaderAnnotationType(Object annotationType) {
        fConfiguredHeaderAnnotationTypes.remove(annotationType);
        fAllowedHeaderAnnotationTypes.clear();
    }
}
