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
package org.eclipse.linuxtools.internal.callgraph.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * Class to create / manipulate Views
 */
public class ViewFactory {

	private static List<IViewPart> views;
	private static SystemTapView newView;

	/**
	 * Create a view of type designated by the viewID argument
	 * @param viewID : A string corresponding to a type of View
	 * @return : The view object that corresponds to the viewID
	 */
	public static SystemTapView createView(final String viewID) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IViewPart view = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().
					showView(viewID);
					if (!(view instanceof SystemTapView)) {
						return;
					}

					newView = ((SystemTapView) view);
					newView.setViewID();
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});

		addView(newView);
		return newView;
	}

	/**
	 * Create a view of type designated by the viewID argument
	 * @param viewID : A string corresponding to a type of View
	 * @return : The view object that corresponds to the viewID
	 */
	public static SystemTapView createView(final String viewID, final String secondaryID) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					IViewPart view = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().showView(viewID, secondaryID, IWorkbenchPage.VIEW_VISIBLE);
					if (!(view instanceof SystemTapView)) {
						return;
					}
					newView = ((SystemTapView) view);
					newView.setViewID();
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});

		addView(newView);
		return newView;
	}


	/**
	 * Adds a view to the factory's list of active SystemTapViews.
	 */
	public static void addView(SystemTapView view) {
		if (views == null) {
			views = new ArrayList<>();
		}
		views.add(view);
	}
}
