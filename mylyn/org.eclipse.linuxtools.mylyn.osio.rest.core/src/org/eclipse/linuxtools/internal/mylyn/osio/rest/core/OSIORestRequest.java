/*******************************************************************************
 * Copyright (c) 2013, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for OSIO connector
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.ErrorResponse;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationException;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationRequest;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpOperation;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;
import org.eclipse.mylyn.commons.repositories.http.core.HttpUtil;
import org.eclipse.osgi.util.NLS;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public abstract class OSIORestRequest<T> extends CommonHttpOperation<T> {
	protected static final String ACCEPT = "Accept"; //$NON-NLS-1$

	protected static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$

	protected static final String APPLICATION_VND_JSON = "application/vnd.jsonapierrors+json"; //$NON-NLS-1$

	protected static final String AUTHORIZATION = "Authorization"; //$NON-NLS-1$

	protected static final String TEXT_XML_CHARSET_UTF_8 = "text/xml; charset=UTF-8"; //$NON-NLS-1$

	private final boolean authenticationRequired;
	
	private final boolean needsAuthURL;

	private final String urlSuffix;

	public OSIORestRequest(CommonHttpClient client, String urlSuffix, boolean authenticationRequired,
			boolean needsAuthURL) {
		super(client);
		this.authenticationRequired = authenticationRequired;
		this.needsAuthURL = needsAuthURL;
		this.urlSuffix = urlSuffix;
	}

	protected T execute(IOperationMonitor monitor) throws IOException, OSIORestException {
		HttpRequestBase request = createHttpRequestBase();
		addHttpRequestEntities(request);
		CommonHttpResponse response = execute(request, monitor);
		return processAndRelease(response, monitor);
	}

	protected abstract T parseFromJson(InputStreamReader in) throws OSIORestException;

	protected abstract HttpRequestBase createHttpRequestBase(String url);

	protected HttpRequestBase createHttpRequestBase() {
		HttpRequestBase request = createHttpRequestBase(createHttpRequestURL());
		return request;
	}

	protected String baseUrl() {
		String url = getClient().getLocation().getUrl();
		if (needsAuthURL) {
			url = url.replace("https://", "https://auth."); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (needsAuthentication()) {
			url = url.replace("https://", "https://api."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!url.endsWith("/api")) { //$NON-NLS-1$
			url += "/api"; //$NON-NLS-1$
		}
		return url;
	}

	protected String getUrlSuffix() {
		return urlSuffix;
	}

	protected String createHttpRequestURL() {
		String urlSuffix = getUrlSuffix();
		return baseUrl() + urlSuffix;
	}

	private String getToken() {
		String auth_token = getClient().getLocation().getProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN);
		if (auth_token != null) {
			return auth_token;
		}
		// TODO: remove below when authorization UI is working
		return "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwb"
				+ "EwwdlhzOVlSVnFaTW93eXc4dU5MUl95cjBpRmFvemR"
				+ "RazlyenEyT1ZVIn0.eyJqdGkiOiI5YTlmMTk3Yi1iY"
				+ "2I4LTRmY2QtYjM2OC04ZDg5MDRjYmRiYWIiLCJleHA"
				+ "iOjE1MDgzODgwNjgsIm5iZiI6MCwiaWF0IjoxNTA1N"
				+ "zk2MDY4LCJpc3MiOiJodHRwczovL3Nzby5vcGVuc2h"
				+ "pZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1Z"
				+ "CI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3V"
				+ "iIjoiZTkwMjRmODQtODQ1My00YTgwLWFjZWMtOThhM"
				+ "jc2ODZlYzI0IiwidHlwIjoiQmVhcmVyIiwiYXpwIjo"
				+ "iZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX"
				+ "3RpbWUiOjE1MDU0OTA4MjUsInNlc3Npb25fc3RhdGU"
				+ "iOiI5MTFlZWE0Ny01NjcyLTRkNGItYWZiMi1mOTFjM"
				+ "TQ2NGE1MTciLCJhY3IiOiIwIiwiYWxsb3dlZC1vcml"
				+ "naW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvb"
				+ "GVzIjpbInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXN"
				+ "vdXJjZV9hY2Nlc3MiOnsiYnJva2VyIjp7InJvbGVzI"
				+ "jpbInJlYWQtdG9rZW4iXX0sImFjY291bnQiOnsicm9"
				+ "sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtY"
				+ "WNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0"
				+ "sImFwcHJvdmVkIjp0cnVlLCJuYW1lIjoiSmVmZiBKb"
				+ "2huc3RvbiIsImNvbXBhbnkiOiJSZWQgSGF0IiwicHJ"
				+ "lZmVycmVkX3VzZXJuYW1lIjoiampvaG5zdG4iLCJna"
				+ "XZlbl9uYW1lIjoiSmVmZiIsImZhbWlseV9uYW1lIjo"
				+ "iSm9obnN0b24iLCJlbWFpbCI6Impqb2huc3RuQHJlZ"
				+ "GhhdC5jb20ifQ.VMe9GfHxG51BkH5YPXyfcLsZgIi9"
				+ "-ui0gXzco3t7AKLhIsHKUYiInurwxYJVT2ToHffMwN"
				+ "rrfUbm0eGAkLbR_A_04vvYzi7keBMep0XjuZW6lM3v"
				+ "xb-93NxITQcHCNMFvLxvm1wrN5ui29X5x4NIcIcU0K"
				+ "ye2qsDKn_d-UxQXDgxavqrc_a5d0RYb-WImPej2ZDe"
				+ "po8IU16Ev-wPWLLN91KnqLBSyVCB2MxFkkdNOE284n"
				+ "I5p2yzCX_QbVMKdbuY0S8Hyu8Bs-A1LMBAB2xuecSu"
				+ "u4Glykw9KNNOV8KqNSAoDwm_KIw7SG2kcv_d8Z4oap"
				+ "HlrNg4u9_2rsPzEKotnw";
	}
	
	private String getBearer() {
		return "Bearer " + getToken(); //$NON-NLS-1$
	}
	
	protected void addHttpRequestEntities(HttpRequestBase request) throws OSIORestException {
		request.setHeader(ACCEPT, APPLICATION_VND_JSON);
		if (authenticationRequired) {
			request.addHeader(AUTHORIZATION, getBearer());
		}
	}

	public T run(IOperationMonitor monitor) throws OSIORestException {
		try {
			return execute(monitor);
		} catch (IOException e) {
			throw new OSIORestException(e);
		}
	}

	protected T doProcess(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		try (BufferedInputStream is = new BufferedInputStream(response.getResponseEntityAsStream())) {
			InputStreamReader in = new InputStreamReader(is);
			throwExeptionIfRestError(is, in);
			return parseFromJson(in);
		}
	}

	protected void doValidate(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		validate(response, HttpStatus.SC_OK, monitor);
	}

	protected void validate(CommonHttpResponse response, int expected, IOperationMonitor monitor)
			throws OSIORestException {
		int statusCode = response.getStatusCode();
		if (statusCode != expected && statusCode != HttpStatus.SC_BAD_REQUEST) {
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				throw new OSIORestResourceNotFoundException(
						NLS.bind("Requested resource ''{0}'' does not exist", response.getRequestPath()));
			} else if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
				throw new OSIORestResourceMovedPermanentlyException(
						response.getResponse().getAllHeaders()[0],
						NLS.bind("Requested resource ''{0}'' has been moved permanently", response.getRequestPath()));
			}
			throw new OSIORestException(NLS.bind("Unexpected response from OSIO REST server for ''{0}'': {1}",
					response.getRequestPath(), HttpUtil.getStatusText(statusCode)));
		}
	}

	protected T processAndRelease(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		try {
			doValidate(response, monitor);
			return doProcess(response, monitor);
		} finally {
			response.release();
		}
	}

	@Override
	protected void validate(HttpResponse response, IOperationMonitor monitor) throws AuthenticationException {
		super.validate(response, monitor);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_FORBIDDEN) {
			AuthenticationRequest<AuthenticationType<UserCredentials>> request = new AuthenticationRequest<AuthenticationType<UserCredentials>>(
					getClient().getLocation(), AuthenticationType.REPOSITORY);
			throw new AuthenticationException(HttpUtil.getStatusText(statusCode), request, true);
		}
	}

	@Override
	protected boolean needsAuthentication() {
		return authenticationRequired;
	}

	protected UserCredentials getCredentials() {
		UserCredentials credentials = getClient().getLocation().getCredentials(AuthenticationType.REPOSITORY);
		checkState(credentials != null, "Authentication requested without valid credentials");
		return credentials;
	}

	protected ErrorResponse parseErrorResponseFromJson(InputStreamReader in) throws OSIORestException {

		TypeToken<ErrorResponse> a = new TypeToken<ErrorResponse>() {
		};
		return new Gson().fromJson(in, a.getType());
	}

	protected void throwExeptionIfRestError(InputStream is, InputStreamReader in)
			throws IOException, OSIORestException {
		try {
			is.mark(18);
			byte[] b = new byte[17];
			is.read(b);
			String str = new String(b);
			if (str.startsWith("{\"code\":") || str.startsWith("{\"message\":") || str.startsWith("{\"error\":") //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					|| str.startsWith("{\"documentation\":")) { //$NON-NLS-1$
				is.reset();
				ErrorResponse resp = parseErrorResponseFromJson(in);
				throw new OSIORestResourceNotFoundException(
						NLS.bind("Error {1}: {0}", new Object[] { resp.getMessage(), resp.getCode() })); //$NON-NLS-1$
			}
		} finally {
			is.reset();
		}
	}
}
