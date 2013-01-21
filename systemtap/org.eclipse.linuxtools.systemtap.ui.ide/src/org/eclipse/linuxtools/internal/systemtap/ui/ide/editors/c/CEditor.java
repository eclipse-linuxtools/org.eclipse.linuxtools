/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c;

import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.RulerDoubleClickHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;


/**
 * A text editor for the C language.
 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class CEditor extends AbstractDecoratedTextEditor {
	private ColorManager colorManager;
	/**
	 * The handler for doubleclick events on the ruler for this text editor.
	 */
	private RulerDoubleClickHandler handler;
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor"; //$NON-NLS-1$



	/**
	 * Default Constructor for the <code>CEditor</code> class. Creates an instance of the editor which
	 * is not associated with any given input.
	 */
	public CEditor() {
		super();
		handler = new RulerDoubleClickHandler(this);
		internal_init();
		IDEPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(cColorPropertyChangeListener);
	}
	/**
	 * Part of the initialization routine. Creates the <code>ColorManager</code> used by this editor,
	 * sets up the CConfiguration for this editor, and sets the DocumentProvider to a new
	 * <code>CDocumentProvider</code>.
	 * @see org.eclipse.linuxtools.systemtap.ui.editor.ColorManager
	 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CConfiguration
	 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CDocumentProvider
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setDocumentProvider(org.eclipse.ui.texteditor.IDocumentProvider)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setSourceViewerConfiguration(org.eclipse.jface.text.source.SourceViewerConfiguration)
	 */
	protected void internal_init() {
		configureInsertMode(SMART_INSERT, false);
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new CConfiguration(colorManager));
		setDocumentProvider(new CDocumentProvider());
	}

	@Override
	public void dispose() {
		IDEPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(cColorPropertyChangeListener);
		colorManager.dispose();
		super.dispose();
	}

	@Override
	protected CompositeRuler createCompositeRuler() {
		CompositeRuler ruler = new CompositeRuler();
		AnnotationRulerColumn column = new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, getAnnotationAccess());
		ruler.addDecorator(0, column);

		if (isLineNumberRulerVisible()) {
			ruler.addDecorator(1, createLineNumberRulerColumn());
		} else if (isPrefQuickDiffAlwaysOn()) {
			ruler.addDecorator(1, createLineNumberRulerColumn());
		}

		return ruler;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		IVerticalRuler ruler = this.getVerticalRuler();
		Control control = ruler.getControl();
		control.addMouseListener(handler);
	}

	/**
	 * Color Preference Change Notification method, called whenever the user has changed preferences
	 * regarding syntax highlighing. This method notifies its internal structures (<code>CScanner</code>,
	 * <code>CConfiguration</code>) that the preferences have changed, and that they need to reconfigure
	 * themselves.
	 */
	private void notifyColorPrefsChanged()
	{
		SourceViewerConfiguration svc = getSourceViewerConfiguration();
		if(!(svc instanceof CConfiguration)) {
			return;
		}
		CConfiguration config = (CConfiguration)svc;
		CScanner scanner = config.getCScanner();
		scanner.initializeScanner();

		SourceViewer viewer = (SourceViewer)getSourceViewer();
		viewer.unconfigure();
		viewer.configure(svc);
		viewer.invalidateTextPresentation();
		viewer.refresh();
	}

	/**
	 * Detects changes in the preferences relating to the C Editor's syntax highlighting, and
	 * fires the <code>notifyColorPrefsChanged</code> method when a change has occured.
	 * @see #notifyColorPrefsChanged()
	 */
	private final IPropertyChangeListener cColorPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(IDEPreferenceConstants.P_C_COMMENT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_DEFAULT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_KEYWORD_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_PREPROCESSOR_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_STRING_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_TYPE_COLOR)) {
				notifyColorPrefsChanged();
			}
		}
	};
}
