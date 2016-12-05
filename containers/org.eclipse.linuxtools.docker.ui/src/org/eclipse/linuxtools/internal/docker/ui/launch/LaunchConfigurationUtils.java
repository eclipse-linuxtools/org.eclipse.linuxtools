/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.COMMAND;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CREATION_DATE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.DATA_VOLUMES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_ID;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.INTERACTIVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.LABELS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.LINKS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.MB;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PRIVILEGED;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * Utility class to manage {@link ILaunchConfiguration}
 */
public class LaunchConfigurationUtils {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"YYYY-MM-dd HH:mm:ss"); //$NON-NLS-1$

	/**
	 * Private constructor for this utility class.
	 */
	private LaunchConfigurationUtils() {
		// empty
	}

	/**
	 * @return the ILaunchConfigurationType for the given configuration type.
	 * @param configType
	 *            the id of the configuration type
	 */
	public static ILaunchConfigurationType getLaunchConfigType(
			final String configType) {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(configType);
	}

	/**
	 * Creates a new {@link ILaunchConfiguration} for the given
	 * {@link IDockerContainer}.
	 * 
	 * @param baseConfigurationName
	 *            the base configuration name to use when creating the
	 *            {@link ILaunchConfiguration}.
	 * @param image
	 *            the {@link IDockerImage} used to create the container
	 * @param containerName
	 *            the actual container name (given by the user or generated by
	 *            the Docker daemon)
	 * @param containerConfig
	 * @param hostConfig
	 *            the user-provided {@link IDockerHostConfig} (created
	 *            container's one)
	 * @param removeWhenExits
	 *            flag to indicate if container should be removed when exited
	 * @return the generated {@link ILaunchConfiguration}
	 * 
	 */
	public static ILaunchConfiguration createRunImageLaunchConfiguration(
			final IDockerImage image,
			final IDockerContainerConfig containerConfig,
			final IDockerHostConfig hostConfig, final String containerName,
			final boolean removeWhenExits) {
		try {
			final ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			final ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(
							IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
			final String imageName = createRunImageLaunchConfigurationName(
					image);
			// using the image repo + first tag
			final ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfigurationWorkingCopy(
					type, imageName);
			workingCopy.setAttribute(CREATION_DATE,
					DATE_FORMAT.format(new Date()));
			workingCopy.setAttribute(CONNECTION_NAME,
					image.getConnection().getName());
			workingCopy.setAttribute(IMAGE_ID, image.id());
			workingCopy.setAttribute(IMAGE_NAME,
					createRunImageLaunchConfigurationName(image));
			if (containerName != null && !containerName.isEmpty()) {
				workingCopy.setAttribute(CONTAINER_NAME, containerName);
			}
			// if we know the raw command string, use it since the container
			// config will remove quotes to split up command properly
			DockerContainerConfig config = (DockerContainerConfig) containerConfig;
			if (config.rawcmd() != null) {
				workingCopy.setAttribute(COMMAND, config.rawcmd());
			} else {
				workingCopy.setAttribute(COMMAND,
						toString(containerConfig.cmd()));
			}
			workingCopy.setAttribute(ENTRYPOINT,
					toString(containerConfig.entrypoint()));
			// selected ports
			workingCopy.setAttribute(PUBLISH_ALL_PORTS,
					hostConfig.publishAllPorts());
			// format: <containerPort><type>:<hostIP>:<hostPort>
			if (hostConfig.publishAllPorts()) {
				final IDockerImageInfo imageInfo = image.getConnection()
						.getImageInfo(image.id());
				if (imageInfo != null) {
					workingCopy.setAttribute(PUBLISHED_PORTS,
							serializePortBindings(imageInfo.containerConfig()
									.exposedPorts()));
				}
			} else {
				workingCopy.setAttribute(PUBLISHED_PORTS,
						serializePortBindings(hostConfig.portBindings()));
			}
			// links (with format being: "<containerName>:<containerAlias>")
			workingCopy.setAttribute(LINKS, hostConfig.links());
			// env variables
			workingCopy.setAttribute(ENV_VARIABLES, containerConfig.env());
			// labels
			workingCopy.setAttribute(LABELS, containerConfig.labels());
			// volumes
			final List<String> volumes = new ArrayList<>();
			// volumes from other containers
			for (String volumeFrom : hostConfig.volumesFrom()) {
				final DataVolumeModel volume = DataVolumeModel
						.parseVolumeFrom(volumeFrom);
				if (volume != null) {
					volumes.add(volume.toString());
				}
			}
			// bindings to host directory or file
			for (String bind : hostConfig.binds()) {
				final DataVolumeModel volume = DataVolumeModel
						.parseHostBinding(bind);
				if (volume != null) {
					volumes.add(volume.toString());
				}
			}
			// TODO: container path declaration

			workingCopy.setAttribute(DATA_VOLUMES, volumes);
			// options
			workingCopy.setAttribute(AUTO_REMOVE, removeWhenExits);
			workingCopy.setAttribute(ALLOCATE_PSEUDO_CONSOLE,
					containerConfig.tty());
			workingCopy.setAttribute(INTERACTIVE, containerConfig.openStdin());
			workingCopy.setAttribute(PRIVILEGED, hostConfig.privileged());
			// resources limitations
			if (containerConfig.memory() != null) {
				workingCopy.setAttribute(ENABLE_LIMITS, true);
				// memory in containerConfig is expressed in bytes
				workingCopy.setAttribute(MEMORY_LIMIT, Long
						.toString(containerConfig.memory().longValue() / MB));
			}
			if (containerConfig.cpuShares() != null) {
				workingCopy.setAttribute(ENABLE_LIMITS, true);
				workingCopy.setAttribute(CPU_PRIORITY,
						containerConfig.cpuShares().toString());
			}
			return workingCopy.doSave();
		} catch (CoreException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.creation.failure"), //$NON-NLS-1$
					e));
		}
		return null;
	}

	private static String createRunImageLaunchConfigurationName(
			final IDockerImage image) {
		return image.repoTags().get(0);
	}

	private static String toString(final List<String> input) {
		final StringBuilder command = new StringBuilder();
		for (Iterator<String> iterator = input.iterator(); iterator
				.hasNext();) {
			String fragment = iterator.next();
			command.append(fragment);
			if (iterator.hasNext()) {
				command.append(" ");
			}
		}
		return command.toString();
	}

	/**
	 * Serializes the given Port Bindings to save them in an
	 * {@link ILaunchConfiguration}
	 * 
	 * @param bindings
	 * @return a {@link List} of port bindings serialized in the following
	 *         format:
	 *         <code>&lt;containerPort&gt;&lt;type&gt;:&lt;hostIP&gt;:&lt;hostPort&gt;</code>
	 *         Note that the <code>&lt;hostIP&gt;</code> part may be empty if
	 *         undefined by the user.
	 */
	public static List<String> serializePortBindings(
			final Map<String, List<IDockerPortBinding>> bindings) {
		final List<String> serializedBindings = new ArrayList<>();
		if (bindings != null) {
			for (Entry<String, List<IDockerPortBinding>> entry : bindings
					.entrySet()) {
				for (IDockerPortBinding portBinding : entry.getValue()) {
					final StringBuilder portBindingBuilder = new StringBuilder();
					portBindingBuilder.append(entry.getKey());
					portBindingBuilder.append(':'); // $NON-NLS-1$
					if (portBinding.hostIp() != null) {
						portBindingBuilder.append(portBinding.hostIp());
					}
					portBindingBuilder.append(':'); // $NON-NLS-1$
					portBindingBuilder.append(portBinding.hostPort());
					serializedBindings.add(portBindingBuilder.toString());
				}
			}
		}
		return serializedBindings;
	}

	/**
	 * Serializes the given Port Bindings to save them in an
	 * {@link ILaunchConfiguration}
	 * 
	 * @param bindings
	 * @return a {@link List} of port bindings serialized in the following
	 *         format:
	 *         <code>&lt;containerPort&gt;:&lt;type&gt;:&lt;hostIP&gt;:&lt;hostPort&gt;</code>
	 *         Note that the <code>&lt;hostIP&gt;</code> part may be empty if
	 *         undefined by the user.
	 *         <p>
	 *         For example: <code>8080/tcp:1.2.3.4:8080</code> or
	 *         <code>8080/tcp::8080</code>
	 *         </p>
	 */
	public static List<String> serializePortBindings(
			final Set<String> bindings) {
		final List<String> serializedBindings = new ArrayList<>();
		if (bindings != null) {
			for (String portBinding : bindings) {
				final StringBuilder portBindingBuilder = new StringBuilder();
				portBindingBuilder.append(portBinding);
				portBindingBuilder.append(':'); // $NON-NLS-1$
				portBindingBuilder.append(':'); // $NON-NLS-1$
				final String[] containerPort = portBinding.split("/"); // $NON-NLS-1$
				portBindingBuilder.append(containerPort[0]);
				serializedBindings.add(portBindingBuilder.toString());
			}
		}
		return serializedBindings;
	}

	/**
	 * Deserializes the given Port Bindings to use them in an
	 * {@link ILaunchConfiguration}
	 * 
	 * @param bindings
	 *            a {@link List} of serialized bindings
	 * @return a {@link Map} of {@link List} of {@link IDockerPortBinding}
	 *         indexed by their associated container port
	 * @see LaunchConfigurationUtils#serializePortBindings(Map)
	 */
	public static Map<String, List<IDockerPortBinding>> deserializePortBindings(
			final List<String> bindings) {
		if (bindings == null) {
			return Collections.emptyMap();
		}
		return bindings.stream().map(b -> b.split(":"))
				.collect(Collectors.toMap(
						// the key in the result map
						split -> split[0],
						// the list with a new element as the value in the
						// result map,
						split -> Arrays.asList(
								new DockerPortBinding(split[1], split[2])),
						// the merge function, to be used if 2 elements exist
						// for the same key
						(e1, e2) -> {
							final List<IDockerPortBinding> merges = new ArrayList<>();
							merges.addAll(e1);
							merges.addAll(e2);
							return merges;
						}));
	}

	/**
	 * Computes the size of the given {@link Button} and returns the width.
	 * 
	 * @param button
	 *            the button for which the size must be computed.
	 * @return the width hint for the given button
	 */
	public static int getButtonWidthHint(final Button button) {
		/* button.setFont(JFaceResources.getDialogFont()); */
		final PixelConverter converter = new PixelConverter(button);
		final int widthHint = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Looks-up the {@link ILaunchConfiguration} with the given type and
	 * <strong>IDockerImage's name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param imageName
	 *            the associated {@link IDockerImage} name
	 * @return the first matching {@link ILaunchConfiguration} or
	 *         <code>null</code> if none was found.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getLaunchConfigurationByImageName(
			final String type, final String imageName) throws CoreException {
		return getLaunchConfigurationByImageName(getLaunchConfigType(type),
				imageName);
	}

	/**
	 * Looks-up the {@link ILaunchConfiguration} with the given type and
	 * <strong>IDockerImage's name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param imageName
	 *            the associated {@link IDockerImage} name
	 * @return the first matching {@link ILaunchConfiguration} or
	 *         <code>null</code> if none was found.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getLaunchConfigurationByImageName(
			final ILaunchConfigurationType type, final String imageName)
			throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfiguration lastLaunchConfiguration = null;
		String lastCreationDate = ""; //$NON-NLS-1$
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(type)) {
			final String launchConfigImageName = launchConfiguration
					.getAttribute(IMAGE_NAME, ""); //$NON-NLS-1$
			final String launchConfigCreationDate = launchConfiguration
					.getAttribute(CREATION_DATE, ""); //$NON-NLS-1$
			if (launchConfigImageName.equals(imageName)
					&& launchConfigCreationDate
							.compareTo(lastCreationDate) > 0) {
				lastCreationDate = launchConfigCreationDate;
				lastLaunchConfiguration = launchConfiguration;
			}
		}
		return lastLaunchConfiguration;
	}

	/**
	 * Returns the {@link ILaunchConfigurationWorkingCopy} with the given type
	 * and <strong>IDockerImage's name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param imageName
	 *            the associated {@link IDockerImage} name
	 * @param createIfNotFound
	 *            flag to indicate if a new {@link ILaunchConfiguration} should
	 *            be created if none was found.
	 * @return the ILaunchConfigurationWorkingCopy for the matching
	 *         {@link ILaunchConfiguration} or a new instance if none was found.
	 * @throws CoreException
	 */
	private static ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy(
			final ILaunchConfigurationType type, final String imageName)
			throws CoreException {
		final ILaunchConfiguration existingLaunchConfiguration = getLaunchConfigurationByImageName(
				type, imageName);
		if (existingLaunchConfiguration != null) {
			return existingLaunchConfiguration.getWorkingCopy();
		}
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		final String configurationName = manager
				.generateLaunchConfigurationName(imageName);
		return type.newInstance(null, configurationName);
	}

	/**
	 * Looks-up the {@link ILaunchConfiguration} with the given type and
	 * <strong>name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param name
	 *            the name
	 * @return the first matching {@link ILaunchConfiguration} or
	 *         <code>null</code> if none was found.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getLaunchConfigurationByName(
			final String type, final String name) throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(getLaunchConfigType(type))) {
			final String launchConfigName = launchConfiguration.getName(); // $NON-NLS-1$
			if (launchConfigName.equals(name)) {
				return launchConfiguration;
			}
		}
		return null;
	}

	/**
	 * Converts the given <code>path</code> to a Unix path if the current OS if
	 * {@link Platform#OS_WIN32}
	 * 
	 * @param path
	 *            the path to convert
	 * @return the converted path or the given path, depending on the current
	 *         OS.
	 * @see <a href=
	 *      "http://docs.docker.com/v1.7/userguide/dockervolumes/#mount-a-host-directory-as-a-data-volume">
	 *      http://docs.docker.com/v1.7/userguide/dockervolumes/#mount-a-host-
	 *      directory-as-a-data-volume</a>
	 */
	public static String convertToUnixPath(String path) {
		return convertToUnixPath(Platform.getOS(), path);
	}

	/**
	 * Converts the given path to a portable form, replacing all "\" and ": "
	 * with "/" if the given <code>os</code> is {@link Platform#OS_WIN32}.
	 * 
	 * @param os
	 *            the current OS
	 * @param path
	 *            the path to convert
	 * @return the converted path or the given path
	 * @see LaunchConfigurationUtils#convertToUnixPath(String)
	 * @see {@link Platform#getOS()}
	 */
	public static String convertToUnixPath(final String os, final String path) {
		if (os != null && os.equals(Platform.OS_WIN32)) {
			// replace all "\" with "/" and then drive info (eg "C:/" to "/c/")
			final Matcher m = Pattern.compile("([a-zA-Z]):/") //$NON-NLS-1$
					.matcher(path.replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
			if (m.find()) {
				final StringBuffer b = new StringBuffer();
				b.append('/');
				m.appendReplacement(b, m.group(1).toLowerCase());
				b.append('/');
				m.appendTail(b);
				return b.toString();
			}
		}
		return path;
	}

	/**
	 * Converts the given <code>path</code> back to a Windows path if the
	 * current OS if {@link Platform#OS_WIN32}.
	 * 
	 * <p>
	 * Note: This is the revert operation of
	 * {@link LaunchConfigurationUtils#convertToUnixPath(String)}.
	 * </p>
	 * 
	 * @param path
	 *            the path to convert
	 * @return the converted path or the given path, depending on the current
	 *         OS.
	 * @see <a href=
	 *      "http://docs.docker.com/v1.7/userguide/dockervolumes/#mount-a-host-directory-as-a-data-volume">
	 *      http://docs.docker.com/v1.7/userguide/dockervolumes/#mount-a-host-
	 *      directory-as-a-data-volume</a>
	 */
	public static String convertToWin32Path(String path) {
		return convertToWin32Path(Platform.getOS(), path);
	}

	/**
	 * Converts the given path to a portable form, replacing all "\" and ": "
	 * with "/" if the given <code>os</code> is {@link Platform#OS_WIN32}.
	 * 
	 * @param os
	 *            the current OS
	 * @param path
	 *            the path to convert
	 * @return the converted path or the given path
	 * @see LaunchConfigurationUtils#convertToWin32Path(String)
	 * @see {@link Platform#getOS()}
	 */
	public static String convertToWin32Path(final String os,
			final String path) {
		if (os != null && os.equals(Platform.OS_WIN32)) {
			// replace all "/" with "\" and then drive info (eg "/c/" to "C:/")
			final Matcher m = Pattern.compile("^/([a-zA-Z])/").matcher(path); //$NON-NLS-1$
			if (m.find()) {
				final StringBuffer b = new StringBuffer();
				m.appendReplacement(b, m.group(1).toUpperCase());
				b.append(":\\"); //$NON-NLS-1$
				m.appendTail(b);
				return b.toString().replace('/', '\\'); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return path;
	}

	/**
	 * Creates a Launch Configuration name from the given repoName or from the
	 * given resource's project if the repoName was <code>null</code>.
	 * 
	 * @param imageName
	 *            the full image name
	 * @param resource
	 *            the Dockerfile to use to build the image
	 * @return the {@link ILaunchConfiguration} name
	 */
	public static String createBuildImageLaunchConfigurationName(
			final String imageName, final IResource resource) {
		if (imageName != null) {
			final String repository = BuildDockerImageUtils
					.getRepository(imageName);
			final String name = BuildDockerImageUtils.getName(imageName);
			final String tag = BuildDockerImageUtils.getTag(imageName);
			final StringBuilder configNameBuilder = new StringBuilder();
			// image name is the minimum requirement
			if (name != null) {
				if (repository != null) {
					configNameBuilder.append(repository).append('_'); // $NON-NLS-1$
				}
				if (name != null) {
					configNameBuilder.append(name);
				}
				if (tag != null) {
					configNameBuilder.append(" [").append(tag).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					configNameBuilder.append(" [latest]"); //$NON-NLS-1$
				}
				return configNameBuilder.toString();
			}
		}
		return "Dockerfile [" //$NON-NLS-1$
				+ resource.getProject().getName() + "]"; //$NON-NLS-1$
	}

	/**
	 * Creates a new {@link ILaunchConfiguration} to build an
	 * {@link IDockerImage} from a Dockerfile {@link IResource}.
	 * 
	 * @param connection
	 *            the connection to use to submit the build
	 * @param repoName
	 *            the repo/name of the {@link IDockerImage} to build
	 * @param dockerfile
	 *            the dockerfile to use to build the {@link IDockerImage}
	 * @return the created {@link ILaunchConfiguration}
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createBuildImageLaunchConfiguration(
			final IDockerConnection connection, final String repoName,
			final IResource dockerfile) throws CoreException {
		final ILaunchConfigurationType configType = LaunchConfigurationUtils
				.getLaunchConfigType(
						IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
		final ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy(
				configType,
				createBuildImageLaunchConfigurationName(repoName, dockerfile));
		wc.setAttribute(
				IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
				dockerfile.getFullPath().removeLastSegments(1).toString());
		wc.setAttribute(
				IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
				true);

		wc.setAttribute(IDockerImageBuildOptions.DOCKER_CONNECTION,
				connection.getName());
		wc.setAttribute(IDockerImageBuildOptions.REPO_NAME, repoName);
		return wc.doSave();
	}

	/**
	 * Creates an {@link ILaunchConfiguration} for to run the
	 * {@code docker-compose up} command.
	 * 
	 * @param connection
	 *            the Docker connection to use
	 * @param dockerComposeScript
	 *            the {@code docker-compose.yml} script
	 * @return the created {@link ILaunchConfiguration}
	 * @throws CoreException
	 *             if something wrong happened when creating the
	 *             {@link ILaunchConfiguration}
	 */
	public static ILaunchConfiguration createDockerComposeUpLaunchConfiguration(
			final IDockerConnection connection,
			final IResource dockerComposeScript) throws CoreException {
		final ILaunchConfigurationType configType = LaunchConfigurationUtils
				.getLaunchConfigType(
						IDockerComposeLaunchConfigurationConstants.CONFIG_TYPE_ID);
		final ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				DebugPlugin.getDefault().getLaunchManager()
						.generateLaunchConfigurationName(
								createDockerComposeLaunchConfigurationName(
										dockerComposeScript)));
		wc.setAttribute(IDockerComposeLaunchConfigurationConstants.WORKING_DIR,
				dockerComposeScript.getFullPath().removeLastSegments(1)
						.toString());
		wc.setAttribute(
				IDockerComposeLaunchConfigurationConstants.WORKING_DIR_WORKSPACE_RELATIVE_LOCATION,
				true);

		wc.setAttribute(
				IDockerComposeLaunchConfigurationConstants.DOCKER_CONNECTION,
				connection.getName());
		return wc.doSave();
	}

	/**
	 * Creates a Launch Configuration name from the given
	 * {@code dockerComposeScript}.
	 * 
	 * @param dockerComposeScript
	 *            the name of the {@code Docker Compose} script
	 * @return the {@link ILaunchConfiguration} name
	 */
	private static String createDockerComposeLaunchConfigurationName(
			final IResource dockerComposeScript) {
		return "Docker Compose [" //$NON-NLS-1$
				+ dockerComposeScript.getProject().getName() + "]"; //$NON-NLS-1$
	}

	/**
	 * Updates all {@link ILaunchConfiguration} of the given {@code type} where
	 * there is an attribute with the given {@code attributeName} of the given
	 * {@code oldValue}, and sets the {@code newValue} instead.
	 * 
	 * @param type
	 *            the type of {@link ILaunchConfiguration} to find
	 * @param attributeName
	 *            the name of the attribute to look-up
	 * @param oldValue
	 *            the old value to match
	 * @param newValue
	 *            the new value to set
	 */
	public static void updateLaunchConfigurations(final String type,
			final String attributeName, final String oldValue,
			final String newValue) {
		final ILaunchConfigurationType configType = LaunchConfigurationUtils
				.getLaunchConfigType(type);
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		try {
			for (ILaunchConfiguration config : manager
					.getLaunchConfigurations(configType)) {
				try {
					if (config.getAttribute(attributeName, "") //$NON-NLS-1$
							.equals(oldValue)) {
						final ILaunchConfigurationWorkingCopy workingCopy = config
								.getWorkingCopy();
						workingCopy.setAttribute(attributeName, newValue);
						workingCopy.doSave();
					}
				} catch (CoreException e) {
					Activator.logErrorMessage(LaunchMessages.getFormattedString(
							"UpdateLaunchConfiguration.named.error", //$NON-NLS-1$
							config.getName()), e);
				}
			}
		} catch (CoreException e) {
			Activator.logErrorMessage(
					LaunchMessages.getString("UpdateLaunchConfiguration.error" //$NON-NLS-1$
					), e);
			Activator.logErrorMessage(
					"Failed to retrieve launch configurations after connection name changed",
					e);
		}
	}

}
