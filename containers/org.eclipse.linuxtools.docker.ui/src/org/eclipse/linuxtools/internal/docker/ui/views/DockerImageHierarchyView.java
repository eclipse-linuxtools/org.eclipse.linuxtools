/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHiearchyNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * 
 */
public class DockerImageHierarchyView extends CommonNavigator
		implements ITabbedPropertySheetPageContributor {

	/** the id of the view as defined in {@code plugin.xml}. */
	public static final String VIEW_ID = "org.eclipse.linuxtools.docker.ui.dockerImageHierarchyView"; //$NON-NLS-1$

	private Control hierarchyPane;
	private Control explanationsPane;
	private PageBook pageBook;

	private IDockerImageHiearchyNode selectedImageHierarchy = null;

	@Override
	protected Object getInitialInput() {
		return this.selectedImageHierarchy;
	}

	/**
	 * Shows the given resolved hierarchy associated with the selected
	 * {@link IDockerImage}.
	 * 
	 * @param selectedImageHierarchy
	 *            the hierarchy to display in this view
	 */
	public void show(final IDockerImageHiearchyNode selectedImageHierarchy) {
		this.selectedImageHierarchy = selectedImageHierarchy;
		this.getCommonViewer().setInput(
				new DockerImageHiearchy(this.selectedImageHierarchy.getRoot()));
		this.getCommonViewer().expandAll();
		showHierarchyOrExplanations();
	}

	@Override
	public void createPartControl(final Composite parent) {
		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		this.pageBook = new PageBook(parent, SWT.NONE);
		this.hierarchyPane = createHierarchyPane(pageBook, toolkit);
		this.explanationsPane = createExplanationPane(pageBook, toolkit);
		showHierarchyOrExplanations();
	}

	private Control createExplanationPane(final PageBook pageBook,
			final FormToolkit toolkit) {
		final Form form = toolkit.createForm(pageBook);
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5)
				.applyTo(container);
		final Label label = new Label(container, SWT.NONE);
		label.setText(
				DVMessages.getString("DockerHierarchyViewNoImageSelected.msg")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL)
				.grab(true, false).applyTo(label);
		return form;
	}

	private Control createHierarchyPane(final PageBook pageBook,
			final FormToolkit toolkit) {
		final Form form = toolkit.createForm(pageBook);
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5)
				.applyTo(container);
		super.createPartControl(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(getCommonViewer().getControl());
		return form;
	}

	@Override
	protected CommonViewer createCommonViewer(final Composite parent) {
		final CommonViewer viewer = super.createCommonViewer(parent);
		setLinkingEnabled(false);
		return viewer;
	}

	@Override
	public String getContributorId() {
		return getSite().getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (IPropertySheetPage.class.isAssignableFrom(adapter)) {
			return (T) new TabbedPropertySheetPage(this, true);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void dispose() {

	}

	/**
	 * Shows the {@link DockerExplorerView#explanationsPane} or the
	 * {@link DockerExplorerView#hierarchyPane} depending on the number of
	 * connections in the {@link DockerConnectionManager}.
	 */
	public void showHierarchyOrExplanations() {
		if (this.selectedImageHierarchy == null) {
			pageBook.showPage(explanationsPane);
		} else {
			pageBook.showPage(hierarchyPane);
		}
	}

	static class DockerImageHiearchy {

		private final IDockerImageHiearchyNode root;

		public DockerImageHiearchy(final IDockerImageHiearchyNode root) {
			this.root = root;
		}

		public IDockerImageHiearchyNode getRoot() {
			return this.root;
		}
	}
}
