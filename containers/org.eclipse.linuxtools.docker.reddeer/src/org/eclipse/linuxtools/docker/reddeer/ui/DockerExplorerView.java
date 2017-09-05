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

package org.eclipse.linuxtools.docker.reddeer.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.NewDockerConnectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.AuthenticationMethod;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.jface.exception.JFaceLayerException;
import org.eclipse.reddeer.jface.handler.TreeViewerHandler;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;

/**
 * 
 * @author jkopriva@redhat.com, mlabuda@redhat.com
 * @contributor adietish@redhat.com
 *
 */
public class DockerExplorerView extends WorkbenchView {

	public static final String SCHEME_TERMINATOR = "://";
	public static final String SCHEME_TCP = "tcp";
	public static final String SCHEME_HTTP = "http";
	private TreeViewerHandler treeViewerHandler = TreeViewerHandler.getInstance();

	public DockerExplorerView() {
		super("Docker", "Docker Explorer");
	}

	/**
	 * Gets names of all docker connections present in docker explorer.
	 * 
	 * @return list of docker connections names
	 */
	public List<String> getDockerConnectionNames() {
		activate();
		List<String> connectionsNames = new ArrayList<String>();
		try {
			List<TreeItem> connections = new DefaultTree().getItems();
			for (TreeItem item : connections) {
				connectionsNames.add(getName(item));
			}
		} catch (CoreLayerException ex) {
			// no connections in view
		}
		return connectionsNames;
	}

	private String getName(TreeItem item) {
		return treeViewerHandler.getNonStyledText(item);
	}

	private String getHost(TreeItem item) {
		String[] styledTexts = treeViewerHandler.getStyledTexts(item);
		if (styledTexts == null || styledTexts.length == 0) {
			return null;
		}
		return styledTexts[0].replaceAll("[\\(\\)]", "");
	}

	public boolean connectionExistForName(String connectionName) {
		return getDockerConnectionByName(connectionName) != null;
	}

	public boolean connectionExistForHost(String host) {
		return getDockerConnectionByHost(host) != null;
	}

	public void refreshView() {
		List<String> connections = getDockerConnectionNames();
		for (String connection : connections) {
			getDockerConnectionByName(connection).refresh();
		}
	}

	/**
	 * Creates a docker connection connected to a docker daemon through Search
	 * Connection socket with name "default".
	 * 
	 */
	public void createDockerConnectionSearch(String connectionName) {
		activate();
		NewDockerConnectionPage connectionWizard = new NewDockerConnectionPage();
		connectionWizard.open();
		connectionWizard.search(connectionName);
		connectionWizard.finish();
	}

	/**
	 * Creates a docker connection connected to a docker daemon through unix
	 * socket with name "default".
	 * 
	 * @param unixSocket
	 *            unix socket of a docker daemon
	 */
	public void createDockerConnectionUnix(String connectionName, String unixSocket) {
		createDockerConnection(AuthenticationMethod.UNIX_SOCKET, unixSocket, null, connectionName);
	}

	/**
	 * Creates a docker connection connected with IDockerConnection.
	 * (used with mockito)
	 * 
	 * @param connections
	 *            IDockerConnections 
	 */
	
	public void createDockerConnectionUnix(final IDockerConnection... connections) {
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connections);
		DockerConnectionManager.getInstance().setConnectionStorageManager(connectionStorageManager);
	}

	/**
	 * Creates a docker connection connected to a docker daemon through TCP with
	 * name "default".
	 * 
	 * @param tcpURI
	 *            TCP URI
	 * @param certificatePath
	 *            path to a certificate
	 */
	public void createDockerConnectionURI(String connectionName, String tcpURI, String certificatePath) {
		createDockerConnection(AuthenticationMethod.TCP_CONNECTION, tcpURI, certificatePath, connectionName);
	}

	/**
	 * Creates a docker connection connected to a docker daemon through TCP or
	 * unix socket with a specified name.
	 * 
	 * @param authMethod
	 *            unix socket or TCP URI
	 * @param unixSocketOrTcpURI
	 *            unix socket path or TCP URI
	 * @param certificatePath
	 *            path to a certificate if exists
	 * @param connectionName
	 *            docker connection name
	 */
	public void createDockerConnection(AuthenticationMethod authMethod, String unixSocketOrTcpURI,
			String authentificationCertificatePath, String connectionName) {

		activate();
		NewDockerConnectionPage connectionWizard = new NewDockerConnectionPage();
		connectionWizard.open();
		connectionWizard.setConnectionName(connectionName);
		if (AuthenticationMethod.TCP_CONNECTION.equals(authMethod)) {
			connectionWizard.setTcpConnection(unixSocketOrTcpURI, authentificationCertificatePath, false);
		} else if (AuthenticationMethod.UNIX_SOCKET.equals(authMethod)) {
			connectionWizard.setUnixSocket(unixSocketOrTcpURI);
		}
		connectionWizard.finish();
	}

	/**
	 * Gets docker connection with specific name or null if does not exists.
	 * 
	 * @return DockerConnection with specific name or null if does not exist.
	 */
	public DockerConnection getDockerConnectionByName(String connectionName) {
		activate();
		try {
			return new DockerConnection(treeViewerHandler.getTreeItem(new DefaultTree(), connectionName));
		} catch (JFaceLayerException ex) {
			return null;
		}
	}

	public DockerConnection getDockerConnectionByHost(String host) {
		activate();
		try {
			List<TreeItem> connections = new DefaultTree().getItems();
			for (TreeItem item : connections) {
				if (equalHosts(host, getHost(item))) {
					return new DockerConnection(item);
				}
			}
		} catch (CoreLayerException ex) {
			// no connections in view
		}
		return null;
	}

	/**
	 * Returns {@code true} if the 2 given hosts are equal. TCP and HTTP schemes
	 * are considered as equivalent.
	 * 
	 * @param host1
	 * @param host2
	 * @return returns true if the host1 is equal to host2
	 */
	private boolean equalHosts(String host1, String host2) {
		if (host1 == null) {
			return host2 == null;
		}

		if (host1.equals(host2)) {
			return true;
		}

		int schemeIndex1 = host1.indexOf(':');
		if (schemeIndex1 >= 0) {
			int schemeIndex2 = host2.indexOf(SCHEME_TERMINATOR);
			if (schemeIndex2 >= 0) {
				String scheme1 = host1.substring(0, schemeIndex1);
				String scheme2 = host2.substring(0, schemeIndex2);
				if ((SCHEME_HTTP.equals(scheme1) || SCHEME_TCP.equals(scheme1))
						&& (SCHEME_HTTP.equals(scheme2) || SCHEME_TCP.equals(scheme2))) {
					String hostAddr1 = host1.substring(schemeIndex1 + SCHEME_TERMINATOR.length(), host1.length());
					String hostAddr2 = host2.substring(schemeIndex2 + SCHEME_TERMINATOR.length(), host2.length());
					return hostAddr1.equals(hostAddr2);
				}
			}
		}
		return false;
	}

}
