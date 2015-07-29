/*******************************************************************************
 * Copyright (c) 2007, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.URLHyperlinkWithMacroDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileChangelogScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePackagesScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileScanner;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.HyperlinkDetectorDescriptor;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;

public class SpecfileConfiguration extends TextSourceViewerConfiguration {
    private SpecfileDoubleClickStrategy doubleClickStrategy;
    private SpecfileScanner scanner;
    private SpecfileChangelogScanner changelogScanner;
    private SpecfilePackagesScanner packagesScanner;
    private ColorManager colorManager;
    private SpecfileHover specfileHover;
    private SpecfileEditor editor;

    public SpecfileConfiguration(ColorManager colorManager, SpecfileEditor editor) {
        super();
        this.colorManager = colorManager;
        this.editor = editor;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return SpecfilePartitionScanner.SPEC_PARTITION_TYPES;
    }

    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(
        ISourceViewer sourceViewer,
        String contentType) {
        if (doubleClickStrategy == null) {
            doubleClickStrategy = new SpecfileDoubleClickStrategy();
        }
        return doubleClickStrategy;
    }

    protected SpecfileScanner getSpecfileScanner() {
        if (scanner == null) {
            scanner = new SpecfileScanner(colorManager);
            scanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(
                        colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
        }
        return scanner;
    }

    protected SpecfileChangelogScanner getSpecfileChangelogScanner() {
        if  (changelogScanner == null) {
            changelogScanner = new SpecfileChangelogScanner(colorManager);
            changelogScanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(
                        colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
        }
        return changelogScanner;
    }

    protected SpecfilePackagesScanner getSpecfilePackagesScanner() {
        if  (packagesScanner == null) {
            packagesScanner = new SpecfilePackagesScanner(colorManager);
            packagesScanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(
                        colorManager.getColor(ISpecfileColorConstants.DEFAULT))));
        }
        return packagesScanner;
    }


    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        if (specfileHover == null) {
            specfileHover = new SpecfileHover(this.editor);
        }
        return specfileHover;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSpecfileScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getSpecfilePackagesScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_PACKAGES);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_PACKAGES);

        dr = new DefaultDamagerRepairer(getSpecfileScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_PREP);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_PREP);

        dr = new DefaultDamagerRepairer(getSpecfileScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_SCRIPT);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_SCRIPT);

        dr = new DefaultDamagerRepairer(getSpecfileScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_FILES);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_FILES);

        dr = new DefaultDamagerRepairer(getSpecfileScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_GROUP);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_GROUP);

        dr = new DefaultDamagerRepairer(getSpecfileChangelogScanner());
        reconciler.setDamager(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);
        reconciler.setRepairer(dr, SpecfilePartitionScanner.SPEC_CHANGELOG);

        return reconciler;
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        if (editor != null && editor.isEditable()) {
            SpecfileReconcilingStrategy strategy= new SpecfileReconcilingStrategy(editor);
            MonoReconciler reconciler= new MonoReconciler(strategy, false);
            reconciler.setProgressMonitor(new NullProgressMonitor());
            reconciler.setDelay(500);
            return reconciler;
        }
        return null;
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant= new ContentAssistant();
        IContentAssistProcessor processor= new SpecfileCompletionProcessor(editor);
        // add content assistance to all the supported contentType
        assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_PREP);
        assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_SCRIPT);
        assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_FILES);
        assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_CHANGELOG);
        assistant.setContentAssistProcessor(processor,SpecfilePartitionScanner.SPEC_PACKAGES);
        assistant.setContentAssistProcessor(processor,
                SpecfilePartitionScanner.SPEC_GROUP);
        // configure content assistance
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        IInformationControlCreator controlCreator= getInformationControlCreator();
        assistant.setInformationControlCreator(controlCreator);
        assistant.enableAutoInsert(true);
        assistant.setStatusLineVisible(true);
        assistant.setStatusMessage(Messages.SpecfileConfiguration_0);
        return assistant;
    }

    private IInformationControlCreator getInformationControlCreator() {
        return new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, false);
            }
        };
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        if (sourceViewer == null) {
            return null;
        }
        Map<?, ?> targets = getHyperlinkDetectorTargets(sourceViewer);
        HyperlinkDetectorRegistry hlDetectorRegistry = EditorsUI.getHyperlinkDetectorRegistry();
        HyperlinkDetectorDescriptor[] hlDetectorDescriptor = hlDetectorRegistry.getHyperlinkDetectorDescriptors();
        List<IHyperlinkDetector> tempHDList = new ArrayList<>();

        for (Map.Entry<?, ?> entry : targets.entrySet()) {
            for (HyperlinkDetectorDescriptor hdd : hlDetectorDescriptor) {
                try {
                    AbstractHyperlinkDetector ahld = (AbstractHyperlinkDetector) hdd.createHyperlinkDetectorImplementation();
                    // filter using target id and not instance of URLHyperlinkDetector
                    // so that an option to open url with unresolved macros won't show
                    // however, allow URLHyperlinkWithMacroDetector
                    if (hdd.getTargetId().equals(entry.getKey()) &&
                            (!(ahld instanceof URLHyperlinkDetector) || ahld instanceof URLHyperlinkWithMacroDetector)) {
                        ahld.setContext((IAdaptable)entry.getValue());
                        tempHDList.add(ahld);
                    }
                } catch (CoreException e) {
                    SpecfileLog.logError(e);
                }
            }
        }

        if (!tempHDList.isEmpty()) {
            return tempHDList.toArray(new IHyperlinkDetector[tempHDList.size()]);
        } else {
            return null;
        }
    }

    @Override
    protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
        Map<String, Object> targets= super.getHyperlinkDetectorTargets(sourceViewer);
        targets.put("org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditorTarget", editor); //$NON-NLS-1$
        targets.put("org.eclipse.ui.DefaultTextEditor", editor); //$NON-NLS-1$
        return targets;
    }

    private int getTabSize() {
        return Activator.getDefault().getPreferenceStore().getInt(
                PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB);
    }

    private boolean isTabConversionEnabled() {
        return Activator.getDefault().getPreferenceStore().getBoolean(
                PreferenceConstants.P_SPACES_FOR_TABS);
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(
            ISourceViewer sourceViewer, String contentType) {
        if (isTabConversionEnabled()) {
            TabsToSpacesConverter tabsConverter = new TabsToSpacesConverter();
            tabsConverter.setLineTracker(new DefaultLineTracker());
            tabsConverter.setNumberOfSpacesPerTab(getTabSize());
            return new IAutoEditStrategy[] { tabsConverter };
        }
        return null;
    }

}