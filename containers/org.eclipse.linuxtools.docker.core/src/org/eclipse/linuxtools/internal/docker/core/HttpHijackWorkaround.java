/*******************************************************************************
 * Copyright (c) 2015, 2021 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.core.Activator;
import org.mandas.docker.client.LogReader;
import org.mandas.docker.client.LogStream;
import org.osgi.framework.Bundle;

/**
 * This is a workaround for lack of HTTP Hijacking support in Apache
 * HTTPClient. The assumptions made in Apache HTTPClient are that a
 * response is an InputStream and so we have no sane way to access the
 * underlying OutputStream (which exists at the socket level)
 *
 * References :
 * https://docs.docker.com/reference/api/docker_remote_api_v1.16/#32-hijacking
 * https://github.com/docker/docker/issues/5933
 */
public class HttpHijackWorkaround {

	public static WritableByteChannel getOutputStream(LogStream stream, String uri) throws Exception {
		final String[] fields = new String[] {
				"reader", //$NON-NLS-1$
				"stream", //$NON-NLS-1$
				"original", //$NON-NLS-1$
				"input", //$NON-NLS-1$
				"in", //$NON-NLS-1$
				"in", //$NON-NLS-1$
				"wrappedStream", //$NON-NLS-1$
				"in", //$NON-NLS-1$
				"inStream" //$NON-NLS-1$
		};
		final String[] declared = new String[] {
				"org.mandas.docker.client.DefaultLogStream",
				LogReader.class.getName(),
				"org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream", //$NON-NLS-1$
				"org.glassfish.jersey.message.internal.EntityInputStream", //$NON-NLS-1$
				FilterInputStream.class.getName(),
				FilterInputStream.class.getName(),
				"org.apache.http.conn.EofSensorInputStream", //$NON-NLS-1$
				"org.apache.http.impl.io.IdentityInputStream", //$NON-NLS-1$
				"org.apache.http.impl.io.SessionInputBufferImpl" }; //$NON-NLS-1$
		final String [] bundles = new String[] {
				"org.glassfish.jersey.core.jersey-common", //$NON-NLS-1$
				"org.apache.httpcomponents.httpcore", //$NON-NLS-1$
				"org.apache.httpcomponents.httpclient" //$NON-NLS-1$
		};

		List<String[]> list = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			list.add(new String[] { declared[i], fields[i] });
		}

		try {
			// See org.apache.http.impl.conn.LoggingManagedHttpClientConnection#getSocketInputStream()
			// We need to perform : LogFactory.getLogger("org.apache.http.wire").isDebugEnabled()
			Class<?> cLogFactory = null;
			Class<?> cLog = null;
			final String[] commonsLogging = new String[] { "org.apache.commons.logging" }; //$NON-NLS-1$
			cLogFactory = loadClass("org.apache.commons.logging.LogFactory", commonsLogging); //$NON-NLS-1$
			cLog = loadClass("org.apache.commons.logging.Log", commonsLogging); //$NON-NLS-1$
			if (cLogFactory != null && cLog != null) {
				Method mGetLog = cLogFactory.getMethod("getLog", String.class); //$NON-NLS-1$
				Object log = mGetLog.invoke(null, "org.apache.http.wire"); //$NON-NLS-1$
				Method mIsDebug = cLog.getMethod("isDebugEnabled"); //$NON-NLS-1$
				Boolean isDebug = (Boolean) mIsDebug.invoke(log);
				if (isDebug.booleanValue()) {
					list.add(new String[] { "org.apache.http.impl.conn.LoggingInputStream", "in" }); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} catch (Exception e) {
		}

		if (uri.startsWith("unix:")) { //$NON-NLS-1$
			list.add(new String[] { "sun.nio.ch.ChannelInputStream", "ch" }); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (uri.startsWith("https:")) { //$NON-NLS-1$
			float jvmVersion = Float.parseFloat(System.getProperty("java.specification.version")); //$NON-NLS-1$
			String fName;
			if (jvmVersion < 1.9f) {
				fName = "c"; //$NON-NLS-1$
			} else {
				fName = "socket"; //$NON-NLS-1$
			}
			list.add(new String[] { "sun.security.ssl.AppInputStream", fName }); //$NON-NLS-1$
		} else {
			float jvmVersion = Float.parseFloat(
					System.getProperty("java.specification.version")); //$NON-NLS-1$
			if (jvmVersion < 13f) {
				list.add(new String[] { "java.net.SocketInputStream", //$NON-NLS-1$
						"socket" }); //$NON-NLS-1$
			}
		}

		Object res = getInternalField(stream, list, bundles);
		if (res instanceof WritableByteChannel) {
			return (WritableByteChannel) res;
		} else if (res != null && "java.net.Socket.SocketInputStream" //$NON-NLS-1$
				.equals(res.getClass().getCanonicalName())) {
			// if we are here, we are Java 13 or higher and have to use an
			// alternate field reflection to get the socket
			List<String[]> list2 = new LinkedList<>();
			list2.add(new String[] { "java.net.Socket$SocketInputStream", //$NON-NLS-1$
					"parent" }); //$NON-NLS-1$
			res = getInternalField(res, list2, bundles);
		}

		if (res instanceof Socket) {
			return Channels.newChannel(((Socket) res).getOutputStream());
		} else {
			// TODO: throw an exception and let callers handle it.
			return null;
		}
	}

	/*
	 * We could add API for this in org.mandas.docker.client since there is
	 * access to the underlying InputStream but better wait and see what
	 * happens with the HTTP Hijacking situation.
	 */
	public static InputStream getInputStream(LogStream stream) {
		final String[] fields = new String[] { "reader", "stream" }; //$NON-NLS-1$ //$NON-NLS-2$
		final String[] declared = new String[] { "org.mandas.docker.client.DefaultLogStream", LogReader.class.getName()};

		List<String[]> list = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			list.add(new String[] { declared[i], fields[i] });
		}
		return (InputStream) getInternalField(stream, list, new String [0]);
	}

	/*
	 * Access arbitrarily nested internal fields.
	 */
	private static Object getInternalField (Object input, List<String []> set, String [] bundles) {
		Object curr = input;
		try {
			for (String [] e : set) {
				Field f = loadClass(e[0], bundles).getDeclaredField(e[1]);
				f.setAccessible(true);
				curr = f.get(curr);
			}
		} catch (Exception e) {
			Activator.log(e);
		}
		return curr;
	}

	/*
	 * Avoid explicitly depending on certain classes that are requirements
	 * of the docker-client library (org.mandas.docker.client).
	 */
	private static Class<?> loadClass(String key, String [] bundles) {
		try {
			return Class.forName(key);
		} catch (ClassNotFoundException e) {
			for (String bsName : bundles) {
				Bundle b = Platform.getBundle(bsName);
				try {
					return b.loadClass(key);
				} catch (ClassNotFoundException e1) {
				}
			}
		}
		return null;
	}

}
