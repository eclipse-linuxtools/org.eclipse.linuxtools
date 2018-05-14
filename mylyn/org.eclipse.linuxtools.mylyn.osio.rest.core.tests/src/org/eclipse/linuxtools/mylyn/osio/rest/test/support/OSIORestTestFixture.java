/*******************************************************************************
 * Copyright (c) 2014, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.mylyn.osio.rest.test.support;

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestCore;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil.PrivilegeLevel;
import org.eclipse.mylyn.commons.sdk.util.FixtureConfiguration;
import org.eclipse.mylyn.commons.sdk.util.RepositoryTestFixture;
import org.eclipse.mylyn.commons.sdk.util.TestConfiguration;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.google.common.collect.ImmutableMap;

public class OSIORestTestFixture extends RepositoryTestFixture {

	public final String version;

	protected TaskRepository repository;

	private final OSIORestConnector connector = new OSIORestConnector();

	public static final OSIORestTestFixture DEFAULT = discoverDefault();

	private static OSIORestTestFixture discoverDefault() {
		return TestConfiguration.getDefault().discoverDefault(OSIORestTestFixture.class, "OSIORest");
	}

	private static OSIORestTestFixture current;

	public static OSIORestTestFixture current() {
		if (current == null) {
			DEFAULT.activate();
		}
		return current;
	}

	@Override
	protected OSIORestTestFixture activate() {
		current = this;
		return this;
	}

	@Override
	protected OSIORestTestFixture getDefault() {
		return DEFAULT;
	}

	public OSIORestTestFixture(FixtureConfiguration configuration) {
		super(OSIORestCore.CONNECTOR_KIND, configuration.getUrl());
		version = configuration.getVersion();
		setInfo("OSIO Rest", configuration.getVersion(), configuration.getInfo());
		setDefaultproperties(configuration.getProperties());
	}

	public String getVersion() {
		return version;
	}

	public TaskRepository repository() {
		if (repository != null) {
			return repository;
		}
		repository = new TaskRepository(getConnectorKind(), getRepositoryUrl());
		UserCredentials credentials = CommonTestUtil.getCredentials(PrivilegeLevel.USER);
		repository.setCredentials(org.eclipse.mylyn.commons.net.AuthenticationType.REPOSITORY,
				new AuthenticationCredentials(credentials.getUserName(), credentials.getPassword()), true);
		return repository;
	}

	public OSIORestConnector connector() {
		return connector;
	}

	public String getTestDataFolder() {
		return "testdata/" + getProperty("testdataVersion");
	}

	public OSIORestHarness createHarness() {
		return new OSIORestHarness(this);
	}

}
