/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/**
 * Base {@link WizardPage} for {@link ImagePushPage} and {@link ImagePullPage}
 * 
 * @param <M>
 *            the type of model associated with this {@link WizardPage}
 */
public abstract class ImagePullPushPage<M extends ImagePullPushPageModel>
		extends WizardPage {

	protected final DataBindingContext dbc;
	protected final String DOCKER_DAEMON_DEFAULT = AbstractRegistry.DOCKERHUB_REGISTRY;

	private final M model;

	/**
	 * Constructor.
	 * 
	 * @param pageName
	 *            the name of the page
	 * @param title
	 *            the title of the page
	 * @param model
	 *            the databinding model associated with this page
	 * 
	 */
	public ImagePullPushPage(final String pageName, final String title,
			final M model) {
		super(pageName, title, SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		this.dbc = new DataBindingContext();
		this.model = model;
	}

	M getModel() {
		return this.model;
	}

	@SuppressWarnings("unchecked")
	IObservableValue<IRegistry> createRegistrySelectionControls(
			Composite parent) {
		// registry selection
		final Label accountLabel = new Label(parent, SWT.NULL);
		accountLabel.setText(WizardMessages
				.getString("ImagePullPushPage.registry.account.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(accountLabel);
		final Combo registryAccountCombo = new Combo(parent,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		registryAccountCombo.setToolTipText(WizardMessages
				.getString("ImagePullPushPage.registry.account.desc")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(registryAccountCombo);
		final ComboViewer registryAccountComboViewer = new ComboViewer(
				registryAccountCombo);
		registryAccountComboViewer
				.setContentProvider(ArrayContentProvider.getInstance());
		registryAccountComboViewer
				.setLabelProvider(new RegistryAccountLabelProvider());
		final List<IRegistry> allRegistryAccounts = getRegistryAccounts();
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		// Calculate selected registry account to be the last one used
		// or else default to the first in the list
		IRegistry defaultRegistry = null;
		String lastRegistry = store
				.getString(PreferenceConstants.LAST_REGISTRY_ACCOUNT);
		if (!allRegistryAccounts.isEmpty()) {
			defaultRegistry = allRegistryAccounts.get(0);
		}
		IRegistry selectedRegistry = allRegistryAccounts.stream()
				.filter(x -> ((RegistryInfo) x).getRegistryId()
						.equals(lastRegistry))
				.findFirst()
				.orElse(defaultRegistry);
		registryAccountComboViewer.setInput(allRegistryAccounts);
		if (selectedRegistry != null) {
			getModel().setSelectedRegistry(selectedRegistry);
		}
		final IObservableValue<IRegistry> registryAccountObservable = BeanProperties
				.value(ImagePushPageModel.class,
						ImagePullPushPageModel.SELECTED_REGISTRY)
				.observe(model);
		dbc.bindValue(ViewerProperties.singleSelection().observe(
				registryAccountComboViewer), registryAccountObservable);
		// link to add registries and accounts
		final Link addRegistryLink = new Link(parent, SWT.NONE);
		addRegistryLink.setText(
				WizardMessages.getString("ImagePullPushPage.add.link")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER)
				.grab(false, false).applyTo(addRegistryLink);
		addRegistryLink.addSelectionListener(
				onAddRegistry(registryAccountComboViewer));
		return registryAccountObservable;
	}

	private SelectionListener onAddRegistry(
			final ComboViewer registryAccountComboViewer) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final RegistryAccountDialog dialog = new RegistryAccountDialog(
					getShell(),
					WizardMessages
							.getString("ImagePullPushPage.addregistry.title"), //$NON-NLS-1$
					AbstractRegistry.DOCKERHUB_REGISTRY,
					WizardMessages.getString(
							"RegistryAccountDialog.add.explanation")); ///$NON-NLS-1$
			if (dialog.open() == Window.OK) {
				RegistryAccountManager.getInstance()
						.add(dialog.getSignonInformation());
				final List<IRegistry> updatedRegistryAccounts = getRegistryAccounts();
				registryAccountComboViewer.setInput(updatedRegistryAccounts);
				// set selection on the newly created registry
				model.setSelectedRegistry(dialog.getSignonInformation());
			}
		});
	}

	/**
	 * @return all existing {@link IRegistryAccount} plus an entry at the first
	 *         position for the registry configured in the selected Docker
	 *         daemon.
	 */
	protected List<IRegistry> getRegistryAccounts() {
		// get a local copy an insert an entry at the first position for Docker
		// Hub with no credentials
		final List<IRegistry> accounts = new ArrayList<>(
				RegistryAccountManager.getInstance().getAccounts());
		accounts.add(0, new RegistryInfo(DOCKER_DAEMON_DEFAULT, true));
		return accounts;
	}

	static final class RegistryAccountLabelProvider
			extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof IRegistryAccount) {
				final IRegistryAccount registryAccount = (IRegistryAccount) element;
				final StringBuilder textBuilder = new StringBuilder();
				// only display account username if it is set.
				if (registryAccount.getUsername() != null) {
					textBuilder.append(registryAccount.getUsername())
							.append('@'); // $NON-NLS-1$
				}
				textBuilder.append(registryAccount.getServerAddress());
				return textBuilder.toString();
			}
			else if (element instanceof IRegistry) {
				final IRegistry registry = (IRegistry) element;
				return registry.getServerAddress();
			}
			return null;
		}
	}

}
