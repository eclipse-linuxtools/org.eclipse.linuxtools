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

package org.eclipse.linuxtools.internal.callgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapView;
import org.eclipse.linuxtools.internal.callgraph.graphlisteners.AutoScrollSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *    The SystemTap View for displaying output of the 'stap' command, and acts
 *    as a container for any graph to be rendered. Any buttons/controls/actions
 *    necessary to the smooth running of SystemTap could be placed here.
 */
public class CallgraphView extends SystemTapView {

    private StapGraphParser parser;

    private Action viewTreeview;
    private Action viewRadialview;
    private Action viewAggregateview;
    private Action viewLevelview;
    private Action viewRefresh;
    private Action animationSlow;
    private Action animationFast;
    private Action modeCollapsedNodes;
    private Action markersNext;
    private Action markersPrevious;
    private Action limits;
    private Action gotoNext;
    private Action gotoPrevious;
    private Action gotoLast;
    private Action play;
    private Action saveDot;
    private Action saveColDot;
    private Action saveCurDot;
    private Action saveText;
    private ImageDescriptor playImage = getImageDescriptor("icons/perform.png"); //$NON-NLS-1$
    private ImageDescriptor pauseImage = getImageDescriptor("icons/pause.gif"); //$NON-NLS-1$

    private Composite graphComp;
    private Composite treeComp;

    private StapGraph g;
    private static final int TREE_SIZE = 200;

    /**
     * Initializes the view by creating composites (if necessary) and canvases
     * Calls loadData(), and calls finishLoad() if not in realTime mode (otherwise
     * it is up to the user-defined update methods to finish loading).
     *
     * @return status
     *
     */
    @Override
    public IStatus initializeView(Display targetDisplay, IProgressMonitor monitor) {

        if (targetDisplay == null && Display.getCurrent() == null) {
            Display.getDefault();
        }

        makeTreeComp();
        makeGraphComp();
        graphComp.setBackgroundMode(SWT.INHERIT_FORCE);

        //Create papa canvas
        Canvas papaCanvas = new Canvas(graphComp, SWT.BORDER);
        GridLayout papaLayout = new GridLayout(1, true);
        papaLayout.horizontalSpacing=0;
        papaLayout.verticalSpacing=0;
        papaLayout.marginHeight=0;
        papaLayout.marginWidth=0;
        papaCanvas.setLayout(papaLayout);
        GridData papaGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        papaGD.widthHint=160;
        papaCanvas.setLayoutData(papaGD);

        //Add first button
        Image image = getImageDescriptor("icons/up.gif").createImage(); //$NON-NLS-1$
        Button up = new Button(papaCanvas, SWT.PUSH);
        GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonData.widthHint = 150;
        buttonData.heightHint = 20;
        up.setData(buttonData);
        up.setImage(image);
        up.setToolTipText(Messages.getString("CallgraphView.ThumbNailUp")); //$NON-NLS-1$

        //Add thumb canvas
        Canvas thumbCanvas = new Canvas(papaCanvas, SWT.NONE);

        //Add second button
        image = getImageDescriptor("icons/down.gif").createImage(); //$NON-NLS-1$
        Button down = new Button(papaCanvas, SWT.PUSH);
        buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonData.widthHint = 150;
        buttonData.heightHint = 0;
        down.setData(buttonData);
        down.setImage(image);
        down.setToolTipText(Messages.getString("CallgraphView.ThumbNailDown")); //$NON-NLS-1$

        //Initialize graph
        g = new StapGraph(graphComp, SWT.BORDER, treeComp, papaCanvas, this);
        g.setLayoutData(new GridData(masterComposite.getBounds().width,Display.getCurrent().getBounds().height - TREE_SIZE));

        up.addSelectionListener(new AutoScrollSelectionListener(
                AutoScrollSelectionListener.AUTO_SCROLL_UP, g));
        down.addSelectionListener(new AutoScrollSelectionListener(
                AutoScrollSelectionListener.AUTO_SCROLL_DOWN, g));

        //Initialize thumbnail
        GridData thumbGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        thumbGD.widthHint=160;
        thumbCanvas.setLayoutData(thumbGD);
        LightweightSystem lws = new LightweightSystem(thumbCanvas);
        ScrollableThumbnail thumb = new ScrollableThumbnail(g.getViewport());
        thumb.setSource(g.getContents());
        lws.setContents(thumb);

        loadData(monitor);
        return finishLoad(monitor);
    }

    /**
     * Load data.
     * @param mon -- Progress monitor.
     * @return
     */
    private IStatus loadData(IProgressMonitor mon) {
        IProgressMonitor monitor = mon;
        //Dummy node, set start time
        if (g.getNodeData(0) == null) {
            g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME,
                    1, 1, -1, false, ""); //$NON-NLS-1$
        }
        g.setStartTime(parser.startTime);
        g.setEndTime(parser.endingTimeInNS);


        /*
         * Load graph data
         */
        for (int id_parent : parser.serialMap.keySet()) {
            if (id_parent < 0) {
                continue;
            }
            boolean marked = false;
            String msg = ""; //$NON-NLS-1$
            if (g.getNodeData(id_parent) == null) {
                if (parser.markedMap.get(id_parent) != null) {
                    marked = true;
                    msg = parser.markedMap.remove(id_parent);
                }
                g.loadData(SWT.NONE, id_parent, parser.serialMap.get(id_parent), parser.timeMap.get(id_parent),
                        1, 0, marked, msg);
            }

            for (int key :parser.neighbourMaps.keySet()) {
                HashMap<Integer, ArrayList<Integer>> outNeighbours = parser.neighbourMaps.get(key);
                if (outNeighbours == null || outNeighbours.get(id_parent) == null) {
                    continue;
                }
                for (int id_child : outNeighbours.get(id_parent)) {
                    if (g.getNodeData(id_child) != null && id_child < 0) {
                        //Assume this is an additional call of the same node
                        //Should only happen in dot-files!!
                        g.addCalled(id_child);
                        continue;
                    } else if (g.getNodeData(id_child) != null) {
                        continue;
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }

                    marked = false;
                    msg = ""; //$NON-NLS-1$
                    if (parser.markedMap.get(id_child) != null) {
                        marked = true;
                        msg = parser.markedMap.remove(id_child);
                    }
                    if (id_child != -1) {
                        if (parser.timeMap.get(id_child) == null){
                            g.loadData(SWT.NONE, id_child, parser.serialMap
                                    .get(id_child), parser.timeMap.get(0),
                                    1, id_parent, marked,msg);
                        }else{
                            g.loadData(SWT.NONE, id_child, parser.serialMap
                                    .get(id_child), parser.timeMap.get(id_child),
                                    1, id_parent, marked,msg);
                        }
                    }
                }
            }

            if (parser.neighbourMaps.size() > 1) {
                g.setThreaded();
            }
        }

        monitor.worked(1);
        if (parser.markedMap.size() > 0) {
            //Still some markers left
            for (int key : parser.markedMap.keySet()) {
                g.insertMessage(key, parser.markedMap.get(key));
            }

            //Erase the remaining nodes, just in case
            parser.markedMap.clear();
        }


        if (g.aggregateTime == null) {
            g.aggregateTime = new HashMap<>();
        }
        if (g.aggregateCount == null) {
            g.aggregateCount = new HashMap<>();
        }

        g.aggregateCount.putAll(parser.countMap);
        g.aggregateTime.putAll(parser.aggregateTimeMap);
        //TODO: Do not set to 0.
        g.setLastFunctionCalled(0);


        //Finish off by collapsing nodes, initializing the tree and setting options
        g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
        monitor.worked(1);
        setGraphOptions(true);
        g.initializeTree();
        g.setProject(parser.project);


        return Status.OK_STATUS;
    }

    /**
     * Completes the loading process by calculating aggregate data.
     *
     * @param monitor
     * @return
     */
    private IStatus finishLoad(IProgressMonitor monitor) {

        if (g.aggregateCount == null) {
            g.aggregateCount = new HashMap<>();
        }

        g.aggregateCount.putAll(parser.countMap);

        if (g.aggregateTime == null) {
            g.aggregateTime = new HashMap<>();
        }
        g.aggregateTime.putAll(parser.aggregateTimeMap);

        //Set total time
        if (parser.totalTime != -1) {
            g.setTotalTime(parser.totalTime);
        }

        //-------------Finish initializations
        //Generate data for collapsed nodes
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        g.initializeTree();


        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        g.setCallOrderList(parser.callOrderList);
        g.setProject(parser.project);


        this.initializePartControl();
        return Status.OK_STATUS;
    }


    /**
     * Enable or Disable the graph options
     * @param visible
     */
    private void setGraphOptions (boolean visible){
        play.setEnabled(visible);
        saveFile.setEnabled(visible);
        saveDot.setEnabled(visible);
        saveColDot.setEnabled(visible);
        saveCurDot.setEnabled(visible);
        saveText.setEnabled(visible);

        viewTreeview.setEnabled(visible);
        viewRadialview.setEnabled(visible);
        viewAggregateview.setEnabled(visible);
        viewLevelview.setEnabled(visible);
        viewRefresh.setEnabled(visible);
        limits.setEnabled(visible);

        markersNext.setEnabled(visible);
        markersPrevious.setEnabled(visible);

        animationSlow.setEnabled(visible);
        animationFast.setEnabled(visible);
        modeCollapsedNodes.setEnabled(visible);

        gotoNext.setEnabled(visible);
        gotoPrevious.setEnabled(visible);
        gotoLast.setEnabled(visible);
    }



    private void makeTreeComp() {
        if (treeComp != null && !treeComp.isDisposed()) {
            treeComp.dispose();
        }

        treeComp = new Composite(this.masterComposite, SWT.NONE);
        GridData treegd = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        treegd.widthHint = TREE_SIZE;
        treeComp.setLayout(new FillLayout());
        treeComp.setLayoutData(treegd);
    }

    private void makeGraphComp() {
        if (graphComp != null && !graphComp.isDisposed()) {
            graphComp.dispose();
        }
        graphComp = new Composite(this.masterComposite, SWT.NONE);
        GridData graphgd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing=0;
        gl.verticalSpacing=0;

        graphComp.setLayout(gl);
        graphComp.setLayoutData(graphgd);
    }


    /**
     * This must be executed before a Graph is displayed
     */
    private void initializePartControl(){
        setGraphOptions(true);
        if (graphComp == null) {
            return;
        }
        graphComp.setParent(masterComposite);

        if (treeComp != null) {
            treeComp.setParent(masterComposite);
        }

        graphComp.setSize(masterComposite.getSize().x ,masterComposite.getSize().y);
    }

    /**
     * The action performed by saveText.
     */
    private void saveTextAction() {
        //Prints an 80 char table
        Shell sh = new Shell();
        FileDialog dialog = new FileDialog(sh, SWT.SAVE);
        String filePath = dialog.open();

        if (filePath == null) {
            return;
        }
        File f = new File(filePath);
        f.delete();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
            f.createNewFile();
            StringBuilder builder = new StringBuilder();
            builder.append("                           Function                           | Called |  Time\n"); //$NON-NLS-1$

            for (StapData k : g.nodeDataMap.values()) {
                if ( (!k.isCollapsed ) && !k.isOnlyChildWithThisName()) {
                    continue;
                }
                if (k.isCollapsed) {
                    StringBuilder name = new StringBuilder(k.name);
                    name = fixString(name, 60);
                    builder.append(" " + name + " | "); //$NON-NLS-1$ //$NON-NLS-2$

                    StringBuilder called = new StringBuilder("" + k.timesCalled); //$NON-NLS-1$
                    called = fixString(called, 6);

                    StringBuilder time = new StringBuilder("" + //$NON-NLS-1$
                            StapNode.numberFormat.format((float) k.getTime()/g.getTotalTime() * 100)
                            + "%"); //$NON-NLS-1$
                    time = fixString(time, 6);

                    builder.append(called + " | " + time + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                if (builder.length() > 0) {
                    out.append(builder.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and
     * initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        if (masterComposite != null) {
            masterComposite.dispose();
        }
        masterComposite = parent;
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing=0;
        GridData gd = new GridData(100, 100);

        parent.setLayout(layout);
        parent.setLayoutData(gd);

        // LOAD ALL ACTIONS
        createActions();

        //MENU FOR SYSTEMTAP BUTTONS
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();


        //MENU FOR SYSTEMTAP GRAPH OPTIONS
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();

        // ADD OPTIONS TO THE GRAPH MENU
        addFileMenu();

        saveCurDot = new Action(Messages.getString("CallgraphView.SaveViewAsDot")) { //$NON-NLS-1$
            @Override
            public void run(){
                writeToDot(g.getCollapseMode(), g.nodeMap.keySet());
            }

        };
        saveDot = new Action(Messages.getString("CallgraphView.SaveAllUncollapsedAsDot")) { //$NON-NLS-1$
            @Override
            public void run(){
              writeToDot(false, g.nodeDataMap.keySet());
            }
        };

        saveColDot = new Action (Messages.getString("CallgraphView.SaveAllCollapsedAsDot")) { //$NON-NLS-1$
             @Override
            public void run(){
                    writeToDot(true, g.nodeDataMap.keySet());
                }

        };

        saveText = new Action (Messages.getString("CallgraphView.SaveCollapsedAsASCII")) { //$NON-NLS-1$
            @Override
            public void run() {
                saveTextAction();
            }
        };
        IMenuManager saveMenu = new MenuManager(Messages.getString("CallgraphView.SaveMenu")); //$NON-NLS-1$
        file.add(saveMenu);
        saveMenu.add(saveCurDot);
        saveMenu.add(saveColDot);
        saveMenu.add(saveText);
        saveMenu.add(saveDot);
        IMenuManager view = new MenuManager(Messages.getString("CallgraphView.ViewMenu")); //$NON-NLS-1$
        IMenuManager animation = new MenuManager(Messages.getString("CallgraphView.AnimationMenu")); //$NON-NLS-1$
        IMenuManager markers = new MenuManager(Messages.getString("CallgraphView.Markers")); //$NON-NLS-1$
        IMenuManager gotoMenu = new MenuManager(Messages.getString("CallgraphView.GoTo")); //$NON-NLS-1$
        menu.add(view);
        menu.add(gotoMenu);
        addHelpMenu();

        view.add(viewTreeview);
        view.add(viewRadialview);
        view.add(viewAggregateview);
        view.add(viewLevelview);
        view.add(getViewRefresh());
        view.add(modeCollapsedNodes);
        view.add(limits);
        view.add(animation);


        gotoMenu.add(play);
        gotoMenu.add(gotoPrevious);
        gotoMenu.add(gotoNext);
        gotoMenu.add(gotoLast);
        gotoMenu.add(markers);

        addKillButton();
        mgr.add(play);
        mgr.add(viewRadialview);
        mgr.add(viewTreeview);
        mgr.add(viewLevelview);
        mgr.add(viewAggregateview);
        mgr.add(modeCollapsedNodes);

        markers.add(markersNext);
        markers.add(markersPrevious);

        animation.add(animationSlow);
        animation.add(animationFast);

        setGraphOptions(false);
    }


    private static StringBuilder fixString(StringBuilder name, int length) {
        if (name.length() > length) {
            name = new StringBuilder(name.substring(0, length - 1));
        } else {
            int diff = length - name.length();
            boolean left = true;
            while (diff > 0) {
                if (left) {
                    name.insert(0, " "); //$NON-NLS-1$
                    left = false;
                } else {
                    name.append(" "); //$NON-NLS-1$
                    left = true;
                }
                diff--;
            }
        }
        return name;
    }


    private void createViewActions() {
        viewTreeview = new Action(Messages.getString("CallgraphView.TreeView")){ //$NON-NLS-1$
            @Override
            public void run() {
                g.draw(StapGraph.CONSTANT_DRAWMODE_TREE,
                        g.getAnimationMode(), g.getRootVisibleNodeNumber());
                g.scrollTo(g.getNode(g.getRootVisibleNodeNumber())
                        .getLocation().x - g.getBounds().width / 2, g
                        .getNode(g.getRootVisibleNodeNumber())
                        .getLocation().y);
                if (play != null) {
                    play.setEnabled(true);
                }
            }
        };
        ImageDescriptor treeImage = getImageDescriptor("icons/tree_view.gif"); //$NON-NLS-1$
        viewTreeview.setImageDescriptor(treeImage);


        //Set drawmode to radial view
        viewRadialview = new Action(Messages.getString("CallgraphView.RadialView")){ //$NON-NLS-1$
            @Override
            public void run(){
                g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL,
                        g.getAnimationMode(), g.getRootVisibleNodeNumber());
                if (play != null) {
                    play.setEnabled(true);
                }
            }
        };
        ImageDescriptor d = getImageDescriptor("/icons/radial_view.gif"); //$NON-NLS-1$
        viewRadialview.setImageDescriptor(d);

        //Set drawmode to aggregate view
        viewAggregateview = new Action(Messages.getString("CallgraphView.AggregateView")){ //$NON-NLS-1$
            @Override
            public void run(){
                g.draw(StapGraph.CONSTANT_DRAWMODE_AGGREGATE,
                        g.getAnimationMode(), g.getRootVisibleNodeNumber());
                if (play != null) {
                    play.setEnabled(false);
                }
            }
        };
        ImageDescriptor aggregateImage = getImageDescriptor("/icons/view_aggregateview.gif"); //$NON-NLS-1$
        viewAggregateview.setImageDescriptor(aggregateImage);


        //Set drawmode to level view
        viewLevelview = new Action(Messages.getString("CallgraphView.LevelView")){ //$NON-NLS-1$
            @Override
            public void run(){
                g.draw(StapGraph.CONSTANT_DRAWMODE_LEVEL,
                        g.getAnimationMode(), g.getRootVisibleNodeNumber());
                if (play != null) {
                    play.setEnabled(true);
                }
            }
        };
        ImageDescriptor levelImage = getImageDescriptor("/icons/showchild_mode.gif"); //$NON-NLS-1$
        viewLevelview.setImageDescriptor(levelImage);


        this.viewRefresh = new Action(Messages.getString("CallgraphView.Reset")){ //$NON-NLS-1$
            @Override
            public void run(){
                g.reset();
            }
        };
        ImageDescriptor refreshImage = getImageDescriptor("/icons/nav_refresh.gif"); //$NON-NLS-1$
        getViewRefresh().setImageDescriptor(refreshImage);

    }

    /**
     * Populates Animate menu.
     */
    private void createAnimateActions() {
        //Set animation mode to slow
        animationSlow = new Action(Messages.getString("CallgraphView.AnimationSlow"), IAction.AS_RADIO_BUTTON){ //$NON-NLS-1$
            @Override
            public void run(){
                g.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
                this.setChecked(true);
                animationSlow.setChecked(true);
                animationFast.setChecked(false);
            }
        };

        animationSlow.setChecked(true);

        //Set animation mode to fast
        animationFast = new Action(Messages.getString("CallgraphView.AnimationFast"), IAction.AS_RADIO_BUTTON){ //$NON-NLS-1$
            @Override
            public void run(){
                g.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
                animationSlow.setChecked(false);
                animationFast.setChecked(true);
            }
        };

        //Toggle collapse mode
        modeCollapsedNodes = new Action(Messages.getString("CallgraphView.CollapsedMode"), IAction.AS_CHECK_BOX){ //$NON-NLS-1$
            @Override
            public void run(){

                if (g.isCollapseMode()) {
                    g.setCollapseMode(false);
                    g.draw(g.getRootVisibleNodeNumber());
                } else {
                    g.setCollapseMode(true);
                    g.draw(g.getRootVisibleNodeNumber());
                }
            }
        };

        ImageDescriptor newImage = getImageDescriptor("icons/mode_collapsednodes.gif"); //$NON-NLS-1$
        modeCollapsedNodes.setImageDescriptor(newImage);

        limits = new Action(Messages.getString("CallgraphView.SetLimits"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
            private Spinner limit;
            private Spinner buffer;
            private Shell sh;
            @Override
            public void run() {
                sh = new Shell();
                sh.setLayout(new GridLayout());
                sh.setSize(150, 200);
                Label limitLabel = new Label(sh, SWT.NONE);
                limitLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
                limitLabel.setText(Messages.getString("CallgraphView.MaxNodes")); //$NON-NLS-1$
                limit = new Spinner(sh, SWT.BORDER);
                limit.setMaximum(5000);
                limit.setSelection(g.getMaxNodes());
                limit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

                Label bufferLabel = new Label(sh, SWT.NONE);
                bufferLabel.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
                bufferLabel.setText(Messages.getString("CallgraphView.MaxDepth")); //$NON-NLS-1$
                buffer = new Spinner(sh, SWT.BORDER);
                buffer.setMaximum(5000);
                buffer.setSelection(g.getLevelBuffer());
                buffer.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

                Button setLimit = new Button(sh, SWT.PUSH);
                setLimit.setText(Messages.getString("CallgraphView.SetValues")); //$NON-NLS-1$
                setLimit.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
                setLimit.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        boolean redraw = false;
                        if (limit.getSelection() >= 0 && buffer.getSelection() >= 0) {
                            g.setMaxNodes(limit.getSelection());
                            g.setLevelBuffer(buffer.getSelection());

                            if (g.changeLevelLimits(g.getLevelOfNode(g.getRootVisibleNodeNumber()))) {
                                SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
                                        Messages.getString("CallgraphView.BufferTooHigh"), Messages.getString("CallgraphView.BufferTooHigh"),  //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("CallgraphView.BufferMessage1") + //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage2") + //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage3") + //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage4") + g.getLevelBuffer() + //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage5") + PluginConstants.NEW_LINE + PluginConstants.NEW_LINE +   //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage6") + //$NON-NLS-1$
                                        Messages.getString("CallgraphView.BufferMessage7")); //$NON-NLS-1$
                                mess.schedule();
                            }

                            redraw = true;
                        }
                        sh.dispose();

                        if (redraw) {
                            g.draw();
                        }
                    }

                });


                sh.open();            }
        };

    }

/**
 * Convenience method for creating all the various actions
 */
    private void createActions() {
        createViewActions();
        createAnimateActions();
        createMarkerActions();
        createMovementActions();

        modeCollapsedNodes.setChecked(true);

    }

    private void createMovementActions() {
        gotoNext = new Action(Messages.getString("CallgraphView.Next")) { //$NON-NLS-1$
            @Override
            public void run() {
                g.drawNextNode();
            }
        };

        gotoPrevious = new Action(Messages.getString("CallgraphView.Previous")) { //$NON-NLS-1$
            @Override
            public void run() {
                if (g.isCollapseMode()) {
                    g.setCollapseMode(false);
                }
                int toDraw = g.getPreviousCalledNode(g.getRootVisibleNodeNumber());
                if (toDraw != -1)
                    g.draw(toDraw);
            }
        };

        gotoLast = new Action(Messages.getString("CallgraphView.Last")) { //$NON-NLS-1$
            @Override
            public void run() {
                if (g.isCollapseMode())
                    g.setCollapseMode(false);
                g.draw(g.getLastFunctionCalled());
            }
        };

        play = new Action(Messages.getString("CallgraphView.Play")) { //$NON-NLS-1$
            @Override
            public void run() {
                if (g.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_AGGREGATE) {
                    g.play();
                    togglePlayImage();
                }
            }
        };
        play.setImageDescriptor(playImage);
    }

    /**
     * Toggles the play/pause image
     * @param play
     */
    private void togglePlayImage() {
        if (play.getToolTipText() == Messages.getString("CallgraphView.Pause")) { //$NON-NLS-1$
            play.setImageDescriptor(playImage);
            play.setToolTipText(Messages.getString("CallgraphView.Play")); //$NON-NLS-1$
        }
        else {
            play.setImageDescriptor(pauseImage);
            play.setToolTipText(""); //$NON-NLS-1$
        }
    }

    private void createMarkerActions() {
        markersNext = new Action(Messages.getString("CallgraphView.nextMarker")) { //$NON-NLS-1$
            @Override
            public void run() {
                g.draw(g.getNextMarkedNode());
            }
        };

        markersPrevious = new Action(Messages.getString("CallgraphView.previousMarker")) { //$NON-NLS-1$
            @Override
            public void run() {
                g.draw(g.getPreviousMarkedNode());
            }
        };
    }

    @Override
    protected boolean createOpenAction() {
        //Opens from specified location
        openFile = new Action(Messages.getString("CallgraphView.Open")){ //$NON-NLS-1$
            @Override
            public void run(){
                FileDialog dialog = new FileDialog(new Shell(), SWT.DEFAULT);
                String filePath =  dialog.open();
                if (filePath != null){
                    StapGraphParser new_parser = new StapGraphParser();
                    new_parser.setSourcePath(filePath);
                        new_parser.setViewID(CallGraphConstants.VIEW_ID);
                    new_parser.schedule();
                }
            }
        };
        return true;
    }


    @Override
    protected boolean createOpenDefaultAction() {
        //Opens from the default location
        openDefault = new Action(Messages.getString("CallgraphView.OpenLastRun")){ //$NON-NLS-1$
            @Override
            public void run(){
                StapGraphParser new_parser = new StapGraphParser();
                new_parser.setViewID(CallGraphConstants.VIEW_ID);
                new_parser.schedule();
            }
        };

        return true;
    }

    @Override
    public boolean setParser(SystemTapParser newParser) {
        if (newParser instanceof StapGraphParser) {
            parser = (StapGraphParser) newParser;
            return true;
        }
        return false;

    }

    @Override
    public void setViewID() {
        viewID = "org.eclipse.linuxtools.callgraph.callgraphview";         //$NON-NLS-1$
    }

    public  Action getAnimationSlow() {
        return animationSlow;
    }

    public  Action getAnimationFast() {
        return animationFast;
    }

    public  Action getModeCollapsednodes() {
        return modeCollapsedNodes;
    }

    public  Action getViewRefresh() {
        return viewRefresh;
    }

    public  Action getGotoNext() {
        return gotoNext;
    }

    public  Action getGotoPrevious() {
        return gotoPrevious;
    }

    public  Action getGotoLast() {
        return gotoLast;
    }

    public  Action getViewTreeview() {
        return viewTreeview;
    }

    public  Action getViewRadialview() {
        return viewRadialview;
    }

    public  Action getViewAggregateview() {
        return viewAggregateview;
    }

    public  Action getViewLevelview() {
        return viewLevelview;
    }

    public Action getPlay() {
        return play;
    }

    public StapGraph getGraph() {
        return g;
    }

    @Override
    public void setFocus() {
        if(masterComposite != null){
            masterComposite.setFocus();
        }
    }


    @Override
    public void updateMethod() {
        IProgressMonitor m = new NullProgressMonitor();
        m.beginTask("Updating callgraph", 4); //$NON-NLS-1$

        loadData(m);
        m.worked(1);
        if (parser.totalTime > 0) {
            finishLoad(m);
        }
        m.worked(1);

        g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW, g.getFirstUsefulNode());
    }

    @Override
    public SystemTapParser getParser() {
        return parser;
    }

    private void writeToDot(boolean mode, Set<Integer> keySet) {
        Shell sh = new Shell();
        FileDialog dialog = new FileDialog(sh, SWT.SAVE);

        String filePath = dialog.open();

        if (filePath != null) {
            File f = new File(filePath);
            f.delete();
            try {
                f.createNewFile();
            } catch (IOException e) {
                return;
            }

            try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
                StringBuilder build = new StringBuilder(""); //$NON-NLS-1$

                out.write("digraph stapgraph {\n"); //$NON-NLS-1$
                for (int i : keySet) {
                    if (i == 0) {
                        continue;
                    }
                    StapData d = g.getNodeData(i);
                    if ( (d.isCollapsed != mode) && !d.isOnlyChildWithThisName()) {
                        continue;
                    }
                    build.append(i + " [label=\"" + d.name + " " ); //$NON-NLS-1$ //$NON-NLS-2$
                    build.append(StapNode.numberFormat.format((float) d.getTime()/g.getTotalTime() * 100) + "%\"]\n"); //$NON-NLS-1$
                    int j = d.parent;
                    if (mode) {
                        j = d.collapsedParent;
                    }

                    if (!keySet.contains(j) || j == 0) {
                        continue;
                    }

                    String called = mode ? " [label=\"" + g.getNodeData(i).timesCalled + "\"]\n" : "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    build.append( j + "->" + i ); //$NON-NLS-1$
                    build.append( called );
                    out.write(build.toString());
                    build.setLength(0);
                }
                out.write("}"); //$NON-NLS-1$
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(CallGraphConstants.PLUGIN_ID, path);
    }


}