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
package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.CTabFolder;
import org.eclipse.reddeer.swt.api.CTabItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.ctab.DefaultCTabFolder;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.swt.keyboard.KeyboardFactory;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class EditDockerFileTest extends AbstractImageBotTest {
	protected static final String IMAGE_NAME = "test-edit-dockerfile";
	private static final String BUILD_IMAGE = "FROM alpine:latest\nMAINTAINER Josef Kopriva <jkopriva@redhat.com>";
	
	@Test
	public void testEditDockerFile() {
		getConnection();
		DockerImagesTab imageTab = openDockerImagesTab();
		
		try {
			String dockerFilePath = new File(EDIT_DOCKERFILE_FOLDER).getCanonicalPath();
			getConnection();
			imageTab.activate();
			new DefaultToolItem("Build Image").click();
			new WaitUntil(new ShellIsAvailable("Build a Docker Image"));
			new LabeledText("Image Name:").setText(IMAGE_NAME);
			new LabeledText("Directory:").setText(dockerFilePath);
			new PushButton("Edit Dockerfile").click();
			new WaitUntil(new ShellIsAvailable("Dockerfile Editor"), TimePeriod.LONG);
			DefaultStyledText defaultStyledText = new DefaultStyledText();
			String editorText = defaultStyledText.getText();
			assertTrue("Editor is empty!", StringUtils.isNotEmpty(editorText));
			defaultStyledText.setText(BUILD_IMAGE);
			CTabFolder tabFolder = new DefaultCTabFolder();
			CTabItem tabItem = tabFolder.getSelection();
			KeyboardFactory.getKeyboard().invokeKeyCombination(SWT.CTRL,'S');
			tabItem.close();
			new DefaultShell("Build a Docker Image").setFocus();
			new FinishButton().click();
			new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		} catch (IOException ex) {
			fail("Resource file not found!");
		}
	}

	@After
	public void after() {
		deleteImageContainer(IMAGE_NAME);
		cleanUpWorkspace();
	}

}