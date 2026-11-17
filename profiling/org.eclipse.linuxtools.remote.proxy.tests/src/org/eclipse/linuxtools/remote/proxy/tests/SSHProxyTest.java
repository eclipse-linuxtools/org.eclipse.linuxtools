/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.remote.proxy.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the JSch based SSH proxy against an embedded SSH server that runs
 * commands with the local shell and serves the local file system over SFTP.
 */
@Timeout(60)
public class SSHProxyTest {

	private static final String USER = "tester"; //$NON-NLS-1$

	private static SshServer server;

	@TempDir
	Path tempDir;

	@BeforeAll
	public static void startServer() throws IOException {
		server = SshServer.setUpDefaultServer();
		server.setHost("127.0.0.1"); //$NON-NLS-1$
		server.setPort(0);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		// the proxy logs in with an empty password when it can't prompt for one
		server.setPasswordAuthenticator((username, password, session) -> USER.equals(username));
		server.setCommandFactory(ProcessShellCommandFactory.INSTANCE);
		server.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
		server.start();
	}

	@AfterAll
	public static void stopServer() throws IOException {
		server.stop(true);
	}

	private static URI uri(String path) {
		return URI.create("jsch://" + USER + "@127.0.0.1:" + server.getPort() + path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String run(IRemoteCommandLauncher launcher, String command, String[] args, String[] env,
			IPath workingDir) throws CoreException, IOException, InterruptedException {
		Process process = launcher.execute(IPath.fromOSString(command), args, env, workingDir,
				new NullProgressMonitor());
		assertNotNull(process);
		String output;
		try (InputStream in = process.getInputStream()) {
			output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		assertEquals(0, process.waitFor(), "Exit code of " + command); //$NON-NLS-1$
		return output.trim();
	}

	@Test
	public void testExecuteInDirectory() throws Exception {
		IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(uri("/")); //$NON-NLS-1$
		assertEquals(tempDir.toString(),
				run(launcher, "pwd", new String[0], new String[0], IPath.fromPath(tempDir))); //$NON-NLS-1$
	}

	@Test
	public void testExecuteWithArgumentsAndEnvironment() throws Exception {
		IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(uri("/")); //$NON-NLS-1$
		assertEquals("one two", run(launcher, "echo", new String[] { "one two" }, new String[0], null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("bar", run(launcher, "printenv", new String[] { "LINUXTOOLS_TEST" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new String[] { "LINUXTOOLS_TEST=bar" }, null)); //$NON-NLS-1$
	}

	@Test
	public void testGetOS() throws Exception {
		Process uname = new ProcessBuilder("uname", "-s").start(); //$NON-NLS-1$ //$NON-NLS-2$
		String expected = new String(uname.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
		uname.waitFor();
		assertEquals(expected, RemoteProxyManager.getInstance().getOS(uri("/"))); //$NON-NLS-1$
	}

	@Test
	public void testGetEnv() throws CoreException {
		Map<String, String> env = new RemoteEnvProxyManager().getEnv(uri("/")); //$NON-NLS-1$
		// commands run by the embedded server inherit the environment of this process
		assertEquals(System.getenv("HOME"), env.get("HOME")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(env.keySet().stream().noneMatch(key -> key.startsWith("BASH_FUNC_")), //$NON-NLS-1$
				"Exported functions should be filtered out: " + env.keySet()); //$NON-NLS-1$
	}

	@Test
	public void testFileStore() throws Exception {
		Files.writeString(tempDir.resolve("existing.txt"), "content"); //$NON-NLS-1$ //$NON-NLS-2$
		IRemoteFileProxy fileProxy = RemoteProxyManager.getInstance().getFileProxy(uri(tempDir.toString()));
		assertEquals("/", fileProxy.getDirectorySeparator()); //$NON-NLS-1$

		IFileStore dir = fileProxy.getResource(tempDir.toString());
		IFileInfo dirInfo = dir.fetchInfo();
		assertTrue(dirInfo.exists());
		assertTrue(dirInfo.isDirectory());
		assertArrayEquals(new String[] { "existing.txt" }, dir.childNames(EFS.NONE, null)); //$NON-NLS-1$

		// read a file
		IFileStore existing = dir.getChild("existing.txt"); //$NON-NLS-1$
		assertEquals(7, existing.fetchInfo().getLength());
		try (InputStream in = existing.openInputStream(EFS.NONE, null)) {
			assertEquals("content", new String(in.readAllBytes(), StandardCharsets.UTF_8)); //$NON-NLS-1$
		}

		// create a directory and write a file into it
		IFileStore subDir = dir.getChild("sub").mkdir(EFS.NONE, null); //$NON-NLS-1$
		try (OutputStream out = subDir.getChild("new.txt").openOutputStream(EFS.NONE, null)) { //$NON-NLS-1$
			out.write("written".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
		}
		assertEquals("written", Files.readString(tempDir.resolve("sub/new.txt"))); //$NON-NLS-1$ //$NON-NLS-2$

		// delete them again
		subDir.getChild("new.txt").delete(EFS.NONE, null); //$NON-NLS-1$
		assertFalse(Files.exists(tempDir.resolve("sub/new.txt"))); //$NON-NLS-1$
		subDir.delete(EFS.NONE, null);
		assertFalse(Files.exists(tempDir.resolve("sub"))); //$NON-NLS-1$
	}
}
