/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Testing the {@link DockerImage} implementation and its utility methods
 */
public class DockerImageTest {

	@Test
	public void shouldExtractRepoFromRepoTag() {
		// given
		final String repoTag = "foo:latest";
		// when
		final String repo = DockerImage.extractRepo(repoTag);
		// then
		assertThat(repo).isEqualTo("foo");
	}

	@Test
	public void shouldExtractRepoFromOrgRepoTag() {
		// given
		final String repoTag = "org/foo:latest";
		// when
		final String repo = DockerImage.extractRepo(repoTag);
		// then
		assertThat(repo).isEqualTo("org/foo");
	}

	@Test
	public void shouldExtractRepoFromOrgRepo() {
		// given
		final String repoTag = "org/foo";
		// when
		final String repo = DockerImage.extractRepo(repoTag);
		// then
		assertThat(repo).isEqualTo("org/foo");
	}

	@Test
	public void shouldExtractRepoFromRepo() {
		// given
		final String repoTag = "foo";
		// when
		final String repo = DockerImage.extractRepo(repoTag);
		// then
		assertThat(repo).isEqualTo("foo");
	}

	@Test
	public void shouldExtractTagFromRepoTag() {
		// given
		final String repoTag = "foo:latest";
		// when
		final String repo = DockerImage.extractTag(repoTag);
		// then
		assertThat(repo).isEqualTo("latest");
	}

	@Test
	public void shouldExtractTagFromOrgRepoTag() {
		// given
		final String repoTag = "org/foo:latest";
		// when
		final String repo = DockerImage.extractTag(repoTag);
		// then
		assertThat(repo).isEqualTo("latest");
	}

	@Test
	public void shouldNotExtractTagFromOrgRepo() {
		// given
		final String repoTag = "org/foo";
		// when
		final String repo = DockerImage.extractTag(repoTag);
		// then
		assertThat(repo).isNull();
	}

	@Test
	public void shouldNotExtractTagFromRepo() {
		// given
		final String repoTag = "foo";
		// when
		final String repo = DockerImage.extractTag(repoTag);
		// then
		assertThat(repo).isNull();
	}

	@Test
	public void shouldExtractTagsByRepo() {
		// given
		final List<String> repoTags = Arrays.asList("foo", "foo:latest", "foo:1.0", "org/foo", "org/foo:1.0",
				"org/foo:latest");
		// when
		final Map<String, List<String>> tagsByRepo = DockerImage.extractTagsByRepo(repoTags);
		// then
		assertThat(tagsByRepo).containsEntry("foo", Arrays.asList("1.0", "latest"));
		assertThat(tagsByRepo).containsEntry("org/foo", Arrays.asList("1.0", "latest"));
	}
}
