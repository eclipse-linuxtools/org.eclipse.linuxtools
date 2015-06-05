/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public abstract class AbstractRemoteTest extends AbstractTest {
	private static final String PLUGIN_ID="org.eclipse.linuxtools.profiling.tests";
    public static final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$
    public static final String REMOTE_SERVICES = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$
    public static final String REMOTE_MAKE_NATURE = "org.eclipse.ptp.rdt.core.remoteMakeNature"; //$NON-NLS-1$
    public static final String REMOTE_MAKE_BUILDER = "org.eclipse.ptp.rdt.core.remoteMakeBuilder"; //$NON-NLS-1$
    public static final String BUILD_SERVICE = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
    public static final String CINDEX_SERVICE = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
    public static final String RDT_CINDEX_SERVICE = "org.eclipse.ptp.rdt.server.dstore.RemoteToolsCIndexServiceProvider"; //$NON-NLS-1$
    public static final String TOOLCHAIN_ID = "org.eclipse.ptp.rdt.managedbuild.toolchain.gnu.base"; //$NON-NLS-1$
    public static final String PTP_EXE = "org.eclipse.ptp.rdt.managedbuild.target.gnu.exe"; //$NON-NLS-1$
    public static final String DEBUG = "Debug"; //$NON-NLS-1$
    public static String USERNAME = ""; //$NON-NLS-1$
    private static String PASSWORD = ""; //$NON-NLS-1$
    public static final String CONNECTION_TYPE_JSCH = "org.eclipse.remote.JSch";
    public static final String SYNC_SERVICE_GIT = "org.eclipse.ptp.rdt.sync.git.core.synchronizeService";

    // Sets localhost as default connection if no remote host is given
    private static String HOST = ""; //$NON-NLS-1$
    public static String CONNECTION_NAME = "localhost"; //$NON-NLS-1$
    public static final String RESOURCES_DIR = "resources/"; //$NON-NLS-1$

    // Skip tests if there is not suitable connection details
    public static void checkConnectionInfo() {
        String host = System.getenv("TEST_HOST");
        if (host != null) {
            HOST = host;
        }
        assumeTrue("Skip remote tests due lack of host information", !HOST.isEmpty());

        String username = System.getenv("TEST_USERNAME");
        if (username != null) {
            USERNAME = username;
        }
        assumeTrue("Skip remote tests due lack of an username for connection", !USERNAME.isEmpty());

        String password = System.getenv("TEST_PASSWORD");
        if (password != null) {
            PASSWORD = password;
        }
        assumeTrue("Skip remote tests due lack of an password for connection", !PASSWORD.isEmpty());
    }

     protected void deleteResource (String directory) {
                IRemoteServicesManager sm = getServicesManager();
                IRemoteConnection conn = sm.getConnectionType("ssh").getConnection(CONNECTION_NAME);
                assertNotNull(conn);
                IRemoteFileService fileManager = conn.getService(IRemoteFileService.class);
                assertNotNull(fileManager);
                final IFileStore dstFileStore = fileManager.getResource(directory);
                try {
                    dstFileStore.delete(EFS.NONE, null);
                } catch (CoreException e) {
                }
            }
    /**
     * Create a new connection. Save the connection working copy before return.
     *
     * @param connName Connection Name
     * @param connTypeId The connection type identifier
     * @return The created remote connection
     * @throws RemoteConnectionException
     */
    protected static IRemoteConnection createJSchConnection(String connName, String connTypeId) throws RemoteConnectionException {
        checkConnectionInfo();
        IRemoteServicesManager manager = getServicesManager();
        IRemoteConnectionType ct = manager.getConnectionType(connTypeId);
        assertNotNull(ct);
        IRemoteConnectionWorkingCopy wc = ct.newConnection(connName);
        wc.setAttribute(JSchConnection.ADDRESS_ATTR, HOST);
        wc.setAttribute(JSchConnection.USERNAME_ATTR, USERNAME);
        wc.setSecureAttribute(JSchConnection.PASSWORD_ATTR, PASSWORD);
        IRemoteConnection conn = wc.save();
        assertNotNull(conn);
        conn.open(new NullProgressMonitor());
        assertTrue(conn.isOpen());
        return conn;
    }

    /**
     * Delete connection
     *
     * @param conn The connection
     * @throws RemoteConnectionException
     */
    protected static void deleteConnection(IRemoteConnection conn) throws RemoteConnectionException {
        IRemoteConnectionType ct = conn.getConnectionType();
        ct.removeConnection(conn);
    }

    /**
     * Prepare a sync project from an already available local project
     *
     * @param project any local project
     * @param conn remote connection
     * @param location sync'ed folder path in remote machine
     * @throws CoreException
     */
    protected static void convertToSyncProject(IProject project, IRemoteConnection conn, String location) throws CoreException {
        // Convert to sync project without file filters
        SyncManager.makeSyncProject(project, conn.getName() + "_sync", SYNC_SERVICE_GIT, conn, location, null);
        // Synchronize project from local to remote
        SyncManager.sync(null, project, SyncFlag.LR_ONLY, null);
    }

    /**
     * Get the *active* synchronize configuration associated with the project
     *
     * @param project A sync project
     * @return the active synchronize configuration
     */
    protected static SyncConfig getSyncConfig(IProject project) {
        return SyncConfigManager.getActive(project);
    }

    private static IRemoteServicesManager getServicesManager() {
		BundleContext context = Platform.getBundle(PLUGIN_ID).getBundleContext();
		ServiceReference<IRemoteServicesManager> ref = context.getServiceReference(IRemoteServicesManager.class);
		assertNotNull(ref);
		return context.getService(ref);
    }

}
