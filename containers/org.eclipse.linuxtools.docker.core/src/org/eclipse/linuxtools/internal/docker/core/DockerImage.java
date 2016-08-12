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
package org.eclipse.linuxtools.internal.docker.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.docker.core.IDockerImage;

public class DockerImage implements IDockerImage {

	/** The 'latest' tag. */
	public static final String LATEST_TAG = "latest"; //$NON-NLS-1$

	private static final String REGISTRY_HOST = "[a-zA-Z0-9]+([\\._-][a-zA-Z0-9]+)*"; //$NON-NLS-1$
	private static final String REGISTRY_PORT = "[0-9]+"; //$NON-NLS-1$
	private static final String REPOSITORY = "[a-z0-9]+([\\._-][a-z0-9]+)*"; //$NON-NLS-1$
	private static final String NAME = "[a-z0-9]+([\\._-][a-z0-9]+)*"; //$NON-NLS-1$
	private static final String TAG = "[a-zA-Z0-9]+([\\._-][a-zA-Z0-9]+)*"; //$NON-NLS-1$

	/** the image name pattern. */
	public static final Pattern imageNamePattern = Pattern.compile("(" //$NON-NLS-1$
			+ REGISTRY_HOST + "\\:" + REGISTRY_PORT + "/)?" //$NON-NLS-1$ //$NON-NLS-2$
			+ "((?<repository>" + REPOSITORY + "(/" + REPOSITORY + ")?)/)?" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ "(?<name>" + NAME + ")" //$NON-NLS-1$ //$NON-NLS-2$
			+ "(\\:(?<tag>" + TAG + "))?"); //$NON-NLS-1$ //$NON-NLS-2$

	// SimpleDateFormat is not thread-safe, so give one to each thread
	private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		}
	};

	private final DockerConnection parent;
	private final String created;
	private final String createdDate;
	private final String id;
	private final String shortId;
	private final String parentId;
	private final List<String> repoTags;
	private final String repo;
	private final List<String> tags;
	private final Long size;
	private final Long virtualSize;

	private final boolean intermediateImage;
	private final boolean danglingImage;

	public DockerImage(final DockerConnection parent,
			@Deprecated final List<String> repoTags, final String repo,
			final List<String> tags, final String id, final String parentId,
			final String created, final Long size, final Long virtualSize,
			final boolean intermediateImage, final boolean danglingImage) {
		this.parent = parent;
		this.repoTags = repoTags;
		this.repo = repo;
		this.tags = tags;
		this.id = id;
		this.shortId = getShortId(id);
		this.parentId = parentId;
		this.created = created;
		this.createdDate = created != null
				? formatter.get()
						.format(new Date(
								Long.valueOf(created).longValue() * 1000))
				: null;
		this.size = size;
		this.virtualSize = virtualSize;
		this.intermediateImage = intermediateImage;
		this.danglingImage = danglingImage;
	}

	/**
	 * @param id
	 *            the complete {@link IDockerImage} id
	 * @return the short id, excluding the {@code sha256:} prefix if present.
	 */
	private static String getShortId(final String id) {
		if (id.startsWith("sha256:")) {
			return getShortId(id.substring("sha256:".length()));
		}
		if (id.length() > 12) {
			return id.substring(0, 12);
		} else {
			return id;
		}
	}

	/**
	 * Extracts the org/repo and all the associated tags from the given
	 * {@code repoTags}, assuming that the given repoTags elements have the
	 * following format: {@code [org/]repo[:tag]}. Tags are sorted by their
	 * natural order.
	 * 
	 * @param repoTags
	 *            the list of repo/tags to analyze
	 * @return the tags indexed by org/repo
	 */
	public static Map<String, List<String>> extractTagsByRepo(
			final List<String> repoTags) {
		final Map<String, List<String>> results = new HashMap<>();
		for (String entry : repoTags) {
			final int indexOfColonChar = entry.lastIndexOf(':');
			final String repo = (indexOfColonChar > -1)
					? entry.substring(0, indexOfColonChar) : entry;
			if (!results.containsKey(repo)) {
				results.put(repo, new ArrayList<String>());
			}
			if (indexOfColonChar > -1) {
				results.get(repo).add(entry.substring(indexOfColonChar + 1));
			}
		}
		// now sort the tags
		for (Entry<String, List<String>> entry : results.entrySet()) {
			Collections.sort(entry.getValue());
		}
		return results;
	}

	/**
	 * Extracts the list of tags in the given repo/tags, assuming that the given
	 * repoTags elements have the following format: {@code [org/]repo[:tag]}.
	 * 
	 * @param repoTags
	 *            the repo/tags list to analyze
	 * @return the list of tags or empty list if none was found.
	 */
	public static List<String> extractTags(final List<String> repoTags) {
		if (repoTags.isEmpty()) {
			return Collections.emptyList();
		}
		final List<String> tags = new ArrayList<>();
		for (String repoTag : repoTags) {
			final int indexOfColonChar = repoTag.lastIndexOf(':');
			if (indexOfColonChar == -1) {
				continue;
			}
			final String tag = repoTag.substring(indexOfColonChar + 1);
			tags.add(tag);
		}
		return tags;
	}

	/**
	 * Extracts the tag in the given repo/tag, assuming that the given repoTag
	 * element has the following format: {@code [org/]repo[:tag]}.
	 * 
	 * @param repoTag
	 *            the repo/tag to analyze
	 * @return the tag or null if none was found.
	 */
	public static String extractRepo(final String repoTag) {
		if (repoTag == null) {
			return null;
		}
		final int indexOfColonChar = repoTag.lastIndexOf(':');
		if (indexOfColonChar == -1) {
			return repoTag;
		}
		return repoTag.substring(0, indexOfColonChar);
	}

	/**
	 * Extracts the tag in the given repo/tag, assuming that the given repoTag
	 * element has the following format: {@code [org/]repo[:tag]}
	 * 
	 * @param repoTag
	 *            the repo/tag to analyze
	 * @return the tag or <code>null</code> if none was found.
	 */
	public static String extractTag(final String repoTag) {
		if (repoTag == null) {
			return null;
		}
		final int indexOfColonChar = repoTag.lastIndexOf(':');
		if (indexOfColonChar == -1) {
			return null;
		}
		return repoTag.substring(indexOfColonChar + 1);
	}

	@Override
	public DockerConnection getConnection() {
		return parent;
	}

	@Override
	public List<String> repoTags() {
		return repoTags;
	}

	@Override
	public String repo() {
		return repo;
	}

	@Override
	public List<String> tags() {
		return tags;
	}

	@Override
	public String id() {
		return this.id;
	}

	/**
	 * @return the short image id, ie, the first 12 figures, excluding the
	 *         <code>sha256:</code> prefix.
	 */
	// TODO: add to the API in version 3.0.0
	public String shortId() {
		return this.shortId;
	}

	@Override
	public String parentId() {
		return parentId;
	}

	@Override
	public String created() {
		return created;
	}

	@Override
	public String createdDate() {
		return createdDate;
	}

	@Override
	public Long size() {
		return size;
	}

	@Override
	public Long virtualSize() {
		return virtualSize;
	}

	@Override
	public boolean isDangling() {
		return danglingImage;
	}

	@Override
	public boolean isIntermediateImage() {
		return intermediateImage;
	}

	@Override
	public String toString() {
		return "Image: id=" + id() + "\n  parentId=" + parentId()
				+ "\n  created=" + created() + "\n  repo=" + repo()
				+ "\n  tags=" + tags() + "\n  size=" + size()
				+ "\n  virtualSize=" + virtualSize();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((repo == null) ? 0 : repo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DockerImage other = (DockerImage) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (repo == null) {
			if (other.repo != null) {
				return false;
			}
		} else if (!repo.equals(other.repo)) {
			return false;
		}

		return true;
	}

	/**
	 * Appends the <code>latest</code> tag to the given {@code imageName}
	 * 
	 * @param imageName
	 *            the image name to check
	 * @return the image name with the <code>latest</code> tag if none was set
	 */
	public static String setDefaultTagIfMissing(final String imageName) {
		if (DockerImage.extractTag(imageName) == null) {
			return imageName + ':' + LATEST_TAG;
		}
		return imageName;
	}

}
