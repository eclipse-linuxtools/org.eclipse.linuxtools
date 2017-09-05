package org.eclipse.linuxtools.docker.reddeer.utils;

/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

import static org.junit.Assert.assertFalse;

import org.eclipse.linuxtools.docker.reddeer.ui.BrowserView;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.eclipse.ui.browser.BrowserEditor;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;

/**
 * 
 * Class is checking contents of browser.
 * 
 * @author rhopp@redhat.com, jkopriva@redhat.com
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
	public static void checkBrowserForErrorPage(BrowserView browserView, String url) {
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

		public boolean test() {
			return !browser.getText().equals("");
		}

		public String description() {
			return "Browser is empty!";
		}
	}

}