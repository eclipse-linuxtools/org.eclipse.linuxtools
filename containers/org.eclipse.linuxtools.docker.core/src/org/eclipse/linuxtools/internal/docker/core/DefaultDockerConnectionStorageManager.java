/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Default implementation of the {@link IDockerConnectionStorageManager}.
 */
public class DefaultDockerConnectionStorageManager
		implements IDockerConnectionStorageManager {

	public final static String CONNECTIONS_FILE_NAME = "dockerconnections.xml"; //$NON-NLS-1$

	@Override
	public List<IDockerConnection> loadConnections() {
		final List<IDockerConnection> connections = new ArrayList<>();
		final IPath stateLocation = Activator.getDefault().getStateLocation();
		final File connectionFile = stateLocation.append(CONNECTIONS_FILE_NAME)
				.toFile();
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
							String key = DockerConnection.getPreferencesKey(uri,
									username);
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
								builder = builder
										.tcpCertPath(certNode.getNodeValue());
							}
						}
						DockerConnection connection = builder.build();
						connections.add(connection);
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Activator.log(e);
		}
		return connections;
	}

	@Override
	public void saveConnections(List<IDockerConnection> connections) {
		final IPath stateLocation = Activator.getDefault().getStateLocation();
		final File connectionFile = stateLocation.append(CONNECTIONS_FILE_NAME)
				.toFile();
		try {
			if (!connectionFile.exists()) {
				connectionFile.createNewFile();
			}
			try (final PrintWriter p = new PrintWriter(
					new BufferedWriter(new FileWriter(connectionFile)))) {
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
					p.println("\"/>"); //$NON-NLS-1$
				}
				p.println("</connections>"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			Activator.log(e);
		}
	}

}
