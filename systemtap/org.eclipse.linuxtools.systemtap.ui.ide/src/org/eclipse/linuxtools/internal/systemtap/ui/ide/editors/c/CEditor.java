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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.NewFileAction;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
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
	private RulerDoubleClickHandler handler = new RulerDoubleClickHandler();
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor";
	
	/**
	 * The <code>RulerDoubleClickHandler</code> handles double click events on the
	 * ruler for this text editor. It first checks to see if the user clicked on a 
	 * comment line, then if they clicked on a line that SystemTap can use as a probe
	 * point, and if the line of code passes both checks, it dispatches an event to the
	 * active STPEditor to insert a code block describing the line of code that the user
	 * clicked on.
	 * 
	 * The block of code is sent to the STPEditor only under the following circumstances:
	 * <ul>
	 * 	<li>The line of code is not blank</li>
	 * 	<li>If the line of code contains a single-line comment, it must not be the only text on that line</li>
	 * 	<li>The line of code must not fall within a multiline comment</li>
	 * 	<li>The line of code must be a line that can be used by SystemTap, 
	 * 	determined by running the following:<br/>
	 * 		<code>stap -p2 -e 'probe kernel.statement("*@filename:linenumber")'</code><br/>
	 * 		If <code>stap</code> does not generate errors while running the test command, the
	 * 		line is assumed valid.</li>
	 * </ul>
	 * 
	 * If all of the above are met, the active STPEditor listed in <code>IDESessionSettings</code>
	 * is told to insert a template probe for this line of code using the <code>SimpleEditor.insertText</code> method.
	 * If no the returned STPEditor reference is null, the code opens a new editor.
	 * 
	 * @author Henry Hughes
	 * @author Ryan Morse
	 * @see org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor#insertText
	 * @see org.eclipse.linuxtools.systemtap.ui.structures.runnable.Command
	 * @see org.eclipse.swt.events.MouseListener
	 */
	private class RulerDoubleClickHandler implements MouseListener
	{
		/**
		 * The doubleclick event handler method.
		 * @param	e	The <code>MouseEvent</code> that represents this doubleclick event.
		 */
		public void mouseDoubleClick(MouseEvent e) 
		{
			LogManager.logDebug("Start mouseDoubleClick: e-" + e, this); //$NON-NLS-1$
			getSite().getShell().setCursor(new Cursor(getSite().getShell().getDisplay(), SWT.CURSOR_WAIT));
			int lineno = getVerticalRuler().getLineOfLastMouseButtonActivity();

			String s = getSourceViewer().getDocument().get();
			String[] lines = s.split("\n");
			String line = lines[lineno].trim();
			boolean die = false;
			if("".equals(line))		//eat blank lines
			   die = true;
			if(line.startsWith("#"))	//eat preprocessor directives
				die = true;
			if(line.startsWith("//"))	//eat C99 comments
				die = true;
			if(line.startsWith("/*") && !line.contains("*/") && !line.endsWith("*/"))	//try to eat single-line C comments
				die = true;
			
			//gogo find comment segments
			try
			{
				ArrayList<Integer> commentChunks = new ArrayList<Integer>();
				char[] chars = s.toCharArray();
				int needle = 1;
				int offset = getSourceViewer().getDocument().getLineOffset(lineno);
				while (needle < chars.length)
				{
					if(chars[needle-1] == '/' && chars[needle] == '*')
					{
						commentChunks.add(new Integer(needle));
						while(needle < chars.length)
						{
							if(chars[needle-1] == '*' && chars[needle] == '/')
							{
								commentChunks.add(new Integer(needle));
								needle++;
								break;
							}
							needle++;
						}
					}
					needle++;
				}
				for(int i=0, pair, start, end; i < commentChunks.size(); i++)
				{
					if(!(((Integer)(commentChunks.get(i))).intValue() < offset))
					{
						pair = i - i%2;
						start = ((Integer)(commentChunks.get(pair))).intValue();
						end = ((Integer)(commentChunks.get(pair+1))).intValue();
						if(offset >= start && offset <= end)
							die=true;
					}
				}
			} catch (Exception excp) {
				LogManager.logCritical("Exception mouseDoubleClick: " + excp.getMessage(), this); //$NON-NLS-1$
			}
			if(die) {
				LogManager.logInfo("Initializing", MessageDialog.class); //$NON-NLS-1$
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Localization.getString("CEditor.ProbeInsertFailed"),Localization.getString("CEditor.CanNotProbeLine")); //$NON-NLS-1$ //$NON-NLS-2$
				LogManager.logInfo("Disposing", MessageDialog.class); //$NON-NLS-1$
			} else {
				IEditorInput in = getEditorInput();
				if(in instanceof FileStoreEditorInput) {
					FileStoreEditorInput input = (FileStoreEditorInput)in;
	
					IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
					String kernroot = p.getString(IDEPreferenceConstants.P_KERNEL_SOURCE);
	
					String filepath = input.getURI().getPath();
					String kernrelative = filepath.substring(kernroot.length()+1, filepath.length());
					StringBuffer sb = new StringBuffer();
					
					sb.append("probe kernel.statement(\"*@"+ kernrelative + ":" + (lineno+1) + "\")");

				/*	if(!checkProbe(sb.toString() + "{ }")) {
						LogManager.logInfo("Initializing", MessageDialog.class);
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								Localization.getString("CEditor.ProbeInsertFailed"),Localization.getString("CEditor.CanNotProbeLine"));
						LogManager.logInfo("Disposing", MessageDialog.class);
					} else { */
						sb.append("\n{\n\t\n}\n");
						if(null == IDESessionSettings.activeSTPEditor) {
							NewFileAction action = new NewFileAction();
							//action.init(input.getMainWindow());
							action.run();
							IEditorPart ed = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
							if(ed instanceof STPEditor)
								IDESessionSettings.activeSTPEditor = (STPEditor)ed;
						}
						STPEditor editor = IDESessionSettings.activeSTPEditor;
						if(null != editor)
							editor.insertText(sb.toString());
					//}
				}
			}
			getSite().getShell().setCursor(null);	//Return the cursor to normal
			LogManager.logDebug("End mouseDoubleClick:", this); //$NON-NLS-1$
		}

		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
		}
	}
	
	/**
	 * Default Constructor for the <code>CEditor</code> class. Creates an instance of the editor which
	 * is not associated with any given input. 
	 */
	public CEditor() {
		super();
		LogManager.logDebug("Start CEditor:", this); //$NON-NLS-1$
		internal_init();
		IDEPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(cColorPropertyChangeListener);
		LogManager.logDebug("End CEditor:", this); //$NON-NLS-1$
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
		LogManager.logDebug("Start internal_init:", this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
		configureInsertMode(SMART_INSERT, false);
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new CConfiguration(colorManager));
		setDocumentProvider(new CDocumentProvider());
		LogManager.logDebug("End internal_init", this); //$NON-NLS-1$
	}
	
	public void dispose() {
		LogManager.logDebug("Start dispose:", this); //$NON-NLS-1$
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		IDEPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(cColorPropertyChangeListener);
		colorManager.dispose();
		super.dispose();
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
	}
	
	protected CompositeRuler createCompositeRuler() {
		LogManager.logDebug("Start createCompositeRuler:", this); //$NON-NLS-1$
		CompositeRuler ruler = new CompositeRuler();
		AnnotationRulerColumn column = new AnnotationRulerColumn(VERTICAL_RULER_WIDTH, getAnnotationAccess());
		ruler.addDecorator(0, column);

		if (isLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		else if (isPrefQuickDiffAlwaysOn())
			ruler.addDecorator(1, createLineNumberRulerColumn());

		LogManager.logDebug("End createCompositeRuler: returnVal-" + ruler, this); //$NON-NLS-1$
		return ruler;
	}

	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
		super.createPartControl(parent);
		IVerticalRuler ruler = this.getVerticalRuler();
		Control control = ruler.getControl();
		try {
			control.addMouseListener(handler);
		} catch(Exception e) {
			LogManager.logCritical("Exception createPartControl: " + e.getMessage(), this); //$NON-NLS-1$
		}
		LogManager.logDebug("End createPartControl:", this); //$NON-NLS-1$
	}
	
	/**
	 * Color Preference Change Notification method, called whenever the user has changed preferences
	 * regarding syntax highlighing. This method notifies its internal structures (<code>CScanner</code>,
	 * <code>CConfiguration</code>) that the preferences have changed, and that they need to reconfigure
	 * themselves.
	 */
	private void notifyColorPrefsChanged()
	{
		LogManager.logDebug("Start notifyColorPrefsChanged:", this); //$NON-NLS-1$
		SourceViewerConfiguration svc = getSourceViewerConfiguration();
		if(!(svc instanceof CConfiguration)) {
			LogManager.logDebug("End notifyColorPrefsChanged:", this); //$NON-NLS-1$
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
		LogManager.logDebug("End notifyColorPrefsChanged:", this); //$NON-NLS-1$
	}
	
	/**
	 * Detects changes in the preferences relating to the C Editor's syntax highlighting, and
	 * fires the <code>notifyColorPrefsChanged</code> method when a change has occured.
	 * @see #notifyColorPrefsChanged() 
	 */
	private final IPropertyChangeListener cColorPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			LogManager.logDebug("Start propertyChange: event-" + event, this); //$NON-NLS-1$
			if(event.getProperty().equals(IDEPreferenceConstants.P_C_COMMENT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_DEFAULT_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_KEYWORD_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_PREPROCESSOR_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_STRING_COLOR) ||
			   event.getProperty().equals(IDEPreferenceConstants.P_C_TYPE_COLOR)) {
				notifyColorPrefsChanged();
			}
			LogManager.logDebug("End propertyChange: event-" + event, this); //$NON-NLS-1$
		}
	};
}
