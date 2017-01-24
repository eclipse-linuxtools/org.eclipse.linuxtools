/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.integration.tests.container.VolumeMountTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.perspective.DockerPerspective;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.AuthenticationMethod;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerConnection;
import org.jboss.reddeer.core.exception.CoreLayerException;
import org.jboss.reddeer.core.handler.ShellHandler;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.views.properties.PropertiesView;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.text.LabeledText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */
@RunWith(RedDeerSuite.class)
@OpenPerspective(DockerPerspective.class)
public abstract class AbstractDockerBotTest {

	private static final String JOB_PUSHING_DOCKER_IMAGE = "Pushing Docker Image";
	private static final String JOB_TAGGING_IMAGE = "Tagging Image";
	private static final String JOB_PULLING_DOCKER_IMAGE = "Pulling docker image";
	private static final String JOB_OPENNING_CONNECTION = "Opening connection...";
	private static final String SYSPROP_DOCKER_MACHINE_NAME = "dockerMachineName";
	private static final String SYSPROP_DOCKER_SERVER_URI = "dockerServerURI";
	private static final String SYSPROP_UNIX_SOCKET = "unixSocket";
	protected static final String SYSPROP_MOCKITO = "dockerMockito";

	protected static final String DEFAULT_CONNECTION_NAME = "default";

	protected DockerConnection connection = null;

	@BeforeClass
	public static void beforeClass() {
		deleteAllConnections();
	}

	@AfterClass
	public static void afterClass() {
		killRunningImageJobs();
		cleanupShells();
	}

	protected static void cleanupShells() {
		ShellHandler.getInstance().closeAllNonWorbenchShells();
	}

	protected List<String> getIds(String stringWithIds) {
		if (StringUtils.isBlank(stringWithIds)) {
			return Collections.emptyList();
		}
		ArrayList<String> idList = new ArrayList<String>();
		idList = new ArrayList<String>(Arrays.asList(stringWithIds.split("\\r?\\n")));
		return idList;
	}

	/**
	 * Creates a connection with the settings in this test. Stores it in
	 * instance variable {@link #connection}.
	 * 
	 * @returns the connection that was creates
	 * 
	 * @see #SYSPROP_DOCKER_MACHINE_NAME
	 * @see #SYSPROP_DOCKER_SERVER_URI
	 * @see #SYSPROP_UNIX_SOCKET
	 */
	protected DockerConnection createConnection() {
		DockerExplorerView dockerView = new DockerExplorerView();
		dockerView.open();
		String dockerMachineName = System.getProperty(SYSPROP_DOCKER_MACHINE_NAME);
		String dockerServerURI = System.getProperty(SYSPROP_DOCKER_SERVER_URI);
		String unixSocket = System.getProperty(SYSPROP_UNIX_SOCKET);
		String mockito = System.getProperty(SYSPROP_MOCKITO);
		if (!StringUtils.isBlank(dockerMachineName)) {
			dockerView.createDockerConnectionSearch(dockerMachineName);
			this.connection = getConnectionByName(dockerMachineName);
		} else if (!StringUtils.isEmpty(dockerServerURI)) {
			dockerView.createDockerConnection(AuthenticationMethod.TCP_CONNECTION, dockerServerURI, null,
					dockerServerURI);
			this.connection = getConnectionByHost(dockerServerURI);
		} else if (!StringUtils.isEmpty(unixSocket)) {
			dockerView.createDockerConnection(AuthenticationMethod.UNIX_SOCKET, unixSocket, null, unixSocket);
			this.connection = getConnectionByHost(unixSocket);
		} else if ("true".equals(mockito)) {
			MockUtils.createDockerMockConnection(DEFAULT_CONNECTION_NAME);
			this.connection = getConnectionByName(DEFAULT_CONNECTION_NAME);
		} else {
			fail("Cannot create a docker connection. " + "Neither " + SYSPROP_DOCKER_MACHINE_NAME + " nor "
					+ SYSPROP_DOCKER_SERVER_URI + " nor " + SYSPROP_UNIX_SOCKET + " were defined nor, "
					+ SYSPROP_MOCKITO + " was enabled.");
		}

		// can't be null, fails before
		connection.enableConnection();
		return connection;
	}

	protected void deleteConnection() {
		getConnection().removeConnection();
		this.connection = null;
	}

	protected String createURL(String tail) {
		String dockerServerURI = System.getProperty(SYSPROP_DOCKER_SERVER_URI);
		String serverURI = null;
		if (!StringUtils.isBlank(System.getProperty(SYSPROP_DOCKER_MACHINE_NAME))
				|| !StringUtils.isBlank(System.getProperty(SYSPROP_UNIX_SOCKET))
				|| StringUtils.isBlank(dockerServerURI)) {
			serverURI = "http://localhost:1234";
		} else if (!StringUtils.isBlank(dockerServerURI)) {
			serverURI = dockerServerURI.replaceAll(DockerExplorerView.SCHEME_TCP, DockerExplorerView.SCHEME_HTTP);
		}
		return serverURI.substring(0, serverURI.lastIndexOf(":")) + tail;
	}

	protected List<String> getImages(String dockerServer) {
		return getConnection().getImagesNames();
	}

	protected List<String> getContainers(String dockerServer) {
		return getConnection().getContainersNames();
	}

	protected void cleanUpWorkspace() {
		cleanupShells();
		killRunningImageJobs();
	}

	private String getServer() {
		if (!StringUtils.isBlank(System.getProperty(SYSPROP_DOCKER_SERVER_URI))) {
			return System.getProperty(SYSPROP_DOCKER_SERVER_URI);
		} else if (!StringUtils.isBlank(System.getProperty(SYSPROP_UNIX_SOCKET))) {
			return System.getProperty(SYSPROP_UNIX_SOCKET);
		} else {
			return DEFAULT_CONNECTION_NAME;
		}
	}

	/**
	 * Returns {@code true} if the configuration for this test is set to use a
	 * host as docker server (unix socket, serverURI). Returns {@code false} if
	 * the configuration for this test is set to use a name as docker server
	 * (docker-machine)
	 * 
	 * @return
	 */
	private boolean isDockerServerHost() {
		return !StringUtils.isBlank(System.getProperty(SYSPROP_DOCKER_SERVER_URI))
				|| !StringUtils.isBlank(System.getProperty(SYSPROP_UNIX_SOCKET));
	}

	public static void setSecureStorage(String password) {
		try {
			new DefaultShell("Secure Storage Password");
			new LabeledText("Password:").setText(password);
			new LabeledText("Confirm password:").setText(password);
			new PushButton("OK").click();
			new DefaultShell("Secure Storage - Password Hint Needed");
			new PushButton("NO").click();
		} catch (CoreLayerException ex) {
			new PushButton("OK").click();
		} catch (SWTLayerException e) {
			try {
				new DefaultShell("Secure Storage");
				new LabeledText("Password:").setText(password);
				new LabeledText("Confirm password:").setText(password);
				new PushButton("OK").click();
			} catch (SWTLayerException ex) {
				// Secure storage password is set
			} catch (CoreLayerException ex) {
				// Confirm password
				new PushButton("OK").click();
			}
		}

	}

	/**
	 * Kills all running jobs that are pulling, tagging or pushing images.
	 */
	public static void killRunningImageJobs() {
		Job[] currentJobs = Job.getJobManager().find(null);
		for (Job job : currentJobs) {
			String jobName = job.getName();
			if (jobName.startsWith(JOB_PULLING_DOCKER_IMAGE) || jobName.startsWith(JOB_TAGGING_IMAGE)
					|| jobName.startsWith(JOB_PUSHING_DOCKER_IMAGE) || jobName.startsWith(JOB_OPENNING_CONNECTION)) {
				job.cancel();
			}
		}
	}

	/**
	 * Returns a connection that matches the current settings. If none is found,
	 * a new one is created.
	 * 
	 * @return
	 * 
	 * @see #createConnection()
	 */
	protected DockerConnection getConnection() {
		if (connection != null) {
			return connection;
		}

		if (isDockerServerHost()) {
			this.connection = getConnectionByHost(getServer());
		} else if (!mockitoIsUsed()) {
			this.connection = getConnectionByName(getServer());
		}

		if (connection == null) {
			createConnection();
		}
		connection.enableConnection();
		return connection;
	}

	protected static DockerConnection getConnectionByName(String name) {
		DockerConnection connection = new DockerExplorerView().getDockerConnectionByName(name);
		return connection;
	}

	private DockerConnection getConnectionByHost(String host) {
		DockerConnection connection = new DockerExplorerView().getDockerConnectionByHost(host);
		return connection;
	}

	protected static void deleteAllConnections() {
		for (String name : new DockerExplorerView().getDockerConnectionNames()) {
			getConnectionByName(name).removeConnection();
		}
	}

	protected void clearConsole() {
		ConsoleView cview = new ConsoleView();
		cview.open();
		try {
			cview.clearConsole();
		} catch (CoreLayerException ex) {
			// swallow intentionally
		}
	}

	protected PropertiesView openPropertiesTab(String tabName) {
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().select();
		propertiesView.selectTab(tabName);
		return propertiesView;
	}

	protected PropertiesView openPropertiesTabForImage(String tabName, String imageName) {
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().getImage(imageName).select();
		propertiesView.selectTab(tabName);
		return propertiesView;
	}

	protected PropertiesView openPropertiesTabForContainer(String tabName, String containerName) {
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().getContainer(containerName).select();
		propertiesView.selectTab(tabName);
		return propertiesView;
	}

	protected String getResourceAsString(String path) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(VolumeMountTest.class.getResourceAsStream("/" + path), out);
		return new String(out.toByteArray());
	}

	/**
	 * Returns {@code true} if the running docker daemon matches at least the
	 * given major and minor version. Returns {@code false} otherwise.
	 * 
	 * @param majorVersion
	 * @param minorVersion
	 * @return
	 */
	protected boolean isDockerDaemon(int majorVersion, int minorVersion) {
		PropertiesView infoTab = openPropertiesTab("Info");
		getConnection().select();
		String daemonVersion = infoTab.getProperty("Version").getPropertyValue();
		assertTrue("Could not retrieve docker daemon version.", !StringUtils.isBlank(daemonVersion));
		String[] versionComponents = daemonVersion.split("\\.");
		assertTrue("Could not evaluate docker daemon version " + daemonVersion,
				versionComponents == null || versionComponents.length >= 2);
		int actualMajorVersion = Integer.parseInt(versionComponents[0]);
		if (actualMajorVersion > majorVersion) {
			return true;
		}
		int actualMinorVersion = Integer.parseInt(versionComponents[1]);
		return actualMinorVersion >= minorVersion;
	}

	protected boolean mockitoIsUsed() {
		String mockito = System.getProperty(SYSPROP_MOCKITO);
		return "true".equals(mockito);
	}

	protected void prepareConnections() {
		deleteAllConnections();
		if (mockitoIsUsed()) {
			MockUtils.createDockerMockConnection(DEFAULT_CONNECTION_NAME);
		} else {
			getConnection();
		}
	}

}