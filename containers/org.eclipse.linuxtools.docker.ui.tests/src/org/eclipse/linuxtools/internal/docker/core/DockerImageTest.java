/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
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

	@Test
	public void shouldExtractEmptyTagsByRepo() {
		// given
		final List<String> repoTags = Arrays.asList("foo");
		// when
		final Map<String, List<String>> tagsByRepo = DockerImage.extractTagsByRepo(repoTags);
		// then
		assertThat(tagsByRepo).hasSize(1).contains(MapEntry.entry("foo", Collections.emptyList()));
	}

	@Test
	public void shouldDuplicateImageByRepo() {
		// given
		final IDockerImage fooImage = MockDockerImageFactory.id("sha256:foo_image")
				.name("foo_image", "foo_image_alias:alias").build();
		// when
		final List<IDockerImage> result = DockerImage.duplicateImageByRepo(fooImage).toList();
		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).id()).isEqualTo("sha256:foo_image");
		assertThat(result.get(0).repo()).isEqualTo("foo_image");
		assertThat(result.get(0).tags()).isEmpty();
		assertThat(result.get(1).id()).isEqualTo("sha256:foo_image");
		assertThat(result.get(1).repo()).isEqualTo("foo_image_alias");
		assertThat(result.get(1).tags()).containsExactly("alias");
	}

}
