/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.messages.Image;

/**
 * 
 */
public class MockDockerImageFactory {

	public static Builder id(final String id) {
		return new Builder().id(id);
	}

	public static Builder name(final String repoTag, final String... otherRepoTags) {
		return new Builder().randomId().name(repoTag, otherRepoTags);
	}

	public static class Builder {

		private static char[] hexa = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		private final Image image;

		private Builder() {
			this.image = Mockito.mock(Image.class);
		}

		private Builder id(final String id) {
			Mockito.when(this.image.id()).thenReturn(id);
			return this;
		}
		
		private Builder randomId() {
			// generate a random id for the image
			final String id = IntStream.range(0, 12)
					.mapToObj(i -> Character.valueOf(hexa[new Random().nextInt(16)]).toString())
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

		public Image build() {
			return image;
		}
	}

}
