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
package org.eclipse.linuxtools.docker.core;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.linuxtools.internal.docker.core.DockerImageSearchResult;
import org.eclipse.linuxtools.internal.docker.core.ImageSearchResultV1;
import org.eclipse.linuxtools.internal.docker.core.ImageSearchResultV2;
import org.eclipse.linuxtools.internal.docker.core.RepositoryTag;
import org.eclipse.linuxtools.internal.docker.core.RepositoryTagV2;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.ImageSearchResult;

/**
 * @since 2.0
 */
public abstract class AbstractRegistry implements IRegistry {

	public static final String[] DOCKERHUB_REGISTRY_ALIASES = new String[] {
			"registry.hub.docker.com", //$NON-NLS-1$
			"index.docker.io" //$NON-NLS-1$
	};
	public static final String DOCKERHUB_REGISTRY = "index.docker.io"; //$NON-NLS-1$
	// Cache the URL for searches to avoid excessive calls
	private String cachedHTTPServerAddress;
	// Cache the result of isVersion2 to avoid excessive calls
	private Boolean isV2 = null;

	@Override
	public abstract String getServerAddress();

	private String getHTTPServerAddress() {
		if (cachedHTTPServerAddress != null) {
			return cachedHTTPServerAddress;
			/*
			 * This is wrong because serverAddress prefixed with http won't work
			 * for push/pull API but let's support this.
			 */
		} else if (getServerAddress().startsWith("http")) { //$NON-NLS-1$
			return getServerAddress();
		}
		// We haven't cached the result, so let's evaluate
		String[] versions = new String[] { "v1", "v2" }; //$NON-NLS-1$ //$NON-NLS-2$
		String[] schemes = new String[] { "http://", "https://" }; //$NON-NLS-1$ //$NON-NLS-2$
		final ClientConfig DEFAULT_CONFIG = new ClientConfig(
				ObjectMapperProvider.class, JacksonFeature.class);
		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);

		for (String scheme : schemes) {
			for (String ver : versions) {
				String url = scheme + getServerAddress();
				WebTarget queryServer = client.target(url).path(ver);
				try {
					Response resp = queryServer.request(APPLICATION_JSON_TYPE)
							.async().method(GET).get();
					int code = resp.getStatus();
					if (code >= 200 && code < 300) {
						cachedHTTPServerAddress = url;
						return url;
					}
				} catch (InterruptedException | ExecutionException e) {
				}
			}
		}
		// URL is probably wrong
		return "http://" + getServerAddress(); //$NON-NLS-1$
	}

	@Override
	public List<IDockerImageSearchResult> getImages(String term) throws DockerException {
		final ClientConfig DEFAULT_CONFIG = new ClientConfig(
				ObjectMapperProvider.class, JacksonFeature.class);
		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);
		List<IDockerImageSearchResult> result = new ArrayList<>();
		WebTarget queryImagesResource;

		if (isVersion2()) {
			final GenericType<ImageSearchResultV2> IMAGE_SEARCH_RESULT_LIST = new GenericType<ImageSearchResultV2>() {
			};
			ImageSearchResultV2 cisr = null;
			queryImagesResource = client.target(getHTTPServerAddress())
					.path("v2").path("_catalog"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				cisr = queryImagesResource.request(APPLICATION_JSON_TYPE)
						.async().method(GET, IMAGE_SEARCH_RESULT_LIST).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new DockerException(e);
			}
			List<ImageSearchResult> tmp = cisr.getRepositories().stream()
					.filter(e -> e.getName().contains(term))
					.collect(Collectors.toList());

			result.addAll(tmp.stream()
					.map(r -> new DockerImageSearchResult(r.getDescription(),
							r.isOfficial(), r.isAutomated(), r.getName(),
							r.getStarCount()))
					.collect(Collectors.toList()));
		} else {
			ImageSearchResultV1 pisr = null;
			final GenericType<ImageSearchResultV1> IMAGE_SEARCH_RESULT_LIST = new GenericType<ImageSearchResultV1>() {
			};
			int page = 0;
			try {
				while (pisr == null || pisr.getPage() < pisr.getTotalPages()) {
					page++;
					queryImagesResource = client.target(getHTTPServerAddress())
							.path("v1").path("search") //$NON-NLS-1$ //$NON-NLS-2$
							.queryParam("q", term) //$NON-NLS-1$
							.queryParam("page", page); //$NON-NLS-1$
					pisr = queryImagesResource.request(APPLICATION_JSON_TYPE)
							.async().method(GET, IMAGE_SEARCH_RESULT_LIST)
							.get();
					List<ImageSearchResult> tmp = pisr.getResult();
					result.addAll(tmp.stream()
							.map(r -> new DockerImageSearchResult(
									r.getDescription(), r.isOfficial(),
									r.isAutomated(), r.getName(),
									r.getStarCount()))
							.collect(Collectors.toList()));
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new DockerException(e);
			}
		}

		return result;
	}

	@Override
	public List<IRepositoryTag> getTags(String repository) throws DockerException {
		final ClientConfig DEFAULT_CONFIG = new ClientConfig(
				ObjectMapperProvider.class, JacksonFeature.class);
		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);
		final List<String> dockerHubAliases = Arrays
				.asList(DOCKERHUB_REGISTRY_ALIASES);
		WebTarget queryTagsResource;
		List<IRepositoryTag> result = new ArrayList<>();

		if (isVersion2()) {
			RepositoryTagV2 crts;
			queryTagsResource = client.target(getHTTPServerAddress()).path("v2") //$NON-NLS-1$
					.path(repository).path("tags").path("list"); //$NON-NLS-1$ //$NON-NLS-2$
			GenericType<RepositoryTagV2> REPOSITORY_TAGS_RESULT_LIST = new GenericType<RepositoryTagV2>() {
			};
			try {
				crts = queryTagsResource.request(APPLICATION_JSON_TYPE).async()
						.method(GET, REPOSITORY_TAGS_RESULT_LIST).get();
				result.addAll(crts.getTags());
			} catch (InterruptedException | ExecutionException e) {
				throw new DockerException(e);
			}

			// Docker Hub Registry format is actually different from an actual
			// registry
		} else if (dockerHubAliases.stream()
				.anyMatch(a -> getServerAddress().contains(a))) {
			queryTagsResource = client.target(getHTTPServerAddress()).path("v1") //$NON-NLS-1$
					.path("repositories").path(repository).path("tags"); //$NON-NLS-1$ //$NON-NLS-2$
			GenericType<List<RepositoryTag>> REPOSITORY_TAGS_RESULT_LIST = new GenericType<List<RepositoryTag>>() {
			};
			try {
				result.addAll(queryTagsResource.request(APPLICATION_JSON_TYPE)
						.async().method(GET, REPOSITORY_TAGS_RESULT_LIST)
						.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				// Do nothing
			}
		} else {
			queryTagsResource = client.target(getHTTPServerAddress()).path("v1") //$NON-NLS-1$
					.path("repositories").path(repository).path("tags"); //$NON-NLS-1$ //$NON-NLS-2$
			GenericType<Map<String, String>> REPOSITORY_TAGS_RESULT_LIST = new GenericType<Map<String, String>>() {
			};
			Map<String, String> ret = new HashMap<>();
			try {
				ret = queryTagsResource.request(APPLICATION_JSON_TYPE).async()
						.method(GET, REPOSITORY_TAGS_RESULT_LIST).get();
				for (Entry<String, String> e : ret.entrySet()) {
					RepositoryTag tag = new RepositoryTag();
					tag.setName(e.getKey());
					tag.setLayer(e.getValue());
					result.add(tag);
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new DockerException(e);
			}
		}
		return result;
	}

	@Override
	public boolean isVersion2() {
		if (isV2 != null) {
			return isV2;
		}
		// We haven't cached the result, so let's evaluate
		final ClientConfig DEFAULT_CONFIG = new ClientConfig(
				ObjectMapperProvider.class, JacksonFeature.class);
		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);
		final WebTarget pingApiv2Resource = client
				.target(getHTTPServerAddress()).path("v2"); //$NON-NLS-1$
		try {
			final Response response = pingApiv2Resource
					.request(APPLICATION_JSON_TYPE).async().get().get();
			if (response.getStatus() == Status.OK.getStatusCode()) {
				isV2 = true;
				return true;
			}
		} catch (ExecutionException | InterruptedException e) {
			// do nothing
		}
		isV2 = false;
		return false;
	}

}
