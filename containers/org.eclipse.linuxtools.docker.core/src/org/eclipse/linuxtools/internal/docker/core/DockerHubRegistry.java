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

package org.eclipse.linuxtools.internal.docker.core;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerRegistry;
import org.eclipse.linuxtools.docker.core.IRepositoryTag;
import org.eclipse.linuxtools.docker.core.Messages;
import org.eclipse.osgi.util.NLS;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.spotify.docker.client.ObjectMapperProvider;

/**
 * The default implementation is the Docker Hub registry (running Docker
 * registry version 0.6.3)
 */
public class DockerHubRegistry implements IDockerRegistry {

	private static final String REGISTRY_LOCATION = "https://registry.hub.docker.com/"; //$NON-NLS-1$

	@Override
	public List<IRepositoryTag> getTags(final String repository)
			throws InterruptedException, ExecutionException, DockerException {
		// check that the registry supports the version 1 API
		// see
		// https://github.com/docker/docker-registry/blob/master/docker_registry/app.py
		final ClientConfig DEFAULT_CONFIG = new ClientConfig(
				ObjectMapperProvider.class, JacksonFeature.class);

		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);
		final WebTarget pingApiv1Resource = client.target(REGISTRY_LOCATION)
				.path("v1").path("_ping"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			final Response response = pingApiv1Resource
					.request(APPLICATION_JSON_TYPE).async().get().get();
			if (response.getStatus() != Status.OK.getStatusCode()) {
				throw new DockerException(
						NLS.bind(Messages.List_Docker_Containers_Failure,
								REGISTRY_LOCATION));
			}
		} catch (ExecutionException e) {
			Activator.log(e);
			return Collections.emptyList();
		}
		// now, query the registry
		final WebTarget queryTagsResource = client.target(REGISTRY_LOCATION)
				.path("v1").path("repositories").path(repository).path("tags"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final GenericType<List<RepositoryTag>> REPOSITORY_TAGS_RESULT_LIST = new GenericType<List<RepositoryTag>>() {
		};
		final List<RepositoryTag> result = queryTagsResource
				.request(APPLICATION_JSON_TYPE).async()
				.method(GET, REPOSITORY_TAGS_RESULT_LIST).get();

		return new ArrayList<IRepositoryTag>(result);
	}

}
