/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Andre Dietisheim
 */
public class UIUtils {

	private UIUtils() {
	}

	/**
	 * Returns the selection of the active workbench window.
	 * 
	 * @return the selection
	 * 
	 * @see IWorkbenchWindow#getSelectionService()
	 */
	public static ISelection getWorkbenchWindowSelection() {
		return getActiveWorkbenchWindow().getSelectionService().getSelection();
	}

	/**
	 * Gets the structured selection.
	 * 
	 * @return the structured selection
	 */
	public static IStructuredSelection getStructuredSelection() {
		ISelection selection = getWorkbenchWindowSelection();
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		} else {
			return null;
		}
	}

	/**
	 * Gets the first element.
	 * 
	 * @param selection
	 *            the selection
	 * @param expectedClass
	 *            the expected class
	 * 
	 * @return the first element
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFirstElement(final ISelection selection,
			final Class<T> expectedClass) {
		if (selection == null) {
			return null;
		} else {
			Assert.isTrue(selection instanceof IStructuredSelection);
			Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement();
			if (firstElement != null
					&& expectedClass.isAssignableFrom(firstElement.getClass())) {
				return (T) firstElement;
			} else {
				return null;
			}
		}
	}

	/**
	 * Gets the active page.
	 * 
	 * @return the active page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchPage workbenchPage = getActiveWorkbenchWindow()
				.getActivePage();
		Assert.isNotNull(workbenchPage);
		return workbenchPage;
	}

	/**
	 * Returns the editor that's currently active (focused).
	 * 
	 * @return the active editor
	 */
	public static IEditorPart getActiveEditor() {
		IEditorPart editor = getActivePage().getActiveEditor();
		Assert.isNotNull(editor);
		return editor;
	}

	/**
	 * Gets the active workbench window.
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbenchWindow workbenchWindow = getWorkbench()
				.getActiveWorkbenchWindow();
		Assert.isNotNull(workbenchWindow);
		return workbenchWindow;
	}

	/**
	 * Gets the workbench.
	 * 
	 * @return the workbench
	 */
	public static IWorkbench getWorkbench() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Assert.isNotNull(workbench);
		return workbench;
	}

	/**
	 * Replaces an image with the given key by the given image descriptor.
	 * 
	 * @param imageKey
	 *            the image key
	 * @param imageDescriptor
	 *            the image descriptor
	 */
	public static void replaceInJfaceImageRegistry(final String imageKey,
			final ImageDescriptor imageDescriptor) {
		Assert.isNotNull(imageKey);
		Assert.isNotNull(imageDescriptor);

		JFaceResources.getImageRegistry().remove(imageKey);
		JFaceResources.getImageRegistry().put(imageKey, imageDescriptor);
	}

	/**
	 * Register the given ContributionManager with the given id. The
	 * contribution manager gets unregistered on control disposal.
	 * 
	 * @param id
	 *            the id
	 * @param contributionManager
	 *            the contribution manager
	 * @param control
	 *            the control
	 * 
	 * @see ContributionManager
	 * @see IMenuService
	 * @see DisposeListener
	 */
	public static void registerContributionManager(final String id,
			final IContributionManager contributionManager,
			final Control control) {
		Assert.isNotNull(id);
		Assert.isNotNull(contributionManager);
		Assert.isTrue(control != null && !control.isDisposed());

		final IMenuService menuService = PlatformUI
				.getWorkbench().getService(IMenuService.class);
		menuService.populateContributionManager(
				(ContributionManager) contributionManager, id);
		contributionManager.update(true);
		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				menuService
						.releaseContributions((ContributionManager) contributionManager);
			}
		});
	}

	/**
	 * Creates context menu to a given control.
	 * 
	 * @param control
	 *            the control
	 * 
	 * @return the i menu manager
	 */
	public static IMenuManager createContextMenu(final Control control) {
		Assert.isTrue(control != null && !control.isDisposed());

		MenuManager menuManager = new MenuManager();
		menuManager
				.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		Menu menu = menuManager.createContextMenu(control);
		control.setMenu(menu);
		return menuManager;
	}

	/**
	 * Gets the dialog settings for the given identifer and plugin.
	 * 
	 * @param identifier
	 *            the identifier
	 * @param plugin
	 *            the plugin
	 * 
	 * @return the dialog settings
	 */
	public static IDialogSettings getDialogSettings(final String identifier,
			final AbstractUIPlugin plugin) {
		Assert.isNotNull(plugin);
		IDialogSettings dialogSettings = plugin.getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(identifier);
		if (section == null) {
			section = dialogSettings.addNewSection(identifier);
		}
		return section;
	}

	/**
	 * Returns the page for a given editor.
	 * 
	 * @param editor
	 *            the editor
	 * @return the page
	 * 
	 * @see IWorkbenchPage
	 */
	public static IWorkbenchPage getPage(EditorPart editor) {
		Assert.isNotNull(editor);
		IWorkbenchPartSite site = editor.getSite();
		Assert.isNotNull(site);
		return site.getPage();
	}

}
