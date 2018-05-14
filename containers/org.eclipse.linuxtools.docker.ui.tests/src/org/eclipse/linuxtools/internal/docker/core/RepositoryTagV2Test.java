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

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifying that a "tags/list" registry response in the V2 format can be
 * deserialized
 */
public class RepositoryTagV2Test {

	@Test
	public void shouldDeserializeResponseEntity() throws JsonParseException, JsonMappingException, IOException {
		// given
		final String responseEntity = "{\"name\":\"jboss/wildfly\",\"tags\":[\"10.0.0.Final\",\"10.1.0.Final\",\"8.1.0.Final\",\"8.2.0.Final\",\"8.2.1.Final\",\"9.0.0.Final\",\"9.0.1.Final\",\"9.0.2.Final\",\"latest\"]}";
		// when
		final RepositoryTagV2 result = new ObjectMapper().readValue(responseEntity, RepositoryTagV2.class);
		// then
		assertThat(result.getName()).isEqualTo("jboss/wildfly");
		assertThat(result.getTags()).hasSize(9).contains(new RepositoryTag("latest", RepositoryTagV2.UNKNOWN_LAYER));
	}

}
