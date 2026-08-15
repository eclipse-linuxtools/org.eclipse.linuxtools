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

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.linuxtools.internal.docker.ui.consoles.StyledTextBuilder.ESC;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Testing that the colored output are properly generated into
 * {@link StyledText}.
 */
public class StyledConsoleLogsTest {

	private static class ParametersBuilder {

		private final List<Arguments> parameters = new ArrayList<>();

		ParametersBuilder add(final String lineText, final StyledString expectation) {
			parameters.add(Arguments.of(lineText, expectation));
			return this;
		}

		Stream<Arguments> build() {
			return parameters.stream();
		}
	}

	// don't use name = "{0}" to display the unit test name, as it breaks the build
	// on Hudson because of an invalid XML character
	public static Stream<Arguments> getData() {
		final ParametersBuilder parametersBuilder = new ParametersBuilder();
		parametersBuilder.add(ESC + "[33mcontainerid|" + ESC + "[0mstandard_content",
				new StyledString().append("containerid|", StylerBuilder.styler(33)).append("standard_content",
						StylerBuilder.defaultStyler()));
		parametersBuilder.add("prefix_content" + ESC + "[33mcontainerid|" + ESC + "[0mstandard_content",
				new StyledString().append("prefix_content", StylerBuilder.defaultStyler())
						.append("containerid|", StylerBuilder.styler(33))
						.append("standard_content", StylerBuilder.defaultStyler()));
		parametersBuilder.add(ESC + "[33merror_content",
				new StyledString().append("error_content", StylerBuilder.styler(33)));
		parametersBuilder.add(ESC + "[33mcontainerid|" + ESC + "[0mstandard_content1" + ESC + "[34mcolorized_content"
				+ ESC + "[0mstandard_content2",
				new StyledString().append("containerid|", StylerBuilder.styler(33))
						.append("standard_content1", StylerBuilder.defaultStyler())
						.append("colorized_content", StylerBuilder.styler(34))
						.append("standard_content2", StylerBuilder.defaultStyler()));
		parametersBuilder.add(
				ESC + "[33mcontainerid|" + ESC + "[0mstandard_content1" + ESC + "[34mcolorized_content" + ESC
						+ "[0mstandard_content2" + ESC + "[35mcolorized_content2",
				new StyledString().append("containerid|", StylerBuilder.styler(33))
						.append("standard_content1", StylerBuilder.defaultStyler())
						.append("colorized_content", StylerBuilder.styler(34))
						.append("standard_content2", StylerBuilder.defaultStyler())
						.append("colorized_content2", StylerBuilder.styler(35)));
		parametersBuilder.add(ESC + "[0m" + ESC + "[33merror_content",
				new StyledString().append("", StylerBuilder.defaultStyler()).append("error_content",
						StylerBuilder.styler(33)));
		parametersBuilder.add(ESC + "[0m", new StyledString().append("", StylerBuilder.defaultStyler()));
		return parametersBuilder.build();
	}

	@ParameterizedTest
	@MethodSource("getData")
	public void shouldGenerateStyledString(final String lineText, final StyledString expectedStyledString) {
		// given
		// when
		final StyledString result = StyledTextBuilder.parse(lineText);
		// then
		assertThat(SWTUtils.syncExec(() -> result.getStyleRanges()))
				.isEqualTo(SWTUtils.syncExec(() -> expectedStyledString.getStyleRanges()));
		assertThat(result.getString()).isEqualTo(expectedStyledString.getString());
	}

}
