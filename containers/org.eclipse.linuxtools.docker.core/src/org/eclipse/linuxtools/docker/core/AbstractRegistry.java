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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.eclipse.linuxtools.internal.docker.core.OAuth2Utils;
import org.eclipse.linuxtools.internal.docker.core.OAuth2Utils.BearerTokenResponse;
import org.eclipse.linuxtools.internal.docker.core.RepositoryTag;
import org.eclipse.linuxtools.internal.docker.core.RepositoryTagV2;
import org.eclipse.osgi.util.NLS;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.ImageSearchResult;

/**
 * @since 2.0
 */
public abstract class AbstractRegistry implements IRegistry {

	/** Aliases of the Docker Hub hostname. */
	public static final String[] DOCKERHUB_REGISTRY_ALIASES = new String[] {
			"registry.hub.docker.com", //$NON-NLS-1$
			"index.docker.io" //$NON-NLS-1$
	};

	private static final ClientConfig DEFAULT_CONFIG = new ClientConfig(
			ObjectMapperProvider.class, JacksonFeature.class);

	public static final String DOCKERHUB_REGISTRY = "https://index.docker.io"; //$NON-NLS-1$
	// Cache the URL for searches to avoid excessive calls
	private String cachedHTTPServerAddress;
	// Cache the result of isVersion2 to avoid excessive calls
	private Boolean isV2 = null;

	@Override
	public abstract String getServerAddress();

	/**
	 * 
	 * @return the server host (and optional port) to prepend to an image name
	 *         when pushing or pulling
	 */
	// TODO: add this method in the IRegistry interface
	public abstract String getServerHost();

	/**
	 * @return <code>true</code> if this {@link IRegistry} is for Docker Hub,
	 *         <code>false</code> otherwise.
	 */
	// TODO: add this method in the IRegistry interface
	public abstract boolean isDockerHubRegistry();

	/**
	 * @return <code>true</code> if this {@link IRegistry} includes credentials,
	 *         <code>false</code> otherwise.
	 */
	// TODO: add this method in the IRegistry interface
	public abstract boolean isAuthProvided();

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
					enableDockerAuthenticator();
					Response resp = queryServer.request(APPLICATION_JSON_TYPE)
							.async().method(GET).get();
					int code = resp.getStatus();
					if (code >= 200 && code < 300) {
						cachedHTTPServerAddress = url;
						return url;
					}
				} catch (InterruptedException | ExecutionException e) {
				} finally {
					restoreAuthenticator();
				}
			}
		}
		// URL is probably wrong
		return "http://" + getServerAddress(); //$NON-NLS-1$
	}

	@Override
	public List<IDockerImageSearchResult> getImages(String term)
			throws DockerException {

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
	public List<IRepositoryTag> getTags(String repository)
			throws DockerException {

		final Client client = ClientBuilder.newClient(DEFAULT_CONFIG);
		try {
			if (isVersion2()) {
				return retrieveTagsFromRegistryV2(client, repository);
			} else if (isDockerHubRegistry()) {
				return retrieveTagsFromDockerHub(client, repository);
			} else {
				return retrieveTagsFromRegistryV1(client, repository);
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new DockerException(e);
		}

	}

	/**
	 * Retrieves the list of tags for a given repository, assuming that the
	 * target registry is Docker Hub.
	 * 
	 * @param client
	 *            the client to use
	 * @param repository
	 *            the repository to look-up
	 * @return the list of tags for the given repository
	 * @throws CancellationException
	 *             if the computation was cancelled
	 */
	private List<IRepositoryTag> retrieveTagsFromDockerHub(final Client client,
			final String repository)
			throws DockerException {
		try {
			// if the given repository is an official repository, we need to
			// prepend
			// with 'library'
			final String repoName = repository.contains("/") ? repository //$NON-NLS-1$
					: "library/" + repository; //$NON-NLS-1$
			// Docker Hub Registry may require a Bearer token which needs to be
			// retrieved if the initial request returned a 401/Forbidden
			// response.
			// In that case, the "Www-Authenticate" response header provides the
			// information needed to obtain a token.
			// attempt to query the registry without a bearer token
			final WebTarget queryTagsResource = client
					.target(getHTTPServerAddress()).path("v2") //$NON-NLS-1$
					.path(repoName).path("tags").path("list"); //$NON-NLS-1$ //$NON-NLS-2$
			// return queryTagsResource.request(APPLICATION_JSON_TYPE).async()
			// .method(GET, REPOSITORY_TAGS_RESULT_LIST).get();
			final Response response = queryTagsResource
					.request(APPLICATION_JSON_TYPE).async().get()
					.get(10, TimeUnit.SECONDS);
			if (response.getStatus() == 200) {
				return response.readEntity(RepositoryTagV2.class).getTags();
			} else if (response.getStatus() != 401) { // anything but
														// "Unauthorized"
				throw new DockerException(
						NLS.bind(Messages.ImageTagsList_failure, repository,
								response.readEntity(String.class)));
			}
			// for "Unauthorized response, let's get a Bearer token and try
			// again
			final String wwwAuthenticateResponseHeader = response
					.getHeaderString("Www-Authenticate"); //$NON-NLS-1$
			// parse the header which should have the following form:
			// Bearer
			// realm="https://auth.docker.io/token",service="registry.docker.io",scope="repository:jboss/wildfly:pull
			final Map<String, String> authenticateInfo = OAuth2Utils
					.parseWwwAuthenticateHeader(wwwAuthenticateResponseHeader);
			if (authenticateInfo == null
					|| !authenticateInfo.containsKey("realm") //$NON-NLS-1$
					|| !authenticateInfo.containsKey("service") //$NON-NLS-1$
					|| !authenticateInfo.containsKey("scope")) { //$NON-NLS-1$
				throw new DockerException(NLS.bind(
						Messages.ImageTagsList_failure_invalidWwwAuthenticateFormat,
						repository));
			}
			// now, call the auth service to obtain a Bearer token:
			final String realm = authenticateInfo.get("realm"); //$NON-NLS-1$
			final String service = authenticateInfo.get("service"); //$NON-NLS-1$
			final String scope = authenticateInfo.get("scope"); //$NON-NLS-1$
			final WebTarget bearerTokenRetrievalTarget = client.target(realm)
					.queryParam("service", service) //$NON-NLS-1$
					.queryParam("scope", scope); //$NON-NLS-1$
			final BearerTokenResponse bearerTokenRetrievalResponse = bearerTokenRetrievalTarget
					.request(APPLICATION_JSON_TYPE).async()
					.get(BearerTokenResponse.class).get(10, TimeUnit.SECONDS);
			// finally, perform the same request, using the Bearer token:
			final WebTarget queryTagsResourceWithBearerTokenTarget = client
					.target(getHTTPServerAddress()).path("v2") //$NON-NLS-1$
					.path(repoName).path("tags").path("list"); //$NON-NLS-1$ //$NON-NLS-2$
			return queryTagsResourceWithBearerTokenTarget
					.request(APPLICATION_JSON_TYPE)
					.header("Authorization", //$NON-NLS-1$
							"Bearer " + bearerTokenRetrievalResponse.getToken()) //$NON-NLS-1$
					.async().get(RepositoryTagV2.class)
					.get(10, TimeUnit.SECONDS)
					.getTags();
		} catch (TimeoutException | ExecutionException
				| InterruptedException e) {
			throw new DockerException(NLS.bind(Messages.ImageTagsList_failure,
					repository, e.getMessage()), e);
		}

	}

	/**
	 * Retrieves the list of tags for a given repository, assuming that the
	 * target registry is a registry v2 instance.
	 * 
	 * @param client
	 *            the client to use
	 * @param repository
	 *            the repository to look-up
	 * @return the list of tags for the given repository
	 * @throws CancellationException
	 *             - if the computation was cancelled
	 * @throws ExecutionException
	 *             - if the computation threw an exception
	 * @throws InterruptedException
	 *             - if the current thread was interrupted while waiting
	 */
	private List<IRepositoryTag> retrieveTagsFromRegistryV1(final Client client,
			final String repository)
			throws InterruptedException, ExecutionException {
		final GenericType<Map<String, String>> REPOSITORY_TAGS_RESULT_LIST = new GenericType<Map<String, String>>() {
		};
		final WebTarget queryTagsResource = client
				.target(getHTTPServerAddress()).path("v1") //$NON-NLS-1$
				.path("repositories").path(repository).path("tags"); //$NON-NLS-1$ //$NON-NLS-2$
		return queryTagsResource.request(APPLICATION_JSON_TYPE).async()
				.method(GET, REPOSITORY_TAGS_RESULT_LIST).get().entrySet()
				.stream().map(e -> new RepositoryTag(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the list of tags for a given repository, assuming that the
	 * target registry is a registry v1 instance.
	 * 
	 * @param client
	 *            the client to use
	 * @param repository
	 *            the repository to look-up
	 * @return the list of tags for the given repository
	 * @throws CancellationException
	 *             - if the computation was cancelled
	 * @throws ExecutionException
	 *             - if the computation threw an exception
	 * @throws InterruptedException
	 *             - if the current thread was interrupted while waiting
	 */
	private List<IRepositoryTag> retrieveTagsFromRegistryV2(final Client client,
			final String repository)
			throws InterruptedException, ExecutionException {
		final GenericType<RepositoryTagV2> REPOSITORY_TAGS_RESULT_LIST = new GenericType<RepositoryTagV2>() {
		};
		final WebTarget queryTagsResource = client
				.target(getHTTPServerAddress()).path("v2") //$NON-NLS-1$
				.path(repository).path("tags").path("list"); //$NON-NLS-1$ //$NON-NLS-2$
		final RepositoryTagV2 crts = queryTagsResource
				.request(APPLICATION_JSON_TYPE).async()
				.method(GET, REPOSITORY_TAGS_RESULT_LIST).get();
		return crts.getTags();
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

	/**
	 * Enable an Authenticator used to pass this registry's authentication
	 * credentials to HTTP Authentication requests.
	 */
	protected abstract void enableDockerAuthenticator();

	/**
	 * Restore the default Authenticator (likely
	 * org.eclipse.ui.internal.net.auth.NetAuthenticator)
	 */
	protected abstract void restoreAuthenticator();

}
