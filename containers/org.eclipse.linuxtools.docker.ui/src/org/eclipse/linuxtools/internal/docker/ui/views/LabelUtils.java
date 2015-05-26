/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;

/**
 * @author xcoulon
 *
 */
public class LabelUtils {
	
	public static final String CREATION_DATE_PATTERN = "yyyy-MM-dd";

	public static final String FINISHED_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sssz";
	
	/**
	 * @param elements
	 *            the {@link List} to reduce
	 * @return each {@link Entry} element of the given {@link Map} has only one
	 *         item, an empty {@link String} if the given list is empty,
	 *         otherwise the given list itself.
	 * 
	 */
	public static <K,V> Object reduce(final Map<K, List<V>> elements) {
		if(elements == null || elements.isEmpty()) {
			return "";
		} else {
			final Map<Object,Object> result = new HashMap<>();
			for(Entry<K, List<V>> entry : elements.entrySet()) {
				result.put(entry.getKey(), reduce(entry.getValue()));
			}
			return result ;
		}
	}
	
	/**
	 * Attempts to flatten the given {@link Set}.
	 * 
	 * @return the first element of the given {@link Set} if it has only one
	 *         item, an empty {@link String} if the given set is empty,
	 *         otherwise the given set itself as array.
	 * @param elements
	 *            the {@link Set} to analyze
	 */
	public static Object reduce(final Set<?> elements) {
		if (elements == null || elements.isEmpty()) {
			return "";
		} else if (elements.size() == 1) {
			return elements.toArray()[0];
		} else {
			return elements.toArray();
		}
	}

	/**
	 * Attempts to flatten the give {@link List}.
	 * 
	 * @return the first element of the given {@link List} has only one item, an
	 *         empty {@link String} if the given list is empty, otherwise the
	 *         given list itself.
	 * @param elements
	 *            the {@link List} to analyze
	 */
	public static Object reduce(final List<?> elements) {
		if(elements == null || elements.isEmpty()) {
			return "";
		} else if(elements.size() == 1) {
			return elements.get(0);
		} else {
			return elements;
		}
	}
	
	public static String toCreatedDate(final long created) {
		return toDate(created, CREATION_DATE_PATTERN);
	}
	
	public static String toCreatedDate(final Date created) {
		return toDate(created, CREATION_DATE_PATTERN);
	}
	
	public static String toFinishedDate(final Date finished) {
		return toDate(finished, FINISHED_DATE_PATTERN);
	}
	
	public static String toDate(final Long date, final String pattern) {
		final long millis = Long.valueOf(date).longValue() * 1000;
		final Date d = new Date(millis);
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern); //$NON-NLS-1$
		return sdf.format(d);
	}

	public static String toDate(final Date date, final String pattern) {
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern); //$NON-NLS-1$
		return sdf.format(date);
	}
	
	public static String containerPortMappingToString(
			final IDockerPortMapping portMapping) {
		final StringBuilder mappingBuffer = new StringBuilder();
		if(portMapping.getIp() != null && portMapping.getPublicPort() != 0) {
			mappingBuffer.append(portMapping.getIp()).append(':').append(portMapping.getPublicPort()).append("->");
		}
		mappingBuffer.append(portMapping.getPrivatePort()).append('/').append(portMapping.getType());
		return mappingBuffer.toString();
	}
	
	public static String toString(final Object value) {
		if(value == null) {
			return "null";
		}
		if(value instanceof IDockerPortBinding) {
			final IDockerPortBinding binding = (IDockerPortBinding) value;
			final String hostIp = (binding.hostIp() == null || binding.hostIp().isEmpty()) ? "<unspecified>" : binding.hostIp();
			final String hostPort = (binding.hostPort() == null || binding.hostPort().isEmpty()) ? "<unspecified>" : binding.hostPort();
			return new StringBuilder().append(hostIp).append(':').append(hostPort).toString();
		}
		return value.toString();
	}
	
}
