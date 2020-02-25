/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.reddeer.utils;

import static org.junit.Assert.assertFalse;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.eclipse.ui.browser.BrowserEditor;
import org.eclipse.reddeer.eclipse.ui.browser.WebBrowserView;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * Class is checking contents of browser.
 * 
 */

public class BrowserContentsCheck {

	/**
	 * 
	 * Close browser if it was opened.
	 * 
	 */
	protected static void closeBrowser() {
		try {
			BrowserEditor browser = new BrowserEditor(new RegexMatcher(".*"));
			while (browser != null) {
				browser.close();
				try {
					browser = new BrowserEditor(new RegexMatcher(".*"));
				} catch (CoreLayerException ex) {
					// Browser editor is not opened
					browser = null;
				}
			}
		} catch (CoreLayerException ex) {
			return;
		}
	}

	/**
	 * 
	 * Checks browser, if page is accessible and address is correct.
	 * 
	 * @param browserEditor
	 */
	public static void checkBrowserForErrorPage(BrowserEditor browserEditor) {
		evaluateBrowserPage(browserEditor.getText());
	}

	/**
	 * 
	 * Checks browser, if page is accessible and address is correct.
	 * 
	 * @param browserView
	 * @param url
	 */
	public static void checkBrowserForErrorPage(WebBrowserView browserView, String url) {
		// Try to refresh page if it is not loaded.
		if (browserView.getText().contains("Unable") || browserView.getText().contains("404")) {
			if (url == null) {
				browserView.refreshPage();
			} else {
				browserView.openPageURL(url);
			}
		}
		new WaitWhile(new JobIsRunning());
		evaluateBrowserPage(browserView.getText());
	}

	/**
	 * 
	 * Evaluate page text, if it not empty or without error.
	 * 
	 * @param browserPage
	 */
	private static void evaluateBrowserPage(String browserPage) {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		assertFalse(
				"Browser contains text 'Status 404'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Status 404") || browserPage.contains("404 - Not Found"));
		assertFalse(
				"Browser contains text 'Error processing request'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Error processing request"));
		assertFalse(
				"Browser contains text 'Forbidden'\n Console output:\n" + consoleView.getConsoleText()
						+ System.getProperty("line.separator") + "Browser contents:" + browserPage,
				browserPage.contains("Forbidden"));

	}

	/**
	 * 
	 * Wait condition if browser is empty.
	 * 
	 */
	class BrowserIsNotEmpty extends AbstractWaitCondition {

		BrowserEditor browser;

		public BrowserIsNotEmpty(BrowserEditor browser) {
			this.browser = browser;
		}

		@Override
		public boolean test() {
			return !browser.getText().equals("");
		}

		@Override
		public String description() {
			return "Browser is empty!";
		}
	}

}