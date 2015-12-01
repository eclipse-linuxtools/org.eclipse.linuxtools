package org.eclipse.linuxtools.internal.docker.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.internal.docker.ui.utils.IRunnableWithResult;

/**
 * An {@link IRunnableWithResult} that retrives {@link IDockerImageInfo} for a
 * given {@link IDockerImage}
 */
public final class FindImageInfoRunnable
		implements IRunnableWithResult<IDockerImageInfo> {
	private final IDockerImage selectedImage;
	private IDockerImageInfo selectedImageInfo;

	public FindImageInfoRunnable(IDockerImage selectedImage) {
		this.selectedImage = selectedImage;
	}

	@Override
	public void run(final IProgressMonitor monitor) {
		selectedImageInfo = selectedImage.getConnection()
				.getImageInfo(selectedImage.id());
	}

	@Override
	public IDockerImageInfo getResult() {
		return selectedImageInfo;
	}
}