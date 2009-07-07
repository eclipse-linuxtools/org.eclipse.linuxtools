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

package org.eclipse.linuxtools.systemtap.ui.ide.editors.stp;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;




public class STPEditor extends SimpleEditor {
	private ColorManager colorManager;
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.ide.editors.stp.STPEditor";

	public STPEditor() {
		super();
		LogManager.logDebug("Start STPEditor:", this);
		IDEPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(stpColorPropertyChangeListener);
		LogManager.logDebug("End STPEditor:", this);
	}
	
	/**
	 * Starts the initialization functions necessary including insert mode, color manage, source viewer
	 * configuration, and document provider.
	 */
	protected void internal_init() {
		LogManager.logDebug("Start internal_init:", this);
		LogManager.logInfo("Initializing", this);
		configureInsertMode(SMART_INSERT, false);
		colorManager = new ColorManager();

		setSourceViewerConfiguration(new STPConfiguration(colorManager)); 
		setDocumentProvider(new STPDocumentProvider());
		LogManager.logDebug("End internal_init:", this);
	}
		
	protected LineNumberRulerColumn lineNumberRulerColumn;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		updateRulerState();
	}

	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler = new CompositeRuler();
		return ruler;
	}
	
	/**
	 * This is all to allow displaying the LineNumberRuler.
	 */
	private void showLineNumberRuler() {
		if (null == lineNumberRulerColumn) {
			IVerticalRuler v = getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c = (CompositeRuler) v;
				c.addDecorator(1, createLineNumberRulerColumn());
			}
		}
		lineNumberRulerColumn.redraw();
	}
	
	private void hideLineNumberRuler() {
		if (null != lineNumberRulerColumn) {
			IVerticalRuler v= getVerticalRuler();
			if (v instanceof CompositeRuler) {
				CompositeRuler c= (CompositeRuler) v;
				c.removeDecorator(lineNumberRulerColumn);
			}
			lineNumberRulerColumn = null;
		}
	}
	
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		lineNumberRulerColumn = new LineNumberRulerColumn();
		initializeLineNumberRulerColumn(lineNumberRulerColumn);
		return lineNumberRulerColumn;
	}
	
	protected void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		rulerColumn.setForeground(new Color(PlatformUI.getWorkbench().getDisplay(), new RGB(0, 0, 0)));
		rulerColumn.setBackground(new Color(PlatformUI.getWorkbench().getDisplay(), new RGB(255, 255, 255)));
		rulerColumn.redraw();
	}

	public void dispose() {
		LogManager.logDebug("Start dispose:", this);
		LogManager.logInfo("Disposing", this);
		colorManager.dispose();
		IDEPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(stpColorPropertyChangeListener);
		super.dispose();
		LogManager.logDebug("End dispose:", this);
	}

	public void update()
	{
//		LogManager.logDebug("Start update:", this);
//		IDocument doc = getSourceViewer().getDocument();
//		String s = doc.get();
//		IViewReference[] ivf = SystemtapPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
//		IViewPart ivp;
//		for(int i=0; i<ivf.length; i++) {
//			ivp = ivf[i].getView(false);
//			if(ivp instanceof FunctionBrowserView) {
//				FunctionBrowserView view = (FunctionBrowserView)ivp;
//				String[] tapsets = view.getTapsets();
//				StapParser parser = new StapParser();
//				parser.parseScript(tapsets, s);
//				view.addLocalFunctions(parser.getFunctions2());
//				break;
//			}
//		}
//		LogManager.logDebug("End update:", this);
	}

	/**
	 * Modifies viewer to accomodate changed color states.
	 */
	private void updateColorState() {
		LogManager.logDebug("Start notifyColorPrefsChanged:", this);
		SourceViewerConfiguration svc = getSourceViewerConfiguration();

		if(svc instanceof STPConfiguration) {
			((STPConfiguration)svc).getSTPScanner().initializeScanner();
			
			SourceViewer viewer = (SourceViewer)getSourceViewer();
			viewer.unconfigure();
			viewer.configure(svc);
			viewer.invalidateTextPresentation();
			viewer.refresh();
		}
		LogManager.logDebug("End notifyColorPrefsChanged:", this);
	}
	
	/**
	 * Hides or shows line numbers depending on the state of the preference.
	 */
	private void updateRulerState() {
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();
		if(store.getBoolean(IDEPreferenceConstants.P_SHOW_LINE_NUMBERS))
			showLineNumberRuler();
		else
			hideLineNumberRuler();
	}
	
	/**
	 * Initiates updates to color states based on color preferences changing.
	 */
	private final IPropertyChangeListener stpColorPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			LogManager.logDebug("Start propertyChange: event-" + event, this);
			if(event.getProperty().equals(IDEPreferenceConstants.P_STP_COMMENT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_DEFAULT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_EMBEDDED_C_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_EMBEDDED_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_KEYWORD_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_STRING_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_STP_TYPE_COLOR)) {
				updateColorState();
			} else if(event.getProperty().equals(IDEPreferenceConstants.P_SHOW_LINE_NUMBERS)) {
				updateRulerState();
			}
			LogManager.logDebug("End propertyChange:", this);
		}
	};
}
