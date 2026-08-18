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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.Messages;
import org.mandas.docker.client.messages.Container;

/**
 * A factory for mock {@link Container}s.
 */
public class MockContainerFactory {

	public static Container of(final String id) {
		return new Container(id, List.of(), "", "", "", 0L, "", "", List.of(), Map.of(), 0L, 0L, null, List.of());
	}

	public static Container of(final String id, String imageName, String status, String... names) {
		return new Container(id, List.of(names), imageName, "", "", 0L, "", status, List.of(), Map.of(), 0L, 0L, null,
				List.of());
	}

	public static Builder name(final String repoTag, final String... otherRepoTags) {
		return new Builder().randomId().name(repoTag, otherRepoTags);
	}

	public static class Builder {

		private static char[] hexa = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		// Container is a record, so it is built rather than mocked: the inline
		// mock maker has to retransform a record to mock it and the JVM rejects
		// the result with a ClassFormatError. Building the real value is also
		// simply the right thing to do for an immutable data carrier.
		private String id;
		private List<String> names = List.of();
		private String image = "";
		private String status = "";
		private Long created = 0L;

		private Builder() {
		}

		private Builder randomId() {
			// generate a random id for the container
			this.id = IntStream.range(0, 12)
					.mapToObj(i -> Character.valueOf(hexa[new Random().nextInt(16)]).toString())
					.collect(Collectors.joining());
			return this;
		}

		public Builder name(final String name, final String... otherNames) {
			final List<String> repoTags = new ArrayList<>();
			repoTags.add(name);
			Stream.of(otherNames).forEach(r -> repoTags.add(r));
			this.status = Messages.Running_specifier;
			this.names = List.copyOf(repoTags);
			this.created = new Date().getTime();
			return this;
		}

		public Builder imageName(final String imageId) {
			this.image = imageId;
			return this;
		}

		public Builder status(final String status) {
			this.status = status;
			return this;
		}

		public Builder statusProvider(final MockStatusProvider statusProvider) {
			this.status = statusProvider.getStatus();
			return this;
		}

		public Container build() {
			return new Container(id, names, image, "", "", created, "", status, List.of(), Map.of(), 0L, 0L, null,
					List.of());
		}

	}

}
