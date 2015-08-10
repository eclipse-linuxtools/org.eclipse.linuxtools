/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DockerConnectionManager {

	public final static String CONNECTIONS_FILE_NAME = "dockerconnections.xml"; //$NON-NLS-1$

	private static DockerConnectionManager instance;

	private ArrayList<IDockerConnection> connections;
	private ListenerList connectionManagerListeners;

	private DockerConnectionManager() {
		connections = new ArrayList<>();
		loadConnections();
	}

	static public DockerConnectionManager getInstance() {
		if (instance == null)
			instance = new DockerConnectionManager();

		return instance;
	}

	private void loadConnections() {
		IPath stateLocation = Activator.getDefault().getStateLocation();
		File connectionFile = stateLocation.append(CONNECTIONS_FILE_NAME)
				.toFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			if (connectionFile.exists()) {
				Document d = db.parse(connectionFile);
				Element e = d.getDocumentElement();
				// Get the stored configuration data
				NodeList connectionNodes = e.getElementsByTagName("connection"); // $NON-NLS-1$
				for (int x = 0; x < connectionNodes.getLength(); ++x) {
					Node n = connectionNodes.item(x);
					NamedNodeMap attrs = n.getAttributes();
					Node nameNode = attrs.getNamedItem("name"); //$NON-NLS-1$
					Node uriNode = attrs.getNamedItem("uri"); //$NON-NLS-1$
					Node usernameNode = attrs.getNamedItem("username"); //$NON-NLS-1$
					Node certNode = attrs.getNamedItem("cert"); //$NON-NLS-1$
					if (uriNode != null) {
						String uri = uriNode.getNodeValue();
						String name = nameNode.getNodeValue();

						if (usernameNode != null) {
							String username = usernameNode.getNodeValue();
							String key = DockerConnection.getPreferencesKey(
									uri, username);
							ISecurePreferences root = SecurePreferencesFactory
									.getDefault();
							ISecurePreferences node = root.node(key);
							@SuppressWarnings("unused")
							String password;
							try {
								password = node.get("password", null); //$NON-NLS-1$
							} catch (StorageException e1) {
								e1.printStackTrace();
							}
						}

						DockerConnection.Builder builder = new DockerConnection.Builder()
								.name(name);
						if (uri.startsWith("unix:")) { //$NON-NLS-1$
							builder = builder.unixSocket(uri);
						} else {
							builder = builder.tcpHost(uri);
							if (certNode != null) {
								String cert = certNode.getNodeValue();
								builder = builder.tcpCertPath(cert);
							}
						}
						try {
							DockerConnection connection = builder.build();
							addConnection(connection);
						} catch (DockerException e1) {
							Activator.log(e1);
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			Activator.log(e);
		} catch (SAXException e) {
			Activator.log(e);
		} catch (IOException e) {
			Activator.log(e);
		}

		/*if (connections.size() == 0) {
			// create a new connection from the UI preferences
			final IEclipsePreferences preferences = InstanceScope.INSTANCE
					  .getNode("org.eclipse.linuxtools.docker.ui"); //$NON-NLS-1$
			final int bindingMode = preferences.getInt(BINDING_MODE, UNIX_SOCKET);
			try {
				if(bindingMode == UNIX_SOCKET) {
					final String unixSocketPath = preferences.get(UNIX_SOCKET_PATH, DEFAULT_UNIX_SOCKET_PATH);
					connections.add(new DockerConnection.Builder().unixSocket(unixSocketPath).build());
				} else {
					final String tcpHost = preferences.get(TCP_HOST, null);
					final String tcpCertPath = preferences.get(TCP_CERT_PATH, null);
					connections.add(new DockerConnection.Builder().tcpHost(tcpHost).tcpCertPath(tcpCertPath).build()); //$NON-NLS-1$
				}
			} catch (DockerCertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$
		}*/
	}

	public void saveConnections() {
		try {
			IPath stateLocation = Activator.getDefault().getStateLocation();
			File ConnectionFile = stateLocation.append(CONNECTIONS_FILE_NAME)
					.toFile();
			if (!ConnectionFile.exists())
				ConnectionFile.createNewFile();
			if (ConnectionFile.exists()) {
				PrintWriter p = new PrintWriter(new BufferedWriter(
						new FileWriter(ConnectionFile)));
				p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
				p.println("<connections>"); // $NON-NLS-1$
				for (IDockerConnection d : connections) {
					p.print("<connection name=\"" + d.getName() + //$NON-NLS-1$
							"\" uri=\"" + d.getUri()); //$NON-NLS-1$
					if (d.getUsername() != null) {
						p.print("\" username=\"" + d.getUsername()); //$NON-NLS-1$
					}
					if (d.getTcpCertPath() != null) {
						p.print("\" cert=\"" + d.getTcpCertPath()); //$NON-NLS-1$
					}
					p.println("\"/>");
				}
				p.println("</connections>"); //$NON-NLS-1$
				p.close();
			}
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	public IDockerConnection[] getConnections() {
		return connections.toArray(new IDockerConnection[connections.size()]);
	}

	public IDockerConnection findConnection(String name) {
		for (IDockerConnection connection : connections) {
			if (connection.getName().equals(name))
				return connection;
		}
		return null;
	}

	public void addConnection(final IDockerConnection dockerConnection) throws DockerException {
		if(!dockerConnection.isOpen()) {
			dockerConnection.open(true);
		}
		connections.add(dockerConnection);
		saveConnections();
		notifyListeners(IDockerConnectionManagerListener.ADD_EVENT);
	}

	public void removeConnection(IDockerConnection d) {
		connections.remove(d);
		saveConnections();
		notifyListeners(IDockerConnectionManagerListener.REMOVE_EVENT);
	}

	public void notifyConnectionRename() {
		saveConnections();
		notifyListeners(IDockerConnectionManagerListener.RENAME_EVENT);
	}

	public void addConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners == null)
			connectionManagerListeners = new ListenerList(ListenerList.IDENTITY);
		connectionManagerListeners.add(listener);
	}

	public void removeConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners != null)
			connectionManagerListeners.remove(listener);
	}

	public void notifyListeners(int type) {
		if (connectionManagerListeners != null) {
			Object[] listeners = connectionManagerListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IDockerConnectionManagerListener) listeners[i])
						.changeEvent(type);
			}
		}
	}

}
