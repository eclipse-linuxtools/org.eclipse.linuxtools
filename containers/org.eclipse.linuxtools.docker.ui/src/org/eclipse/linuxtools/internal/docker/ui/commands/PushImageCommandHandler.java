/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.DefaultImagePushProgressHandler;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImagePush;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to push a given image to the registry
 */
public class PushImageCommandHandler extends AbstractHandler {

	private final static String PUSH_IMAGE_JOB_TITLE = "ImagePush.title"; //$NON-NLS-1$
	private final static String PUSH_IMAGE_JOB_TASK = "ImagePush.msg"; //$NON-NLS-1$
	private static final String ERROR_PUSHING_IMAGE = "ImagePushError.msg"; //$NON-NLS-1$
	private static final String NO_CONNECTION = "NoConnection.error"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerImage selectedImage = CommandUtils
				.getSelectedImage(activePart);
		final ImagePush wizard = new ImagePush(selectedImage,
				selectedImage.repo() + ":" + selectedImage.tags().get(0));
		final boolean pushImage = CommandUtils.openWizard(wizard,
				HandlerUtil.getActiveShell(event));
		if (pushImage) {
			final IDockerConnection connection = CommandUtils
					.getCurrentConnection(activePart);
			performPushImage(wizard, connection);
		}
		return null;
	}

	private void performPushImage(final ImagePush wizard,
			final IDockerConnection connection) {
		if (connection == null) {
			Display.getDefault()
					.syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(ERROR_PUSHING_IMAGE,
									wizard.getSelectedImageTag()),
							DVMessages.getFormattedString(NO_CONNECTION)));
			return;
		}
		final Job pushImageJob = new Job(DVMessages.getFormattedString(
				PUSH_IMAGE_JOB_TITLE, wizard.getSelectedImageTag())) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final IDockerImage image = wizard.getImage();
				final String defaultImageNameTag = wizard.getDefaultImageName();
				final String selectedImageNameTag = wizard
						.getSelectedImageTag();
				// TODO: remove cast once AbstractRegistry methods are
				// part of the IRegistry interface
				final AbstractRegistry registry = (AbstractRegistry) wizard
						.getRegistry();
				final boolean forceTagging = wizard.isForceTagging();
				final boolean keepTaggedImage = wizard.isKeepTaggedImage();

				monitor.beginTask(DVMessages.getString(PUSH_IMAGE_JOB_TASK),
						IProgressMonitor.UNKNOWN);

				// push the image and let the progress
				// handler refresh the images when done
				final String tmpRegistryTag = getNameToTag(
						selectedImageNameTag, registry);
				boolean tagCreated = false;
				try {
					// tag image (if necessary or if '--force' option was
					// selected)
					if (!image.repoTags().contains(tmpRegistryTag)
							|| forceTagging) {
						// TODO: remove cast to DockerConnection once the
						// 'tagImage' is added in the public API
						((DockerConnection) connection).tagImage(
								defaultImageNameTag, tmpRegistryTag,
								forceTagging);
						tagCreated = true;
					}
					// push image
					if (!registry.isAuthProvided()) {
						connection.pushImage(tmpRegistryTag,
								new DefaultImagePushProgressHandler(connection,
										tmpRegistryTag));
					} else {
						final IRegistryAccount registryAccount = (IRegistryAccount) registry;
						connection.pushImage(tmpRegistryTag, registryAccount,
								new DefaultImagePushProgressHandler(connection,
										tmpRegistryTag));
					}
				} catch (final DockerException e) {
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(ERROR_PUSHING_IMAGE,
									defaultImageNameTag),
							e.getMessage()));
					// for now
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					if (tagCreated && !keepTaggedImage) {
						try {
							connection.removeTag(tmpRegistryTag);
							connection.getImages(true);
						} catch (Exception e) {
							// do nothing
						}
					}
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};
		pushImageJob.schedule();
	}

	/**
	 * Computes the full repo/name/tag to apply on the given image
	 * 
	 * @param repoTag
	 *            the repo/tag that could be added
	 * @param registry
	 *            the target registry where the image will be pushed
	 * @return the full image name to tag the image with, or <code>null</code>
	 *         if the image already has this tag.
	 */
	private static String getNameToTag(final String repoTag,
			final AbstractRegistry registry) {
		if (registry.isDockerHubRegistry()) {
			return repoTag;
		}
		return registry.getServerHost() + '/' + repoTag;
	}

}
