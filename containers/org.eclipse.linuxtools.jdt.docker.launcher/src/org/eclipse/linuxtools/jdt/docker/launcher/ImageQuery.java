/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.osgi.framework.Version;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ExecCreation;

public class ImageQuery {

	private String id;
	private DockerClient client;

	public ImageQuery(DockerConnection conn, String image) {
		IDockerHostConfig hc = DockerHostConfig.builder().build();
		IDockerContainerConfig cc = new DockerContainerConfig.Builder()
				.image(image)
				.cmd("/bin/sh")
				.attachStdout(true)
				.attachStderr(true)
				.attachStdin(true)
				.openStdin(true)
				.tty(true)
				.build();
		try {
			this.id = conn.createContainer(cc, hc);
			conn.startContainer(id, null);
			this.client = conn.getClient();
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String exec(String [] cmd) {
		LogStream stream = null;
		try {
			ExecCreation exeCr = client.execCreate(id, cmd,
					ExecCreateParam.attachStdout(), ExecCreateParam.attachStderr(), ExecCreateParam.detach(),
					ExecCreateParam.attachStdin()); // needed to avoid connection reset on unix socket
			stream = client.execStart(exeCr.id());
			StringBuffer res = new StringBuffer();
			while (stream.hasNext()) {
				ByteBuffer b = stream.next().content();
				byte[] buffer = new byte[b.remaining()];
				b.get(buffer);
				res.append(new String(buffer));
			}
			return res.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public String getDefaultJVMName () {
		String result = null;
		File f =  getDefaultJVMInstallLocation();
		if (f != null) {
			result = f.getName();
		}
		return result;
	}

	public File getDefaultJVMInstallLocation () {
		final String JRE = "jre"; //$NON-NLS-1$
		final String[] fgCandidateJavaFiles = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		final String[] fgCandidateJavaLocations = {JRE + File.separatorChar + "bin" + File.separatorChar, "bin" + File.separatorChar}; //$NON-NLS-1$ //$NON-NLS-2$

		String result = exec(new String [] {"sh", "-c", "readlink -f `which java`"});
		if (result != null) {
			result = result.replaceAll("\n", "");

			for (int i = 0; i < fgCandidateJavaFiles.length; i++) {
				for (int j = 0; j < fgCandidateJavaLocations.length; j++) {
					if (result.endsWith(fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i])) {
						return new File(result.replace(fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i], ""));
					}
				}
			}

			result = exec(new String [] {"readlink", "-f", "/usr/lib/jvm/java"});
			if (result != null) {
				result = result.replaceAll("\n", "");
			}
			return new File(result);
		} else {
			return null;
		}
	}

	public double getJavaVersion () {
		String result = exec(new String [] {"sh", "-c", "java -version 2>&1 | grep version | cut -d\\\" -f2 | cut -d_ -f1"});
		if (result != null) {
			result = result.replaceAll("\n", "");
			Version v = new Version(result);
			String newV = v.getMajor() + "." + v.getMinor();
			return Double.valueOf(newV);
		} else {
			return 0;
		}
	}

	public boolean fileExists (File file) {
		String result = exec(new String [] {"sh", "-c", "[ -e " + file.getAbsolutePath() + " ]; echo $?"});
		try {
			result = result.replaceAll("\n", "");
			int res = Integer.parseInt(result);
			return res == 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public boolean isFile (File file) {
		String result = exec(new String [] {"sh", "-c", "[ -f " + file.getAbsolutePath() + " ]; echo $?"});
		try {
			result = result.replaceAll("\n", "");
			int res = Integer.parseInt(result);
			return res == 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void destroy() {
		IDockerConnection conn = DockerConnectionManager.getInstance().getFirstConnection();
		try {
			conn.stopContainer(id);
			conn.removeContainer(id);
		} catch (DockerException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
