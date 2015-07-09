/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Class copied from ImagePullFailedException and modified

package org.eclipse.linuxtools.docker.core;

public class DockerImageBuildFailedException extends DockerException {

	private static final long serialVersionUID = 1L;

	private final String image;

	public DockerImageBuildFailedException(final String image,
			final Throwable cause) {
		super(Messages.Image_Build_Failed_Header + image, cause);
		this.image = image;
	}

	public DockerImageBuildFailedException(final String message) {
		super(Messages.Image_Build_Failed_Header + message);
		this.image = null;
	}

	public DockerImageBuildFailedException(final String image,
			final String message) {
		super(Messages.Image_Build_Failed_Header + image + ": " + message);
		this.image = image;
	}

	public String getImage() {
		return image;
	}
}
