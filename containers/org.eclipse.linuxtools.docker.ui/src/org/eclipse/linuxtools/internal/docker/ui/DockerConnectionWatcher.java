/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Singleton class to track current connection set by Docker Explorer View
 * 
 * @author jjohnstn
 *
 */
public class DockerConnectionWatcher implements ISelectionListener {

	public final static String CONNECTION_FILE_NAME = "dockerselectedconnection.xml"; //$NON-NLS-1$

	private static DockerConnectionWatcher instance;
	private IDockerConnection connection;

	public static DockerConnectionWatcher getInstance() {
		if (instance == null) {
			instance = new DockerConnectionWatcher();
		}
		return instance;
	}

	private DockerConnectionWatcher() {
		// track selection changes in the Docker Explorer view (only)
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService()
				.addSelectionListener(DockerExplorerView.VIEW_ID, this);
		IDockerConnection selectedConnection = loadConnection();
		if (selectedConnection != null) {
			this.connection = selectedConnection;
		}
	}

	public void dispose() {
		// stop tracking selection changes in the Docker Explorer view (only)
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService()
				.removeSelectionListener(DockerExplorerView.VIEW_ID, this);
	}

	/**
	 * Set the current connection
	 * 
	 * @param connection
	 *            new connection to set
	 * 
	 */
	public void setConnection(IDockerConnection connection) {
		this.connection = connection;
		if (connection != null) {
			saveConnection(connection);
		}
	}

	// Save the current selected connection for when Eclipse restarts
	private void saveConnection(IDockerConnection connection) {
		final IPath stateLocation = Activator.getDefault().getStateLocation();
		final File connectionFile = stateLocation.append(CONNECTION_FILE_NAME)
				.toFile();
		if (!connection.isOpen()) {
			return;
		}
		try {
			if (!connectionFile.exists()) {
				connectionFile.createNewFile();
			}
			try (final PrintWriter p = new PrintWriter(
					new BufferedWriter(new FileWriter(connectionFile)))) {
				p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
				String name = connection.getName();
				if (name.equals(Messages.getString("Unnamed"))) { //$NON-NLS-1$
					name = ""; //$NON-NLS-1$
				}
				p.print("<connection name=\"" + name + //$NON-NLS-1$
						"\" uri=\"" + connection.getUri()); //$NON-NLS-1$
				if (connection.getUsername() != null) {
					p.print("\" username=\"" + connection.getUsername()); //$NON-NLS-1$
				}
				p.println("\"/>"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	// Load the previously selected connection at start-up
	private IDockerConnection loadConnection() {
		IDockerConnection connection = null;
		final IPath stateLocation = Activator.getDefault().getStateLocation();
		final File connectionFile = stateLocation.append(CONNECTION_FILE_NAME)
				.toFile();
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			if (connectionFile.exists()) {
				Document d = db.parse(connectionFile);
				Element e = d.getDocumentElement();
				// Get the stored configuration data
				NodeList connectionNodes = e.getElementsByTagName("connection"); // $NON-NLS-1$
				if (connectionNodes == null
						|| connectionNodes.getLength() == 0) {
					return null;
				}
				Node n = connectionNodes.item(0);
				NamedNodeMap attrs = n.getAttributes();
				Node nameNode = attrs.getNamedItem("name"); //$NON-NLS-1$
				Node uriNode = attrs.getNamedItem("uri"); //$NON-NLS-1$
				Node usernameNode = attrs.getNamedItem("username"); //$NON-NLS-1$
				if (uriNode != null) {
					String uri = uriNode.getNodeValue();
					String name = nameNode.getNodeValue();
					String username = null;

					if (usernameNode != null) {
						username = usernameNode.getNodeValue();
					}
					IDockerConnection[] connections = DockerConnectionManager
							.getInstance().getConnections();
					for (IDockerConnection c : connections) {
						if (c.getUri().equals(uri)) {
							if (c.getName().equals(name)) {
								if (c.getUsername() == null
										|| c.getUsername().equals(username)) {
									connection = c;
									break;
								}
							}
						}
					}
				}

			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Activator.log(e);
		}
		return connection;
	}

	/**
	 * Get the current connection
	 * 
	 * @return the current connection or <code>null</code> if none is set
	 */
	public IDockerConnection getConnection() {
		return this.connection;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty()) {
			setConnection(null);
			return;
		}
		final Object firstSegment = treeSelection.getPaths()[0]
				.getFirstSegment();
		if (firstSegment instanceof IDockerConnection) {
			setConnection((IDockerConnection) firstSegment);
		}
	}
}
