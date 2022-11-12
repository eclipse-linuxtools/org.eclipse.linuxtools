package org.eclipse.linuxtools.docker.core;

/**
 * @since 5.9
 */
public interface IDockerConnection5 extends IDockerConnection4 {

	/**
	 * Pull an image from the registry
	 *
	 * @param id
	 *            The image to pull
	 * @param info
	 *            Account information needed to log into the registry
	 * @param handler
	 *            A progress handler that gets called on Progress
	 * @throws DockerOperationCancelledException
	 *             If the progress handler throws an
	 *             {@link DockerOperationCancelledException}. Note that
	 *             DockerOperationCancelledException is a child of
	 *             DockerException.
	 * @throws DockerException
	 *             In case of an error
	 * @throws InterruptedException
	 *             If the thread is interrupted
	 */
	void pullImageWithHandler(String id, IRegistryAccount info,
			IDockerProgressHandler handler) throws DockerException,
			InterruptedException, DockerCertificateException;

}
