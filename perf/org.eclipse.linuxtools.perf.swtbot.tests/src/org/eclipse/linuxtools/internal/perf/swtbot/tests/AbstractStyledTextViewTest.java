/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.ui.IViewReference;
import org.hamcrest.Matcher;

/**
 * Specialized abstract SWTBot test for views containing
 * <code>StyledText<code>, subclasses must provide the view id and expected text.
 */
public abstract class AbstractStyledTextViewTest extends AbstractSWTBotTest {

    @Override
    protected void testPerfView() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        Matcher<IViewReference> withPartName = withPartName(getViewId());
        SWTBotView view = bot.view(withPartName);
        assertNotNull(view);

        view.setFocus();
        SWTBotStyledText text = bot.styledText();
        assertNotNull(text);
        assertEquals(getExpectedText(), text.getText());
    }

    /**
     * Get unique test view identifier.
     * @return String unique identifier of view part to test.
     */
    protected abstract String getViewId();

    /**
     * Get exptected text of <code>StyledText</code> widget contained in this view.
     * @return String expected text of view.
     */
    protected abstract String getExpectedText();
}
