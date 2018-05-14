/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
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
package org.eclipse.linuxtools.mylyn.osio.rest.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestOSIORestClient.class, TestOSIORestGetAuthUser.class, TestOSIORestGetSingleTask.class,
	TestOSIORestGetTaskAssignee.class, TestOSIORestGetTaskComments.class, TestOSIORestGetTaskCreator.class,
	TestOSIORestGetTaskData.class, TestOSIORestGetTaskLabels.class, TestOSIORestGetTaskLinks.class,
	TestOSIORestGetUser.class, TestOSIORestGetWorkItem.class, TestOSIORestPatchUpdateTask.class,
	TestOSIORestPostNewComment.class, TestOSIORestPostNewLabel.class, TestOSIORestPostNewLink.class,
	TestOSIORestPostNewTask.class})
public class AllTests {
}
