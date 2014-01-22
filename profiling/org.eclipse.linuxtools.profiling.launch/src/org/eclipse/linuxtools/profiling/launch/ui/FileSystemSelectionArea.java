/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * FileSystemSelectionArea is the area used to select the file system.
 *
 */

/**
 * @since 2.0
 */
public class FileSystemSelectionArea {

	private Label fileSystemTitle;
	private ComboViewer fileSystems;

	private static final String EXTENSION_POINT_ID = "RemoteResourceSelectorProxy"; //$NON-NLS-1$
	private static final String RESOURCE_SELECTOR_PROXY_NAME = "resourceSelectorProxy"; //$NON-NLS-1$
	private static final String SCHEME_ID = "scheme"; //$NON-NLS-1$
	private static final String SCHEME_LABEL_ID = "schemeLabel"; //$NON-NLS-1$
	private static final String IS_DEFAULT_ID = "isDefault"; //$NON-NLS-1$
	private static final String EXT_ATTR_CLASS = "class"; //$NON-NLS-1$

	private LinkedList<FileSystemElement> fsElements;

	/**
	 * Create a new instance of the receiver.
	 */
	public FileSystemSelectionArea(){
	}

	private FileSystemElement[] getSchemes() {
		if (fsElements == null) {
			fsElements = new LinkedList<>();

			// Add all of the ones declared by the registry.
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, EXTENSION_POINT_ID);
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			for (int i = 0; i < infos.length; i++) {
				IConfigurationElement configurationElement = infos[i];
				if (configurationElement.getName().equals(RESOURCE_SELECTOR_PROXY_NAME)) {
					IRemoteResourceSelectorProxy remoteSelector = null;
					try {
						Object obj = configurationElement.createExecutableExtension(EXT_ATTR_CLASS);
						if (obj instanceof IRemoteResourceSelectorProxy) {
							remoteSelector = (IRemoteResourceSelectorProxy)obj;
						}
					} catch (CoreException e) {
						ProfileLaunchPlugin.log(IStatus.ERROR, ResourceSelectorWidgetMessages.FileSystemSelectionArea_exception_while_creating_runnable_class + configurationElement.getAttribute(EXT_ATTR_CLASS), e);
					}
					FileSystemElement element = new FileSystemElement(
							configurationElement.getAttribute(SCHEME_ID),
							configurationElement.getAttribute(SCHEME_LABEL_ID),
							Boolean.valueOf(configurationElement.getAttribute(IS_DEFAULT_ID)),
							remoteSelector);
					fsElements.addLast(element);
				}
			}
		}
		return fsElements.toArray(new FileSystemElement[fsElements.size()]);
	}

	/**
	 * Create the contents of the receiver in composite.
	 * @param composite
	 */
	public void createContents(Composite composite) {

		fileSystemTitle = new Label(composite, SWT.NONE);
		fileSystemTitle.setText(ResourceSelectorWidgetMessages.fileSystemSelectionText);
		fileSystemTitle.setFont(composite.getFont());

		fileSystems = new ComboViewer(composite, SWT.READ_ONLY);
		fileSystems.getControl().setFont(composite.getFont());

		fileSystems.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				return ((FileSystemElement)element).getSchemeLabel();
			}
		});

		fileSystems.setContentProvider(new IStructuredContentProvider() {

			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			@Override
			public void dispose() {
				// Nothing to do
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			@Override
			public Object[] getElements(Object inputElement) {
				return getSchemes();
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			@Override
			public void inputChanged(org.eclipse.jface.viewers.Viewer viewer,
					Object oldInput, Object newInput) {
				// Nothing to do
			}
		});

		fileSystems.setInput(this);
		Iterator<FileSystemElement> fsEltItr = fsElements.iterator();
		boolean foundDefault = false;
		while (fsEltItr.hasNext()) {
			FileSystemElement fsElt = fsEltItr.next();
			if (fsElt.getIsDefault()) {
				if (foundDefault) {
					ProfileLaunchPlugin.log(IStatus.WARNING, ResourceSelectorWidgetMessages.FileSystemSelectionArea_found_multiple_default_extensions + fsElt.getScheme());
					// use only the first one we found marked as default
					continue;
				}
				fileSystems.setSelection(new StructuredSelection(fsElt));
				foundDefault = true;
			}
		}
	}

	/**
	 * Return the selected file system.
	 * @return FileSystemElement or <code>null</code> if nothing
	 * is selected.
	 */
	public FileSystemElement getSelectedFileSystem() {
		ISelection selection = fileSystems.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			if (structured.size() == 1) {
				return ((FileSystemElement) structured.getFirstElement());
			}
		}
		return null;
	}

	/**
	 * Set the filesystem selection combo box to the specified scheme.  If the scheme isn't
	 * legal, throw a CoreException.
	 * @param scheme name of scheme, e.g. "rse"
	 */
	public void setSelectedFileSystem(String scheme) throws CoreException {
		Iterator<FileSystemElement> fsEltItr = fsElements.iterator();
		boolean foundMatch = false;
		while (fsEltItr.hasNext()) {
			FileSystemElement fsElt = fsEltItr.next();
			if (fsElt.getScheme().equalsIgnoreCase(scheme)) {
				foundMatch = true;
				fileSystems.setSelection(new StructuredSelection(fsElt));
				break;
			}
		}
		if (!foundMatch) {
			throw new CoreException(new Status(IStatus.ERROR, ProfileLaunchPlugin.PLUGIN_ID, IStatus.OK,
					ResourceSelectorWidgetMessages.FileSystemSelectionArea_unrecognized_scheme + scheme, null));
		}
	}


	/**
	 * Set the enablement state of the widget.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		fileSystemTitle.setEnabled(enabled);
		fileSystems.getControl().setEnabled(enabled);
	}
}
