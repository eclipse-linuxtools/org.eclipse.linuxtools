/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.localgui.graphing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.localgui.core.Helper;
import org.eclipse.linuxtools.systemtap.localgui.core.Messages;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapErrorHandler;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;

/**
 *	The SystemTap View for displaying output of the 'stap' command, and acts
 *	as a container for any graph to be rendered. Any buttons/controls/actions
 *	necessary to the smooth running of SystemTap could be placed here.
 */
public class SystemTapView extends ViewPart {
	private static final String NEW_LINE = Messages.getString("SystemTapView.3"); //$NON-NLS-1$
	private static SystemTapView stapview;
	private static boolean isInitialized = false;
	
	private Display display;
	private static StyledText viewer;
	private int previousEnd;
	
	private Action killSystemTapScript;
	private Action checkSystemTapVersion;
	private Action open_callgraph;
	private Action disposeGraph;

	private static Action save_callgraph;
	private static Action open_default;
	private static Action error_errorLog;
	private static Action error_deleteError;
	private static Action view_treeview;
	private static Action view_radialview;
	private static Action view_aggregateview;
	private static Action view_boxview;	
	private static Action view_refresh;	
	private static Action animation_slow;
	private static Action animation_fast;
	private static Action mode_collapsednodes;
	private static Action markers_next; 
	private static Action markers_previous;
	private static Action limits; 
	private static Action goto_next;
	private static Action goto_previous;
	private static Action goto_last;
	
	private static IMenuManager menu;
	private static IMenuManager gotoMenu;
	private static IMenuManager file;
	private static IMenuManager errors;
	private static IMenuManager view;
	private static IMenuManager animation;
	private static IMenuManager markers; //Unused
	private static IMenuManager help;
	@SuppressWarnings("unused")
	private static Action help_about;
	private static Action help_version;
	public static IToolBarManager mgr;
	
	public static Composite masterComposite;
	private static Composite graphComp;
	private static Composite treeComp;
	
	private static StapGraphParser parser;
	private static StapGraph graph;
	

	/**
	 * The constructor.
	 * @return 
	 */
	public SystemTapView() {
		isInitialized = true;
	}
	
	public static SystemTapView getSingleInstance(){
		if (isInitialized){
			return stapview;			
		}
		return null;
	}
	
	
	public static void testFunction() {
		if (masterComposite != null && !masterComposite.isDisposed())
			masterComposite.dispose();
	}
	
	public static void setValues(Composite graphC, Composite treeC, StapGraph g, StapGraphParser p){
		treeComp = treeC;
		graphComp = graphC;
		graph = g;
		parser = p;
	}
	
	
	/**
	 * Enable or Disable the graph options
	 * @param visible
	 */
	public static void setGraphOptions (boolean visible){
		save_callgraph.setEnabled(visible);
		view_treeview.setEnabled(visible);
		view_radialview.setEnabled(visible);
		view_aggregateview.setEnabled(visible);
		view_boxview.setEnabled(visible);
		view_refresh.setEnabled(visible);
		limits.setEnabled(visible);
		
		markers_next.setEnabled(visible);
		markers_previous.setEnabled(visible);
		
		animation_slow.setEnabled(visible);
		animation_fast.setEnabled(visible);
		mode_collapsednodes.setEnabled(visible);
		
		goto_next.setEnabled(visible);
		goto_previous.setEnabled(visible);
		goto_last.setEnabled(visible);
	}
	
/**
 * @param doMaximize : true && view minimized will maximize the view, 
 * otherwise it will just 'refresh'
 */
	public static void maximizeOrRefresh(boolean doMaximize){
		IWorkbenchPage page = SystemTapView
		.getSingleInstance().getViewSite().getWorkbenchWindow().getActivePage();
		
		if (doMaximize && page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED){
			IWorkbenchAction action = ActionFactory.MAXIMIZE.create(SystemTapView
					.getSingleInstance().getViewSite().getWorkbenchWindow());
			action.run();
		}else{
		    SystemTapView.layout();
		}
	}
	
	
	public static void firstTimeRefresh(){
		graphComp.setSize(masterComposite.getSize().x ,masterComposite.getSize().y);
	}
	
	
	public static Composite makeTreeComp(int treeSize) {
		if (treeComp != null && !treeComp.isDisposed()) {
			return treeComp;
		}
		
		Composite treeComp = new Composite(SystemTapView.masterComposite, SWT.NONE);
		GridData treegd = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		treegd.widthHint = treeSize;
		treeComp.setLayout(new FillLayout());
		treeComp.setLayoutData(treegd);
		return treeComp; 
	}
	
	public static Composite makeGraphComp() {
//		if (graphComp != null && !graphComp.isDisposed()) {
//			return graphComp;
//		}
		if (graphComp != null)
			graphComp.dispose();
		Composite graphComp = new Composite(SystemTapView.masterComposite, SWT.NONE);
		GridData graphgd = new GridData(SWT.FILL, SWT.FILL, true, true);
		graphComp.setLayout(new FillLayout());
		graphComp.setLayoutData(graphgd);
		return graphComp;
	}
	
	public static void layout() {
		masterComposite.layout();
	}
	
	/**
	 * If view is not maximized it will be maximized
	 */
	public static void maximizeIfUnmaximized() {
		IWorkbenchPage page = SystemTapView
		.getSingleInstance().getViewSite().getWorkbenchWindow().getActivePage();
		
		if (page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED){
			IWorkbenchAction action = ActionFactory.MAXIMIZE.create(SystemTapView
					.getSingleInstance().getViewSite().getWorkbenchWindow());
			action.run();
		}
		
	}
	
	
//	
//	public static void disposeAll() {
//		if (graphComp != null) {
//			graphComp.setVisible(false);
//			GridData gd = (GridData) graphComp.getLayoutData();
//			gd.exclude = true;
//			graphComp.setLayoutData(gd);
//			graphComp.dispose();
//		}
//		if (treeComp != null) {
//			treeComp.setVisible(false);
//			GridData gd = (GridData) treeComp.getLayoutData();
//			gd.exclude = true;
//			treeComp.setLayoutData(gd);
//			treeComp.dispose();
//		}
//	}

	/**
	 * This must be executed before a Graph is displayed
	 */
	public static void createPartControl(){
		
		
		setGraphOptions(true);
		String text = "";
		StyleRange[] sr = null;
		
		
		if (viewer != null && !viewer.isDisposed()) {
			text = viewer.getText();
			sr = viewer.getStyleRanges();
			viewer.dispose();
		}
		
		
		graphComp.setParent(masterComposite);
		
		if (treeComp != null)
			treeComp.setParent(masterComposite);

		if (graph == null) {
			createViewer(masterComposite);
			viewer.setText(text);
			viewer.setStyleRanges(sr);
		}
		
		//MAXIMIZE THE SYSTEMTAP VIEW WHEN RENDERING A GRAPH
		firstTimeRefresh();
        graph.reset();
        SystemTapView.layout();
	}
	
	
	public static void createViewer(Composite parent){
		viewer = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI
				| SWT.V_SCROLL | SWT.WRAP);

		viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Font font = new Font(parent.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
		viewer.setFont(font);
	}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	public void createPartControl(Composite parent) {
		masterComposite = parent;
		this.display = parent.getDisplay();
		Layout layout = new GridLayout(2, false);
		GridData gd = new GridData(100, 100);

		parent.setLayout(layout);
		parent.setLayoutData(gd);

		//CREATE THE TEXT VIEWER
		if (graph == null)
			createViewer(parent);

		// LOAD ALL ACTIONS
		createActions();
		
		//MENU FOR SYSTEMTAP BUTTONS
		mgr = getViewSite().getActionBars().getToolBarManager();
		
		
		//MENU FOR SYSTEMTAP GRAPH OPTIONS
		menu = getViewSite().getActionBars().getMenuManager();
		
		// ADD OPTIONS TO THE GRAPH MENU
		file = new MenuManager(Messages.getString("SystemTapView.0")); //$NON-NLS-1$
		view = new MenuManager(Messages.getString("SystemTapView.1")); //$NON-NLS-1$
		errors = new MenuManager(Messages.getString("SystemTapView.Errors")); //$NON-NLS-1$
		animation = new MenuManager(Messages.getString("SystemTapView.2")); //$NON-NLS-1$
		help = new MenuManager("Help");
		markers = new MenuManager("Markers");
		gotoMenu = new MenuManager("Goto");
		

		
		menu.add(file);
		menu.add(view);
		menu.add(animation);
		menu.add(gotoMenu);
		menu.add(errors);
		menu.add(help);
		
		
		file.add(open_callgraph);
		file.add(open_default);
		file.add(save_callgraph);
		
		
		errors.add(error_errorLog);
		errors.add(error_deleteError);
		
		
		view.add(view_treeview);
		view.add(view_radialview);
		view.add(view_aggregateview);
		view.add(view_boxview);
		view.add(getView_refresh());
		view.add(mode_collapsednodes);
		view.add(limits);
		
		
		gotoMenu.add(goto_previous);
		gotoMenu.add(goto_next);
		gotoMenu.add(goto_last);
		
		
		mgr.add(view_radialview);
		mgr.add(view_treeview);
		mgr.add(view_boxview);
		mgr.add(view_aggregateview);
		mgr.add(getView_refresh());
		mgr.add(mode_collapsednodes);
		
//		help.add(help_about);
		help.add(help_version);
		
		markers.add(markers_next);
		markers.add(markers_previous);
		
		animation.add(animation_slow);
		animation.add(animation_fast);
//		menu.add(markers);

		setGraphOptions(false);
		
		// Colouring helper variable
		previousEnd = 0;
		stapview = this;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (viewer != null && !viewer.isDisposed())
			viewer.setFocus();
	}
	
	/**
	 * Force the SystemTapView to initialize
	 */
	public static void forceDisplay(){
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			window.getActivePage().showView("org.eclipse.linuxtools.systemtap.localgui.graphing.stapview").setFocus(); //$NON-NLS-1$
		} catch (PartInitException e2) {
			e2.printStackTrace();
		}
		
	}

	public void prettyPrintln(String text) {	
		Vector<StyleRange> styles = new Vector<StyleRange>();
	    String[] txt = text.split("\\n"); //$NON-NLS-1$
	    int lineOffset = 0;	    
	    int inLineOffset;
	    
	    //txt[] contains text, with one entry for each new line
	    for (int i = 0; i < txt.length; i++) {
	    	
	    	//Skip blank strings
	    	if (txt[i].length() == 0)	{
	    		viewer.append(NEW_LINE);
	    		continue;
	    	}
	    	
	    	//Search for colour codes, if none exist then continue
	    	String[] split_txt = txt[i].split("~\\(");  //$NON-NLS-1$
	    	if (split_txt.length == 1) {
	    		viewer.append(split_txt[0]);
	    		viewer.append(NEW_LINE);
	    		continue;
	    	}
	    	
	    	inLineOffset = 0;
	    	for (int k = 0; k < split_txt.length; k++) {
	    		//Skip blank substrings
	    		if (split_txt[k].length() == 0)
	    			continue;
	    		
		    	//Split for the number codes
		    	String[] coloursAndText = split_txt[k].split("\\)~"); //$NON-NLS-1$
		    	
		    	//If the string is properly formatted, colours should be length 2
		    	//If it is not properly formatted, don't colour (just print)
		    	if (coloursAndText.length != 2) {
		    		for (int j = 0; j < coloursAndText.length; j++) {
		    			viewer.append(coloursAndText[j]);
		    			inLineOffset += coloursAndText[j].length(); 
		    		}
		    		continue;
		    	}
		    	
		    	//The first element in the array should contain the colours
		    	String[] colours = coloursAndText[0].split(","); //$NON-NLS-1$
		    	if (colours.length < 3) continue;
		    	
		    	//The second element in the array should contain the text
		    	viewer.append(coloursAndText[1]);
		    	
		    	//Create a colour based on the 3 integers (if there are any more integers, just ignore)
		    	int R = new Integer(colours[0].replaceAll(" ", "")).intValue();    	 //$NON-NLS-1$ //$NON-NLS-2$
		    	int G = new Integer(colours[1].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		    	int B = new Integer(colours[2].replaceAll(" ", "")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		    	
		    	if (R > 255) R = 255;
		    	if (G > 255) G = 255;
		    	if (B > 255) B = 255;
		    	
		    	if (R < 0 ) R = 0;
		    	if (G < 0 ) G = 0;
		    	if (B < 0 ) B = 0;
		    	
		    	Color newColor = new Color(display, R, G, B);
		    	
		    	//Find the offset of the current line
		    	lineOffset = viewer.getOffsetAtLine(viewer.getLineCount() - 1);
		    	
		    	//Create a new style that lasts no further than the length of the line
		    	StyleRange newStyle = new StyleRange(lineOffset + inLineOffset,
		    			coloursAndText[1].length(),
		    			newColor, null);
		    	styles.addElement(newStyle);
		    	
		    	inLineOffset+=coloursAndText[1].length();
	    	}
	    	
	    	viewer.append(NEW_LINE);
	    }

	    //Create a new style range
	    StyleRange[] s = new StyleRange[styles.size()];
	    styles.copyInto(s);
	    
	    int cnt = viewer.getCharCount();
	    
	    //Using replaceStyleRanges with previousEnd, etc, effectively adds
	    //the StyleRange to the existing set of Style Ranges (so we don't
	    //waste time fudging with old style ranges that haven't changed)
		viewer.replaceStyleRanges(previousEnd, cnt - previousEnd, s);
		previousEnd = cnt;

		//Change focus and update
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
	
	
	/**
	 * Populates the file menu
	 */
	public void createFileActions() {
		//Opens from some location in your program
		open_callgraph = new Action(Messages.getString("SystemTapView.7")){ //$NON-NLS-1$
			public void run(){
				FileDialog dialog = new FileDialog(new Shell(), SWT.DEFAULT);
				String filePath =  dialog.open();
				if (filePath != null){
					StapGraphParser new_parser = new StapGraphParser(Messages.getString("SystemTapView.10"), filePath); //$NON-NLS-1$
					new_parser.schedule();					
				}
			}
		};
		
		//Opens from the default location
		open_default = new Action(Messages.getString("SystemTapView.11")){ //$NON-NLS-1$
			public void run(){
				StapGraphParser new_parser = new 
						StapGraphParser(Messages.getString("SystemTapView.12"),  //$NON-NLS-1$
										PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH);
				new_parser.schedule();					
			}
		};
		
		
		//Save callgraph.out
		save_callgraph = new Action(Messages.getString("SystemTapView.8")){ //$NON-NLS-1$
			public void run(){
				Shell sh = new Shell();
				FileDialog dialog = new FileDialog(sh, SWT.SAVE);
				String filePath = dialog.open();
				
				if (filePath != null) {
					File file = new File(filePath);
					String content = Messages.getString("SystemTapView.25") //$NON-NLS-1$
					+ parser.text
					+ NEW_LINE
					+ parser.endingTimeInNS
					+ NEW_LINE
					+ parser.totalTime;
					try {
						// WAS THE FILE CREATED OR DOES IT ALREADY EXIST
						if (file.createNewFile()) {
							Helper.writeToFile(filePath, content);
						} else {
							if (MessageDialog
									.openConfirm(
											sh,
											Messages
													.getString("SystemTapView.FileExistsTitle"), //$NON-NLS-1$
											Messages
													.getString("SystemTapView.FileExistsMessage"))) { //$NON-NLS-1$
								file.delete();
								file.createNewFile();
								Helper.writeToFile(filePath, content);
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		};
		
	}
	
	public void createHelpActions() {
		help_version = new Action("Version") { 
			public void run() {
			Runtime rt = Runtime.getRuntime();
			try {
				Process pr = rt.exec("stap -V"); //$NON-NLS-1$
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr
						.getErrorStream()));
				String line = ""; //$NON-NLS-1$
				String message = ""; //$NON-NLS-1$
				
				while ((line = buf.readLine()) != null) {
					message += line + NEW_LINE; //$NON-NLS-1$
				}
				
				try {
					pr.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				
				Shell sh = new Shell();
				
				MessageDialog.openInformation(sh, Messages.getString("SystemTapView.SystemTapVersionBox"), message); //$NON-NLS-1$
					
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		};
		
		help_about = new Action("About") {
			public void run() {
				Display disp = Display.getCurrent();
				if (disp == null){
					disp = Display.getDefault();
				}

				
				Shell sh = new Shell(disp, SWT.MIN | SWT.MAX);
				sh.setSize(425, 540);
				GridLayout gl = new GridLayout(1, true);
				sh.setLayout(gl);

				sh.setText(Messages.getString("LaunchAbout.0")); //$NON-NLS-1$
				
				Image img = new Image(disp, PluginConstants.PLUGIN_LOCATION+"systemtap.png"); //$NON-NLS-1$
				Composite cmp = new Composite(sh, sh.getStyle());
				cmp.setLayout(gl);
				GridData data = new GridData(415,100);
				cmp.setLayoutData(data);
				cmp.setBackgroundImage(img);

				Composite c = new Composite(sh, sh.getStyle());
				c.setLayout(gl);
				GridData gd = new GridData(415,400);
				c.setLayoutData(gd);
				c.setLocation(0,300);
				StyledText viewer = new StyledText(c, SWT.READ_ONLY | SWT.MULTI
						| SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);		
				
				GridData viewerGD = new GridData(SWT.FILL, SWT.FILL, true, true);
				viewer.setLayoutData(viewerGD);
				Font font = new Font(sh.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
				viewer.setFont(font);
				viewer.setText(
						 Messages.getString("LaunchAbout.2") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.3") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.4") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.5") +  //$NON-NLS-1$
						 Messages.getString("LaunchAbout.6") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.7") + //$NON-NLS-1$
						 
						 Messages.getString("LaunchAbout.8") + //$NON-NLS-1$
//						 
//						 Messages.getString("LaunchAbout.9") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.10") + //$NON-NLS-1$
						 
						 Messages.getString("LaunchAbout.11") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.12") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.13") + //$NON-NLS-1$
						 
//						 Messages.getString("LaunchAbout.14") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.15") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.16") + //$NON-NLS-1$
						 
						 Messages.getString("LaunchAbout.17") + //$NON-NLS-1$
						 
//						 Messages.getString("LaunchAbout.18") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.19") + //$NON-NLS-1$
						 
						 Messages.getString("LaunchAbout.20") + //$NON-NLS-1$
						 Messages.getString("LaunchAbout.21") //$NON-NLS-1$
						);


				
				sh.open();		
			}
		};
	}
	
	/**
	 * Populates the Errors menu
	 */
	public void createErrorActions() {

		error_errorLog = new Action(Messages.getString("SystemTapView.OpenLog")) { //$NON-NLS-1$
			public void run() {
				boolean error = false;
				File log = new File(PluginConstants.DEFAULT_OUTPUT + "Error.log"); //$NON-NLS-1$
				BufferedReader buff;
				try {
					buff = new BufferedReader(new FileReader(log));
				String logText = ""; //$NON-NLS-1$
				String line;
				
				while ((line = buff.readLine()) != null) {
					logText+=line + PluginConstants.NEW_LINE;
				}
				
				Shell sh = new Shell(SWT.BORDER | SWT.TITLE);
				
				sh.setText("SystemTap Error Log");
				sh.setLayout(new FillLayout());
				sh.setSize(600,600);
				
				StyledText txt = new StyledText(sh, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
				
				txt.setText(logText);

				sh.setText("Error Log");
				
				sh.open();
				txt.setTopIndex(txt.getLineCount());

				
				} catch (FileNotFoundException e) {
					error = true;
				} catch (IOException e) {
					error = true;
				} finally {
					if (error) {
						SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
							Messages.getString("SystemTapView.ErrorMessageName"), //$NON-NLS-1$
							Messages.getString("SystemTapView.ErrorMessageTitle"), //$NON-NLS-1$
							Messages.getString("SystemTapView.ErrorMessageBody") + //$NON-NLS-1$
							Messages.getString("SystemTapView.ErrorMessageBody2")); //$NON-NLS-1$
						mess.schedule();
					}
				}
				
			}
		};
		
		
		error_deleteError = new Action(Messages.getString("SystemTapView.ClearLog")) { //$NON-NLS-1$
			public void run() {
					if (!MessageDialog.openConfirm(new Shell(), Messages.getString("SystemTapView.DeleteLogsTitle"),  //$NON-NLS-1$
							Messages.getString("SystemTapView.DeleteLogsMessage") + //$NON-NLS-1$
							Messages.getString("SystemTapView.DeleteLogsMessage2"))) //$NON-NLS-1$
						return;
					
					SystemTapErrorHandler.delete();
			}
		};
		
		
	}
	
	public void createViewActions() {
		//Set drawmode to tree view
		view_treeview = new Action(Messages.getString("SystemTapView.16")){ //$NON-NLS-1$
			public void run() {
				graph.draw(StapGraph.CONSTANT_DRAWMODE_TREE, graph.getAnimationMode(), 
						graph.getRootVisibleNodeNumber());
				graph.scrollTo(graph.getNode(graph.getRootVisibleNodeNumber()).getLocation().x
						- graph.getBounds().width / 2, graph.getNode(
						graph.getRootVisibleNodeNumber()).getLocation().y);
			}
		};
		ImageDescriptor treeImage = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), PluginConstants.PLUGIN_LOCATION + "icons/tree_view.gif"));
		view_treeview.setImageDescriptor(treeImage);
		
		
		//Set drawmode to radial view
		view_radialview = new Action(Messages.getString("SystemTapView.17")){ //$NON-NLS-1$
			public void run(){
				graph.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, graph.getAnimationMode(),
						graph.getRootVisibleNodeNumber());

			}
		};
		ImageDescriptor d = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), 
						PluginConstants.PLUGIN_LOCATION + "/icons/radial_view.gif"));
		view_radialview.setImageDescriptor(d);

		
		//Set drawmode to aggregate view
		view_aggregateview = new Action(Messages.getString("SystemTapView.18")){ //$NON-NLS-1$
			public void run(){
				graph.draw(StapGraph.CONSTANT_DRAWMODE_AGGREGATE, graph.getAnimationMode(), 
						graph.getRootVisibleNodeNumber());

			}
		};
		ImageDescriptor aggregateImage = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), 
						PluginConstants.PLUGIN_LOCATION + "/icons/view_aggregateview.gif"));
		view_aggregateview.setImageDescriptor(aggregateImage);
		
		
		//Set drawmode to box view
		view_boxview = new Action(Messages.getString("SystemTapView.19")){ //$NON-NLS-1$
			public void run(){
				graph.draw(StapGraph.CONSTANT_DRAWMODE_BOX, graph.getAnimationMode(), 
						graph.getRootVisibleNodeNumber());
			}
		};
		ImageDescriptor boxImage = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), 
						PluginConstants.PLUGIN_LOCATION + "/icons/showchild_mode.gif"));
		view_boxview.setImageDescriptor(boxImage);
		
		
		setView_refresh(new Action("Reset"){
			public void run(){
				graph.reset();
			}
		});
		ImageDescriptor refreshImage = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), 
						PluginConstants.PLUGIN_LOCATION + "/icons/nav_refresh.gif"));
		getView_refresh().setImageDescriptor(refreshImage);
		
		
	}
	

	/**
	 * Populates Animate menu.
	 */
	public void createAnimateActions() {
		//Set animation mode to slow
		animation_slow = new Action(Messages.getString("SystemTapView.20"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
			public void run(){
				graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
				this.setChecked(true);
				animation_slow.setChecked(true);
				animation_fast.setChecked(false);
			}
		};
		
		animation_slow.setChecked(true);
		
		//Set animation mode to fast
		animation_fast = new Action(Messages.getString("SystemTapView.22"), Action.AS_RADIO_BUTTON){ //$NON-NLS-1$
			public void run(){
				graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
				animation_slow.setChecked(false);
				animation_fast.setChecked(true);
			}
		};
		
		//Toggle collapse mode
		mode_collapsednodes = new Action(Messages.getString("SystemTapView.24"), Action.AS_CHECK_BOX){ //$NON-NLS-1$
			public void run(){
				
				if (graph.isCollapseMode()) {
					graph.setCollapseMode(false);
					graph.draw(graph.getRootVisibleNodeNumber());
				}
				else {
					graph.setCollapseMode(true);
					graph.draw(graph.getRootVisibleNodeNumber());
				}
			}
		};
		
		ImageDescriptor newImage = ImageDescriptor.createFromImage(
				new Image(Display.getCurrent(), PluginConstants.PLUGIN_LOCATION + "icons/mode_collapsednodes.gif"));
		mode_collapsednodes.setImageDescriptor(newImage);
		
		limits = new Action("Set limits", Action.AS_PUSH_BUTTON) {
			private Spinner limit;
			private Spinner buffer;
			private Shell sh;
			public void run() {
				sh = new Shell();
				sh.setLayout(new GridLayout());
				sh.setSize(150, 200);
				Label limitLabel = new Label(sh, SWT.NONE);
				limitLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
				limitLabel.setText("Max nodes: ");
				limit = new Spinner(sh, SWT.BORDER);
				limit.setMaximum(5000);
				limit.setSelection(graph.getMaxNodes());
				limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
				
				Label bufferLabel = new Label(sh, SWT.NONE);
				bufferLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
				bufferLabel.setText("Max depth: ");
				buffer = new Spinner(sh, SWT.BORDER);
				buffer.setMaximum(5000);
				buffer.setSelection(graph.getLevelBuffer());
				buffer.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
				
				Button set_limit = new Button(sh, SWT.PUSH);
				set_limit.setText("Set values");
				set_limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
				set_limit.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						boolean redraw = false;
						if (limit.getSelection() > 0 && buffer.getSelection() > 0) {
							graph.setMaxNodes(limit.getSelection());
							graph.setLevelBuffer(buffer.getSelection());
							
							if (graph.changeLevelLimits(graph.getLevelOfNode(graph.getRootVisibleNodeNumber()))) {
								SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
										"LevelBuffer too high", "LevelBuffer too high", 
										"Unfortunately the requested setting for level buffer " +
										"could not be processed due to the maximum number of nodes. " +
										"Setting levelBuffer to the maximum number of levels possible " +
										"without hitting the max nodes(" + graph.getLevelBuffer() +
										")." + PluginConstants.NEW_LINE + PluginConstants.NEW_LINE +  
										"If you really want to see more levels, increment max nodes. " +
										"This may cause lag on some systems.");
								mess.schedule();
							}
							
							redraw = true;
						}
						sh.dispose();
						
						if (redraw)
							graph.draw();
					}
					
				});

				
				sh.open();
			}
		};

	}

/**
 * Creates actions by calling the relevant functions
 */
	public void createActions() {
		createFileActions();
		createHelpActions();
		createErrorActions();
		createViewActions();
		createAnimateActions();
		createMarkerActions();		
		createMovementActions();
//		createButtonActions();

		mode_collapsednodes.setChecked(true);
		
	}
	
	public void createMovementActions() {
		goto_next = new Action("(N)ext") {
			public void run() {
				if (graph.isCollapseMode()) {
					graph.setCollapseMode(false);
				}
				int toDraw = graph.getNextCalledNode(graph.getRootVisibleNodeNumber());
				if (toDraw != -1)
					graph.draw(toDraw);
			}
		};
		
		goto_previous = new Action("(P)revious") {
			public void run() {
				if (graph.isCollapseMode()) {
					graph.setCollapseMode(false);
				}
				int toDraw = graph.getPreviousCalledNode(graph.getRootVisibleNodeNumber());
				if (toDraw != -1)
					graph.draw(toDraw);
			}
		};
		
		goto_last = new Action("(L)ast") {
			public void run() {
				if (graph.isCollapseMode())
					graph.setCollapseMode(false);
				graph.draw(graph.getLastFunctionCalled());
			}
		};
	}
	
	public void createMarkerActions() {
		markers_next = new Action("(n)ext") {
			public void run() {
				graph.draw(graph.getNextMarkedNode());
			}
		};
		
		markers_previous = new Action("(p)revious") {
			public void run() {
				graph.draw(graph.getPreviousMarkedNode());
			}
		};
	}
	
	
	/**
	 * Creates actions that appear on the SystemTap View taskbar
	 * Do not use.
	 */
	@Deprecated
	public void createButtonActions(){
    	
    	/*
    	 * Execute a kill command on all running processes containing 'stap' 
    	 */
		killSystemTapScript = new Action(Messages.getString("SystemTapView.KillScriptButton")) { //$NON-NLS-1$
			
			public void run() {
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec("kill stap"); //$NON-NLS-1$
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		};
		killSystemTapScript.setToolTipText(Messages.getString("SystemTapView.KillScriptToolTip")); //$NON-NLS-1$
		
		/*
		 * Check the current version of SystemTap
		 */
		checkSystemTapVersion = new Action(Messages.getString("SystemTapView.CheckVersionButton")) { //$NON-NLS-1$
			
			public void run() {
				Runtime rt = Runtime.getRuntime();
				try {
					Process pr = rt.exec("stap -V"); //$NON-NLS-1$
					BufferedReader buf = new BufferedReader(new InputStreamReader(pr
							.getErrorStream()));
					String line = ""; //$NON-NLS-1$
					String message = ""; //$NON-NLS-1$
					
					while ((line = buf.readLine()) != null) {
						message += line + NEW_LINE; //$NON-NLS-1$
					}
					
					try {
						pr.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					
					Shell sh = new Shell();
					
					MessageDialog.openInformation(sh, Messages.getString("SystemTapView.SystemTapVersionBox"), message); //$NON-NLS-1$
						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		};
		checkSystemTapVersion.setToolTipText(Messages.getString("SystemTapView.CheckVersionToolTip")); //$NON-NLS-1$
		
		
		disposeGraph = new Action("Dispose graph") { //$NON-NLS-1$
			
			public void run() {
				SystemTapView.disposeGraph();
			}
			
			
		};
		disposeGraph.setToolTipText("Dispose of graph area"); //$NON-NLS-1$
	}
	
	public static void disposeGraph() {
		if (graphComp != null && !graphComp.isDisposed())
			graphComp.dispose();
		if (treeComp != null && !treeComp.isDisposed())
			treeComp.dispose();
		if (viewer!= null && !viewer.isDisposed()) {
			String tmp = viewer.getText();
			StyleRange[] tempRange = viewer.getStyleRanges();
			viewer.dispose();
			createViewer(masterComposite);
			viewer.setText(tmp);
			viewer.setStyleRanges(tempRange);
		}
		SystemTapView.setGraphOptions(false);
		//Force a redraw (.redraw() .update() not working)
		SystemTapView.maximizeOrRefresh(false);
	}

	public static Action getAnimation_slow() {
		return animation_slow;
	}

	public static void setAnimation_slow(Action animation_slow) {
		SystemTapView.animation_slow = animation_slow;
	}

	public static Action getAnimation_fast() {
		return animation_fast;
	}

	public static void setAnimation_fast(Action animation_fast) {
		SystemTapView.animation_fast = animation_fast;
	}

	public static IMenuManager getAnimation() {
		return animation;
	}

	public static void setAnimation(IMenuManager animation) {
		SystemTapView.animation = animation;
	}

	public static Action getMode_collapsednodes() {
		return mode_collapsednodes;
	}

	public static void setMode_collapsednodes(Action mode_collapsednodes) {
		SystemTapView.mode_collapsednodes = mode_collapsednodes;
	}

	public static void setView_refresh(Action view_refresh) {
		SystemTapView.view_refresh = view_refresh;
	}

	public static Action getView_refresh() {
		return view_refresh;
	}

	public static Action getGoto_next() {
		return goto_next;
	}

	public static void setGoto_next(Action gotoNext) {
		goto_next = gotoNext;
	}

	public static Action getGoto_previous() {
		return goto_previous;
	}

	public static void setGoto_parent(Action gotoParent) {
		goto_previous = gotoParent;
	}

	public static Action getGoto_last() {
		return goto_last;
	}

	public static void setGoto_last(Action gotoLast) {
		goto_last = gotoLast;
	}
}
	
/**
 * The code graveyard: Where snippets go to die
 */
//StyleRange[] existingRange = viewer.getStyleRanges();
//
//StyleRange[] s = new StyleRange[styles.size()];
//StyleRange[] s2 = new StyleRange[styles.size() + existingRange.length];
//styles.copyInto(s);
//
//for (int i = 0; i < existingRange.length; i++)
//	s2[i] = existingRange[i];
//
//for (int i = 0; i < styles.size(); i ++)
//	s2[i+existingRange.length] = s[i];
