package org.eclipse.linuxtools.docker.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 5.8
 */
public interface IDockerConnection4
		extends IDockerConnection, IDockerConnection2, IDockerConnection3 {

	/**
	 * Get the default progress handler for pulling images.
	 *
	 * @param image
	 *            name of image being pulled
	 * @param monitor
	 *            A Jobs progress monitor. If it is cancelled the pull will be
	 *            cancelled, too.
	 * @return progress handler
	 * @since 5.7
	 */
	IDockerProgressHandler getDefaultPullImageProgressHandler(String image,
			IProgressMonitor monitor);
}
