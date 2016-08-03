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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;

/**
 * A factory for mock {@link DockerImage}s.
 */
public class MockDockerImageFactory {

	public static Builder id(final String id) {
		return new Builder().id(id);
	}

	public static Builder name(final String repoTag, final String... otherRepoTags) {
		return new Builder().randomId().name(repoTag, otherRepoTags);
	}

	public static class Builder {

		private final IDockerImage image;

		private Builder() {
			this.image = Mockito.mock(IDockerImage.class);
		}

		private Builder id(final String id) {
			Mockito.when(this.image.id()).thenReturn(id);
			return this;
		}
		
		private Builder randomId() {
			// generate a random id for the image
			final String id = IntStream.range(0, 12)
					.mapToObj(i -> Character.valueOf(MockImageFactory.HEXA[new Random().nextInt(16)]).toString())
					.collect(Collectors.joining());
			Mockito.when(this.image.id()).thenReturn(id);
			return this;
		}
		
		public Builder name(final String repoTag, final String... otherRepoTags) {
			final List<String> repoTags = new ArrayList<>();
			repoTags.add(repoTag);
			Stream.of(otherRepoTags).forEach(r -> repoTags.add(r));
			Mockito.when(this.image.repoTags()).thenReturn(ImmutableList.copyOf(repoTags));
			Mockito.when(this.image.created()).thenReturn(Long.toString(new Date().getTime()));
			return this;
		}

		public Builder connection(final IDockerConnection connection) {
			Mockito.when(this.image.getConnection()).thenReturn(connection);
			return this;
		}
		
		public IDockerImage build() {
			return image;
		}

	}

}
