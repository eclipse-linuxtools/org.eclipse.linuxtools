package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Base class for {@link ILaunchShortcut} that rely on an
 * {@link IDockerConnection} and an {@link IResource}. A matching
 * {@link ILaunchConfiguration} may be retrieved from the selected
 * {@link IResource} or a new one may be created if needed.
 */
public abstract class BaseResourceAwareLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IResource resource = (IResource) ((IStructuredSelection) selection)
					.toArray()[0];
			launch(resource, mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(editor.getEditorInput().getAdapter(IResource.class), mode);
	}

	/**
	 * Locate a configuration to launch for the given type. If one cannot be
	 * found, create one.
	 * 
	 * @param resource
	 *            The {@code docker-compose.yml} to look up launch for.
	 *
	 * @return A re-useable config or <code>null</code> if none was found.
	 */
	protected ILaunchConfiguration findLaunchConfiguration(
			final String configTypeId, final IResource resource,
			final Predicate<ILaunchConfiguration> predicate) {
		final ILaunchConfigurationType configType = LaunchConfigurationUtils
				.getLaunchConfigType(
						configTypeId);
		final List<ILaunchConfiguration> candidateConfigs = new ArrayList<>();
		try {
			final ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			Stream.of(configs).filter(predicate)
					.forEach(config -> candidateConfigs.add(config));
		} catch (CoreException e) {
			Activator.log(e);
		}
	
		// If there are no existing configurations associated with the
		// given resource,
		// create one. If there is exactly one configuration associated with the
		// given resource, return it. Otherwise, if there is more than one
		// configuration associated with the given resource, prompt the user to
		// choose
		// one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			return createConfiguration(resource);
		} else if (candidateCount == 1) {
			return candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a configuration. A null result means
			// the user
			// cancelled the dialog, in which case this method returns null,
			// since canceling the dialog should also cancel launching
			// anything.
			return chooseConfiguration(candidateConfigs);
		}
	}

	/**
	 * Get the path of the {@code Dockerfile}.
	 * 
	 * @param sourcePathLocation
	 *            - location path of the {@code Dockerfile}
	 * @param sourcePathWorkspaceRelativeLocation
	 *            - true, if path above is relative to workspace
	 * @return the absolute file path of the {@code Dockerfile}
	 */
	public static IPath getPath(final String sourcePathLocation,
			final boolean sourcePathWorkspaceRelativeLocation) {
		if (sourcePathWorkspaceRelativeLocation) {
			final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(sourcePathLocation));
			if (resource != null)
				return resource.getLocation();
			else // return an empty path that won't match an existing resource
				return new Path(""); //$NON-NLS-1$
		}
		return new Path(sourcePathLocation);
	}

	protected abstract ILaunchConfiguration createConfiguration(
			final IResource resource);

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * specified launch configurations.
	 * 
	 * @param configList
	 *            The list of launch configurations to choose from.
	 * @return The chosen config, or <code>null</code> if the user cancelled the
	 *         dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList) {
		IDebugModelPresentation labelProvider = DebugUITools
				.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getActiveWorkbenchShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(
				LaunchMessages.getString(
						"DockerComposeUpShortcutConfigSelection.title")); //$NON-NLS-1$
		dialog.setMessage(
				LaunchMessages
						.getString("DockerComposeUpShortcutChooseLaunch.msg")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == IStatus.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Get the active Workbench shell.
	 * 
	 * @return active shell as returned by the plug-in
	 */
	protected Shell getActiveWorkbenchShell() {
		return Activator.getActiveWorkbenchShell();
	}

	protected abstract void launch(IResource resource, String mode);

}