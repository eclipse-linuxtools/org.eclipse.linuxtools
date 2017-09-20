package org.eclipse.linuxtools.internal.docker.ui.testutils;

import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerConfig;

public class MockContainerConfigFactory {

	public static Builder labels(ImmutableMap<String, String> labels) {
		return new Builder().labels(labels);
	}

	public static class Builder {

		private final ContainerConfig containerConfig;
		private ImmutableMap<String, String> labels;

		private Builder() {
			this.containerConfig = Mockito.mock(ContainerConfig.class, Mockito.RETURNS_DEEP_STUBS);
			labels = ImmutableMap.of();
		}

		public Builder labels(ImmutableMap<String, String> labels) {
			this.labels = labels;
			Mockito.when(this.containerConfig.labels()).thenReturn(this.labels);
			return this;
		}

		public ContainerConfig build() {
			Mockito.when(this.containerConfig.labels()).thenReturn(this.labels);
			return this.containerConfig;
		}
	}

}
