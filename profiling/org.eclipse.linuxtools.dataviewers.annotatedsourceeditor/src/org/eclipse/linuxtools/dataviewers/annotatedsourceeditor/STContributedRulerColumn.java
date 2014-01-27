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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension.RenderingMode;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDifferExtension;
import org.eclipse.jface.text.source.ILineDifferExtension2;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

public class STContributedRulerColumn extends AbstractContributedRulerColumn implements IVerticalRulerInfo,
        IVerticalRulerInfoExtension {
    /**
     * Forwarder for preference checks and ruler creation. Needed to maintain the forwarded APIs in
     * {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditor}.
     */
    public static final String ID = "org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.column"; //$NON-NLS-1$

    private static final String FG_COLOR_KEY = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
    private static final String BG_COLOR_KEY = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
    private static final String USE_DEFAULT_BG_KEY = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
    private final static String ST_KEY = "STRuler"; //$NON-NLS-1$
    private final static String REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY = AbstractDecoratedTextEditorPreferenceConstants.REVISION_ASK_BEFORE_QUICKDIFF_SWITCH;

    private STChangeRulerColumn fDelegate;

    /**
     * Preference dispatcher that registers a single listener so we don't have to manage every single preference
     * listener.
     */
    private PropertyEventDispatcher fDispatcher;
    private ISourceViewer fViewer;

    /*
     * @see
     * org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler,
     * org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
        Assert.isTrue(fDelegate != null);
        ITextViewer viewer = parentRuler.getTextViewer();
        Assert.isLegal(viewer instanceof ISourceViewer);
        fViewer = (ISourceViewer) viewer;
        initialize();
        Control control = fDelegate.createControl(parentRuler, parentControl);
        return control;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getControl()
     */
    @Override
    public Control getControl() {
        return fDelegate.getControl();
    }

    public ISTAnnotationColumn getAnnotationColumn(int line) {
        if (fDelegate != null) {
            return fDelegate.getSTAnnotationColumn();
        }
        return null;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#getWidth()
     */
    @Override
    public int getWidth() {
        return fDelegate.getWidth();
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#redraw()
     */
    @Override
    public void redraw() {
        fDelegate.redraw();
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setFont(org.eclipse.swt.graphics.Font)
     */
    @Override
    public void setFont(Font font) {
        fDelegate.setFont(font);
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerColumn#setModel(org.eclipse.jface.text.source.IAnnotationModel)
     */
    @Override
    public void setModel(IAnnotationModel model) {
        if (getQuickDiffPreference()) {
            fDelegate.setModel(model);
        }
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
     */
    @Override
    public int getLineOfLastMouseButtonActivity() {
        if (fDelegate != null) {
           fDelegate.getLineOfLastMouseButtonActivity();
        }
        return -1;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
     */
    @Override
    public int toDocumentLineNumber(int y_coordinate) {
        if (fDelegate != null) {
            return fDelegate.toDocumentLineNumber(y_coordinate);
        }
        return -1;
    }

    /*
     * @see
     * org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source
     * .IVerticalRulerListener)
     */
    @Override
    public void addVerticalRulerListener(IVerticalRulerListener listener) {
        if (fDelegate != null)
            fDelegate.addVerticalRulerListener(listener);
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
     */
    @Override
    public IAnnotationHover getHover() {
        if (fDelegate != null) {
            return fDelegate.getHover();
        }
        return null;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
     */
    @Override
    public IAnnotationModel getModel() {
        if (fDelegate != null) {
            return fDelegate.getModel();
        }
        return null;
    }

    /*
     * @see
     * org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.
     * source.IVerticalRulerListener)
     */
    @Override
    public void removeVerticalRulerListener(IVerticalRulerListener listener) {
        if (fDelegate != null) {
        	fDelegate.removeVerticalRulerListener(listener);
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn#columnRemoved()
     */
    @Override
    public void columnRemoved() {
        if (fDispatcher != null) {
            fDispatcher.dispose();
            fDispatcher = null;
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return EditorsUI.getPreferenceStore();
    }

    private ISharedTextColors getSharedColors() {
        return EditorsUI.getSharedTextColors();
    }

    /**
     * Initializes the given line number ruler column from the preference store.
     */
    private void initialize() {
        final IPreferenceStore store = getPreferenceStore();
        if (store == null)
            return;

        // initial set up
        updateForegroundColor(store, fDelegate);
        updateBackgroundColor(store, fDelegate);

        updateLineNumbersVisibility(fDelegate);
        updateQuickDiffVisibility(fDelegate);
        updateCharacterMode(store, fDelegate);
        updateRevisionRenderingMode(store, fDelegate);
        updateRevisionAuthorVisibility(store, fDelegate);
        updateRevisionIdVisibility(store, fDelegate);

        Map<Object, AnnotationPreference> annotationPrefs = getAnnotationPreferenceMap();
        final AnnotationPreference changedPref = annotationPrefs
                .get("org.eclipse.ui.workbench.texteditor.quickdiffChange"); //$NON-NLS-1$
        final AnnotationPreference addedPref = annotationPrefs
                .get("org.eclipse.ui.workbench.texteditor.quickdiffAddition"); //$NON-NLS-1$
        final AnnotationPreference deletedPref = annotationPrefs
                .get("org.eclipse.ui.workbench.texteditor.quickdiffDeletion"); //$NON-NLS-1$
        updateChangedColor(changedPref, store, fDelegate);
        updateAddedColor(addedPref, store, fDelegate);
        updateDeletedColor(deletedPref, store, fDelegate);

        if (fDelegate != null)
            fDelegate.redraw();

        // listen to changes
        fDispatcher = new PropertyEventDispatcher(store);

        fDispatcher.addPropertyChangeListener(FG_COLOR_KEY, new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                updateForegroundColor(store, fDelegate);
                fDelegate.redraw();
            }
        });
        IPropertyChangeListener backgroundHandler = new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                updateBackgroundColor(store, fDelegate);
                fDelegate.redraw();
            }
        };
        fDispatcher.addPropertyChangeListener(BG_COLOR_KEY, backgroundHandler);
        fDispatcher.addPropertyChangeListener(USE_DEFAULT_BG_KEY, backgroundHandler);

        fDispatcher.addPropertyChangeListener(ST_KEY, new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                // only handle quick diff on/off information, but not ruler visibility (handled by
                // AbstractDecoratedTextEditor)

                updateLineNumbersVisibility(fDelegate);
            }
        });

        fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE,
                new IPropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        updateCharacterMode(store, fDelegate);
                    }
                });

        fDispatcher.addPropertyChangeListener(
                AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_RENDERING_MODE,
                new IPropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        updateRevisionRenderingMode(store, fDelegate);
                    }
                });

        fDispatcher.addPropertyChangeListener(
                AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_AUTHOR,
                new IPropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        updateRevisionAuthorVisibility(store, fDelegate);
                    }
                });

        fDispatcher.addPropertyChangeListener(
                AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_REVISION,
                new IPropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        updateRevisionIdVisibility(store, fDelegate);
                    }
                });

        fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON,
                new IPropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        updateQuickDiffVisibility(fDelegate);
                    }
                });

        if (changedPref != null) {
            fDispatcher.addPropertyChangeListener(changedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    updateChangedColor(changedPref, store, fDelegate);
                    fDelegate.redraw();
                }
            });
        }
        if (addedPref != null) {
            fDispatcher.addPropertyChangeListener(addedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    updateAddedColor(addedPref, store, fDelegate);
                    fDelegate.redraw();
                }
            });
        }
        if (deletedPref != null) {
            fDispatcher.addPropertyChangeListener(deletedPref.getColorPreferenceKey(), new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    updateDeletedColor(deletedPref, store, fDelegate);
                    fDelegate.redraw();
                }
            });
        }
    }

    private Map<Object, AnnotationPreference> getAnnotationPreferenceMap() {
        Map<Object, AnnotationPreference> annotationPrefs = new HashMap<>();
        MarkerAnnotationPreferences fAnnotationPreferences = new MarkerAnnotationPreferences();
        Iterator<?> iter = fAnnotationPreferences.getAnnotationPreferences().iterator();
        while (iter.hasNext()) {
            AnnotationPreference pref = (AnnotationPreference) iter.next();
            Object type = pref.getAnnotationType();
            annotationPrefs.put(type, pref);
        }
        return annotationPrefs;
    }

    private void updateForegroundColor(IPreferenceStore store, IVerticalRulerColumn column) {
        RGB rgb = getColorFromStore(store, FG_COLOR_KEY);
        if (rgb == null)
            rgb = new RGB(0, 0, 0);
        ISharedTextColors sharedColors = getSharedColors();
        if (column instanceof STRulerColumn)
            ((STRulerColumn) column).setForeground(sharedColors.getColor(rgb));
    }

    private void updateBackgroundColor(IPreferenceStore store, IVerticalRulerColumn column) {
        // background color: same as editor, or system default
        RGB rgb;
        if (store.getBoolean(USE_DEFAULT_BG_KEY))
            rgb = null;
        else
            rgb = getColorFromStore(store, BG_COLOR_KEY);
        ISharedTextColors sharedColors = getSharedColors();
        if (column instanceof STRulerColumn)
            ((STRulerColumn) column).setBackground(sharedColors.getColor(rgb));
    }

    private void updateChangedColor(AnnotationPreference pref, IPreferenceStore store, STChangeRulerColumn column) {
        if (pref != null && column != null) {
            RGB rgb = getColorFromAnnotationPreference(store, pref);
            column.setChangedColor(getSharedColors().getColor(rgb));
        }
    }

    private void updateAddedColor(AnnotationPreference pref, IPreferenceStore store, STChangeRulerColumn column) {
        if (pref != null && column != null) {
            RGB rgb = getColorFromAnnotationPreference(store, pref);
            column.setAddedColor(getSharedColors().getColor(rgb));
        }
    }

    private void updateDeletedColor(AnnotationPreference pref, IPreferenceStore store, STChangeRulerColumn column) {
        if (pref != null && column != null) {
            RGB rgb = getColorFromAnnotationPreference(store, pref);
            column.setDeletedColor(getSharedColors().getColor(rgb));
        }
    }

    private void updateCharacterMode(IPreferenceStore store, STChangeRulerColumn column) {
        if (column != null) {
            column.setDisplayMode(store
                    .getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
        }
    }

    private void updateLineNumbersVisibility(STChangeRulerColumn column) {
        if (column != null) {
            column.showLineNumbers(getLineNumberPreference());
        }
    }

    private void updateRevisionRenderingMode(IPreferenceStore store, STChangeRulerColumn column) {
        if (column != null) {
            String option = store
                    .getString(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_RENDERING_MODE);
            RenderingMode[] modes = { IRevisionRulerColumnExtension.AUTHOR, IRevisionRulerColumnExtension.AGE,
                    IRevisionRulerColumnExtension.AUTHOR_SHADED_BY_AGE };
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].name().equals(option)) {
                    ((IRevisionRulerColumnExtension) column).setRevisionRenderingMode(modes[i]);
                    return;
                }
            }
        }
    }

    private void updateRevisionAuthorVisibility(IPreferenceStore store, STChangeRulerColumn column) {
        if (column != null) {
            boolean show = store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_AUTHOR);
            column.showRevisionAuthor(show);
        }
    }

    private void updateRevisionIdVisibility(IPreferenceStore store, STChangeRulerColumn column) {
        if (column != null) {
            boolean show = store
                    .getBoolean(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_REVISION);
            column.showRevisionId(show);
        }
    }

    private void updateQuickDiffVisibility(STChangeRulerColumn column) {
        boolean show = getQuickDiffPreference();
        if (show == isShowingChangeInformation())
            return;

        if (show) {
            installChangeRulerModel(column);
        } else {
            uninstallChangeRulerModel(column);
        }
    }

    /**
     * Returns whether the line number ruler column should be visible according to the preference store settings.
     * Subclasses may override this method to provide a custom preference setting.
     *
     * @return <code>true</code> if the line numbers should be visible
     */
    private boolean getLineNumberPreference() {
        IPreferenceStore store = getPreferenceStore();
        return store != null ? store.getBoolean(STAnnotatedCSourceEditor.ST_RULER) : false;
    }

    /**
     * Returns whether quick diff info should be visible upon opening an editor according to the preference store
     * settings.
     *
     * @return <code>true</code> if the line numbers should be visible
     */
    private boolean getQuickDiffPreference() {
        IPreferenceStore store = getPreferenceStore();
        boolean setting = store != null ? store
                .getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON) : false;
        if (!setting)
            return false;

        // The column is used only in STAnnotatedCSourceEditor hence it can be safely assumed that we get ITextEditorExtension2
        ITextEditorExtension2 editor = (ITextEditorExtension2)getEditor();
        return editor.isEditorInputModifiable();
    }

    /**
     * Extracts the color preference for the given preference from the given store. If the given store indicates that
     * the default value is to be used, or the value stored in the preferences store is <code>null</code>, the value is
     * taken from the <code>AnnotationPreference</code>'s default color value.
     * <p>
     * The return value is
     * </p>
     *
     * @param store
     *            the preference store
     * @param pref
     *            the annotation preference
     * @return the RGB color preference, not <code>null</code>
     */
    private static RGB getColorFromAnnotationPreference(IPreferenceStore store, AnnotationPreference pref) {
        String key = pref.getColorPreferenceKey();
        RGB rgb = null;
        if (store.contains(key)) {
            if (store.isDefault(key))
                rgb = pref.getColorPreferenceValue();
            else
                rgb = PreferenceConverter.getColor(store, key);
        }
        if (rgb == null)
            rgb = pref.getColorPreferenceValue();
        return rgb;
    }

    private static RGB getColorFromStore(IPreferenceStore store, String key) {
        RGB rgb = null;
        if (store.contains(key)) {
            if (store.isDefault(key))
                rgb = PreferenceConverter.getDefaultColor(store, key);
            else
                rgb = PreferenceConverter.getColor(store, key);
        }
        return rgb;
    }

    /**
     * Ensures that quick diff information is displayed and the quick diff provider is the one with the specified id. If
     * a different quick diff provider is in use, the user may be asked whether he wants to switch.
     *
     * @param diffProviderId
     *            the quick diff provider id to use
     * @return <code>true</code> if quick diff could be enabled for the given id, <code>false</code> otherwise
     */
    private boolean ensureQuickDiffProvider(String diffProviderId) {
        if (!isShowingChangeInformation())
            installChangeRulerModel(fDelegate); // FIXME pass provider id

        IAnnotationModel annotationModel = fViewer.getAnnotationModel();
        IAnnotationModel oldDiffer = getDiffer();
        if (oldDiffer == null && annotationModel != null)
            return false; // quick diff is enabled, but no differ? not working for whatever reason

        if (annotationModel == null)
            annotationModel = new AnnotationModel();
        if (!(annotationModel instanceof IAnnotationModelExtension))
            return false;

        QuickDiff util = new QuickDiff();
        Object oldDifferId = util.getConfiguredQuickDiffProvider(oldDiffer);
        if (oldDifferId.equals(diffProviderId)) {
            if (oldDiffer instanceof ILineDifferExtension)
                ((ILineDifferExtension) oldDiffer).resume();
            return true;
        }

        // Check whether the desired provider is available at all
        IAnnotationModel newDiffer = util.createQuickDiffAnnotationModel(getEditor(), diffProviderId);
        if (util.getConfiguredQuickDiffProvider(newDiffer).equals(oldDifferId)) {
            if (oldDiffer instanceof ILineDifferExtension)
                ((ILineDifferExtension) oldDiffer).resume();
            return true;
        }

        // quick diff is showing with the wrong provider - ask the user whether he wants to switch
        IPreferenceStore store = EditorsUI.getPreferenceStore();
        if (oldDiffer != null
                && !store.getString(REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY).equals(MessageDialogWithToggle.ALWAYS)) {
            MessageDialogWithToggle toggleDialog = MessageDialogWithToggle.openOkCancelConfirm(fViewer.getTextWidget()
                    .getShell(), STRulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_title,
                    STRulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_message,
                    STRulerMessages.AbstractDecoratedTextEditor_revision_quickdiff_switch_rememberquestion, true,
                    store, REVISION_ASK_BEFORE_QUICKDIFF_SWITCH_KEY);
            if (toggleDialog.getReturnCode() != Window.OK)
                return false;
        }

        IAnnotationModelExtension modelExtension = (IAnnotationModelExtension) annotationModel;
        modelExtension.removeAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);

        modelExtension.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, newDiffer);

        if (fDelegate != null) {
            fDelegate.setModel(annotationModel); // picks up the new model attachment
        }

        return true;
    }

    /**
     * Installs the differ annotation model with the current quick diff display.
     *
     * @param column
     *            the column to install the model on
     */
    private void installChangeRulerModel(STChangeRulerColumn column) {
        if (column != null) {
            IAnnotationModel model = getAnnotationModelWithDiffer();
            ((IChangeRulerColumn) column).setModel(model);
            if (model != null) {
                ISourceViewer viewer = fViewer;
                if (viewer != null && viewer.getAnnotationModel() == null)
                    viewer.showAnnotations(true);
            }
        }
    }

    /**
     * Uninstalls the differ annotation model from the current quick diff display.
     *
     * @param column
     *            the column to remove the model from
     */
    private void uninstallChangeRulerModel(IVerticalRulerColumn column) {
        if (column instanceof IChangeRulerColumn)
            ((IChangeRulerColumn) column).setModel(null);
        IAnnotationModel model = getDiffer();
        if (model instanceof ILineDifferExtension)
            ((ILineDifferExtension) model).suspend();

        ISourceViewer viewer = fViewer;
        if (viewer != null && viewer.getAnnotationModel() == null)
            viewer.showAnnotations(false);
    }

    /**
     * Returns the annotation model that contains the quick diff annotation model.
     * <p>
     * Extracts the line differ from the displayed document's annotation model. If none can be found, a new differ is
     * created and attached to the annotation model.
     * </p>
     *
     * @return the annotation model that contains the line differ, or <code>null</code> if none could be found or
     *         created
     * @see IChangeRulerColumn#QUICK_DIFF_MODEL_ID
     */
    private IAnnotationModel getAnnotationModelWithDiffer() {
        ISourceViewer viewer = fViewer;
        if (viewer == null)
            return null;

        IAnnotationModel m = viewer.getAnnotationModel();
        IAnnotationModelExtension model = null;
        if (m instanceof IAnnotationModelExtension)
            model = (IAnnotationModelExtension) m;

        IAnnotationModel differ = getDiffer();
        // create diff model if it doesn't
        if (differ == null) {
            IPreferenceStore store = getPreferenceStore();
            if (store != null) {
                String defaultId = store
                        .getString(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
                differ = new QuickDiff().createQuickDiffAnnotationModel(getEditor(), defaultId);
                if (differ != null) {
                    if (model == null)
                        model = new AnnotationModel();
                    model.addAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID, differ);
                }
            }
        } else if (differ instanceof ILineDifferExtension2) {
            if (((ILineDifferExtension2) differ).isSuspended())
                ((ILineDifferExtension) differ).resume();
        } else if (differ instanceof ILineDifferExtension) {
            ((ILineDifferExtension) differ).resume();
        }

        return (IAnnotationModel) model;
    }

    /**
     * Extracts the line differ from the displayed document's annotation model. If none can be found, <code>null</code>
     * is returned.
     *
     * @return the line differ, or <code>null</code> if none could be found
     */
    private IAnnotationModel getDiffer() {
        // get annotation model extension
        ISourceViewer viewer = fViewer;
        if (viewer == null) {
            return null;
        }

        IAnnotationModel m = viewer.getAnnotationModel();
        if (m == null && fDelegate != null) {
            m = fDelegate.getModel();
        }

        if (!(m instanceof IAnnotationModelExtension))
            return null;

        IAnnotationModelExtension model = (IAnnotationModelExtension) m;

        // get diff model if it exists already
        return model.getAnnotationModel(IChangeRulerColumn.QUICK_DIFF_MODEL_ID);
    }

    /**
     * Sets the STRulerColumn. Used by {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditor} to maintain the contract of its
     * {@link org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createLineNumberRulerColumn} method.
     *
     * @param rulerColumn The ruler column.
     */
    public void setSTColumn(STChangeRulerColumn rulerColumn) {
        fDelegate = rulerColumn;
    }

    /**
     * Initializes the given line number ruler column from the preference store.
     *
     * @param rulerColumn
     *            the ruler column to be initialized
     */
    public void initializeSTRulerColumn(STChangeRulerColumn rulerColumn) {
        IPreferenceStore store = getPreferenceStore();
        if (store != null) {
            updateForegroundColor(store, rulerColumn);
            updateBackgroundColor(store, rulerColumn);
            updateLineNumbersVisibility(rulerColumn);
            rulerColumn.redraw();
        }
    }

    /**
     * Returns <code>true</code> if the ruler is showing line numbers, <code>false</code> if it is only showing change
     * information.
     *
     * @return <code>true</code> if line numbers are shown, <code>false</code> otherwise
     */
    public boolean isShowingSTRuler() {
        boolean b = fDelegate != null && fDelegate.isShowingSTRuler();
        return b;
    }

    /**
     * Returns <code>true</code> if the ruler is showing change information, <code>false</code> if it is only showing
     * line numbers.
     *
     * @return <code>true</code> if change information is shown, <code>false</code> otherwise
     */
    public boolean isShowingChangeInformation() {
		return fDelegate != null && fDelegate.isShowingChangeInformation();
    }

    /**
     * Shows revision information on the receiver.
     *
     * @param info
     *            the revision information to show
     * @param quickDiffProviderId
     *            the id of the corresponding quick diff provider
     */
    public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId) {
        if (!ensureQuickDiffProvider(quickDiffProviderId)) {
            return;
        }

        if (fDelegate != null) {
            fDelegate.setRevisionInformation(info);
        }
    }

    /**
     * Hides revision information.
     */
    public void hideRevisionInformation() {
        if (fDelegate != null) {
            fDelegate.setRevisionInformation(null);
        }
    }

    /**
     * Returns <code>true</code> if the ruler is showing revision information, <code>false</code> if it is only showing
     * line numbers.
     *
     * @return <code>true</code> if revision information is shown, <code>false</code> otherwise
     */
    public boolean isShowingRevisionInformation() {
        if (fDelegate != null) {
        	return fDelegate.isShowingRevisionInformation();
        }
        return false;
    }

    /**
     * Returns the selection provider of the revision column, <code>null</code> if none is available.
     *
     * @return the revision selection provider
     */
    public ISelectionProvider getRevisionSelectionProvider() {
        if (fDelegate != null) {
            return fDelegate.getRevisionSelectionProvider();
        }
        return null;
    }

}
