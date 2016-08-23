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

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.linuxtools.internal.docker.ui.consoles.StyledTextBuilder.ESC;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.swt.custom.StyledText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testing that the colored output are properly generated into
 * {@link StyledText}.
 */
@RunWith(Parameterized.class)
public class StyledConsoleLogsTest {

	private static class ParametersBuilder {

		private final List<Object[]> parameters = new ArrayList<>();

		ParametersBuilder add(final String lineText, final StyledString expectation) {
			parameters.add(new Object[] { lineText, expectation });
			return this;
		}

		Object[][] build() {
			return parameters.toArray(new Object[0][0]);
		}
	}

	@Parameters() // don't use name = "{0}" to display the unit test name, as it
					// breaks the build on Hudson because of an invalid XML
					// character
	public static Object[][] getData() {
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

	@Parameter(0)
	public String lineText;

	@Parameter(1)
	public StyledString expectedStyledString;

	@Test
	public void shouldGenerateStyledString() {
		// given
		// when
		final StyledString result = StyledTextBuilder.parse(lineText);
		// then
		assertThat(SWTUtils.syncExec(() -> result.getStyleRanges()))
				.isEqualTo(SWTUtils.syncExec(() -> expectedStyledString.getStyleRanges()));
		assertThat(result.getString()).isEqualTo(expectedStyledString.getString());
	}

}
