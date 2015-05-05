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
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerImage;

public class DockerImage implements IDockerImage {

	// SimpleDateFormat is not thread-safe, so give one to each thread
    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    
	private final DockerConnection parent;
	private final String created;
	private final String createdDate;
	private final String id;
	private final String parentId;
	private final List<String> repoTags;
	private final String repo;
	private final List<String> tags;
	private final Long size;
	private final Long virtualSize;

	private final boolean intermediateImage;
	private final boolean danglingImage;

	public DockerImage(final DockerConnection parent, final List<String> repoTags,
			final String id, final String parentId, final String created, final Long size,
			final Long virtualSize, final boolean intermediateImage, final boolean danglingImage) {
		this.parent = parent;
		this.repoTags = repoTags;
		this.repo = extractRepo(repoTags);
		this.tags = extractTags(repoTags);
		this.id = id;
		this.parentId = parentId;
		this.created = created;
		this.createdDate = formatter.get().format(new Date(Long.valueOf(created).longValue() * 1000));
		this.size = size;
		this.virtualSize = virtualSize;
		this.intermediateImage = intermediateImage;
		this.danglingImage = danglingImage;
	}

	/**
	 * Extracts the org/repo from the list of repo tags, assuming that the given repoTags elements have the following format:
	 * <pre>org/repo:tag</pre> or <pre>repo:tag</pre>.
	 * @param repoTags the list of repo/tags to analyze
	 * @return the repo or org/repo name or {@code null} if none was found.
	 */
	public static String extractRepo(final List<String> repoTags) {
		if(repoTags.isEmpty()) {
			return null;
		} 
		final String firstRepoTag = repoTags.get(0);
		final int indexOfColonChar = firstRepoTag.indexOf(':');
		if(indexOfColonChar == -1) {
			return firstRepoTag;
		}
		return firstRepoTag.substring(0, indexOfColonChar);
	}

	/**
	 * Extracts the list of tags in the give repo/tags, assuming that the given repoTags elements have the following format:
	 * <pre>org/repo:tag</pre> or <pre>repo:tag</pre>.
	 * @param repoTags the repo/tags list to analyze
	 * @return the list of tags or empty list if none was found.
	 */
	public static List<String> extractTags(final List<String> repoTags) {
		if(repoTags.isEmpty()) {
			return Collections.emptyList();
		} 
		final List<String> tags = new ArrayList<>();
		for(String repoTag : repoTags) {
			final int indexOfColonChar = repoTag.indexOf(':');
			if(indexOfColonChar == -1) {
				continue;
			}
			tags.add(repoTag.substring(indexOfColonChar+1));
		}
		return tags;
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
		return id;
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
		return "Image: id=" + id() + "\n" + "  parentId=" + parentId() + "\n"
				+ "  created=" + created() + "\n" + "  repoTags="
				+ repoTags().toString() + "\n" + "  size=" + size() + "\n"
				+ "  virtualSize=" + virtualSize() + "\n";
	}

}
