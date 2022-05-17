/********************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    * Red Hat Inc. - Original snippets from ContainerLauncher.java and
 *                     ContainerCommandProcess.java
 *    * Mathema - Merged, deduplicated and enhanced the original code into
 *                CopyFromDockerJob
 *
 ********************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.CloseableContainer;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;

/**
 * A Job that copies data from a docker image or a running container to the
 * local system. It supports the normal copy mode as well as a mirror mode. In
 * mirror mode all symlinks necessary to reach the passed folder will be
 * created. Also all folders being pointed at by symlinks within that folder
 * will be copied up to MAXLINKDEPTH.
 *
 */
public class CopyFromDockerJob extends Job {

	/** The file to save the current state of the copied files between runs */
	private static final String COPY_STATE_FILE = ".copyState"; //$NON-NLS-1$

	/**
	 * The file to save the docker-image-id. If it changed the files must be
	 * invalidated (deleted)
	 */
	private static final String IMAGE_ID_FILE = ".image_id"; //$NON-NLS-1$

	private final static boolean isWin = java.io.File.separatorChar == '\\'; // $NON-NLS-1$

	/**
	 * The maximum number of symliks to follow.
	 */
	private static final int MAXLINKDEPTH = 20;

	/**
	 * Lock-Object for the static variables that can be accessed from multiple jobs
	 */
	private static Object m_lockObject = new Object();

	/**
	 * This locks the initialization of a mirror-job.
	 */
	private static Object m_lockObjectManagedInit = new Object();

	/**
	 * [Mirror-mode] Maps a target path to an image/container id
	 *
	 * This is necessary to handle the case where the image changes.
	 *
	 * This should only be accessed under {@link #m_lockObjectManagedInit}
	 */
	private static Map<Path, String> m_targetpathToImageIdMap = new HashMap<>();

	/**
	 * A map of host paths to lists of files that were already copied to that
	 * location
	 *
	 * Only used in Mirror-mode
	 *
	 * This should only be accessed under {@link #m_lockObjectManagedInit}
	 */
	private static Map<Path, HashSet<Path>> m_pathToCopiedList = new HashMap<>();

	/**
	 * Maps a target path to the {@link #m_copyingMap}. This is used to synchronize
	 * multiple jobs in mirror-mode.
	 *
	 * This must only be accessed under {@link #m_lockObjectManagedInit}
	 */
	private static Map<Path, Map<Path, Job>> m_targetpathToCopyingMap = new HashMap<>();

	/**
	 * The jobs currently copying a certain folder. This is only used in mirror-mode
	 *
	 * This must be accessed under {@link #m_lockObject}
	 *
	 */
	private Map<Path, Job> m_copyingMap;
	/**
	 * [Mirror-mode] Already copied source folders
	 *
	 * This must be accessed under {@link #m_lockObject}
	 */
	private Set<Path> m_copiedList;

	/**
	 * The monitor assigned to the job
	 */
	private IProgressMonitor m_monitor = null;

	/** The docker connection */
	private final DockerConnection m_connection;
	/** The image to copy from - can be null if copying from a container */
	private final String m_image;
	/** The folder to copy to */
	private final Path m_targetfolder;
	/** Whether this is a mirror-job */
	private final boolean m_mirror;
	/** The paths to copy */
	private Map<Path, Path> m_pathsToCopy;
	/** The container to copy from */
	private String m_containerId = null;
	/** The id of the image-id of the image */
	private String m_imageId = null;

	/**
	 * Get the global set of copied paths and copy it into this job
	 *
	 * @param folder The folder with the paths
	 * @return a set of paths already copied
	 */
	@SuppressWarnings("unchecked")
	public static Set<IPath> getCopiedPaths(Path folder) {

		HashSet<Path> ps = getCopiedSet(folder);
		synchronized (m_lockObject) {
			return ((Set<IPath>) ps.clone());
		}
	}

	/**
	 * The type of copy operation
	 */
	public enum CopyType {
		/** Normal copy from an image */
		Image,
		/** Normal copy from a container */
		Container,
		/** Mirror (follow symlinks) from an image */
		ImageMirror,
		/** Mirror (follow symlinks) from a container */
		ContainerMirror
	}

	/**
	 * Copy a path from a docker image or container to the local system If copying
	 * from an image, the container will be started. Container and Image must
	 * provide the <code>ls</code> command within the <code>PATH</code> Images must
	 * also provide the <code>sleep</code> command within the <code>PATH</code>
	 *
	 * @param connection The docker connection to use
	 * @param copyType   The kind of copy operation
	 * @param desc       The name or hash of the image or container.
	 * @param copySet    The paths to copy to the target folder
	 * @param targetDir  Where to copy folder
	 */
	public CopyFromDockerJob(IDockerConnection connection, CopyType copyType, String desc, Set<Path> copySet,
			Path targetDir) {
		this(connection, copyType, desc, copySet.stream().collect(Collectors.toMap(x -> x, x -> targetDir)));
	}

	/**
	 * Copy a path from a docker image or container to the local system If copying
	 * from an image, the container will be started. Container and Image must
	 * provide the <code>ls</code> command within the <code>PATH</code> Images must
	 * also provide the <code>sleep</code> command within the <code>PATH</code>
	 *
	 * @param connection The docker connection to use
	 * @param copyType   The kind of copy operation
	 * @param desc       The name or hash of the image or container.
	 * @param copyMap    A map of source and destinations folders. In mirror-mode
	 *                   they must all point to the same target folder.
	 */
	public CopyFromDockerJob(IDockerConnection connection, CopyType copyType, String desc, Map<Path, Path> copyMap) {

		super(JobMessages.getString("CopyFromDockerJob.title")); //$NON-NLS-1$

		m_connection = (DockerConnection) connection;

		m_containerId = null;
		if (copyType == CopyType.Container || copyType == CopyType.ContainerMirror) {
			m_image = null;
			m_containerId = desc;
		} else if (copyType == CopyType.Image || copyType == CopyType.ImageMirror) {
			m_image = desc;
			m_containerId = null;
		} else {
			throw new IllegalArgumentException();
		}

		if (copyType == CopyType.Container || copyType == CopyType.Image) {
			m_mirror = false;
			m_targetfolder = null;
		} else {
			m_mirror = true;
			m_targetfolder = copyMap.values().iterator().next();
		}

		// Check that the Container/Image exists
		getImageId();

		if (m_mirror) {
			assert copyMap.values().stream().allMatch(x -> x.equals(m_targetfolder))
					: "In mirror-mode all target paths must point to the same folder"; //$NON-NLS-1$
		}

		this.m_pathsToCopy = copyMap;
	}

	/**
	 * Helper to convert a docker-path to a host path when extracting a tar In
	 * mirror-mode pathInDocker is attached to targetBasePath. In copy mode path is
	 * mapped according to the base paths. It is also ensured, that the base path is
	 * not escaped.
	 *
	 * @param pathInDocker   The normalized path within the docker file system
	 * @param copyBasePath   The base path that is copied from the docker file
	 *                       system
	 * @param targetBasePath The base path to copy the file to
	 * @return The final mapping
	 */
	private Path toHost(IPath pathInDocker, Path copyBasePath, Path targetBasePath) {

		if (m_mirror) {
			return (Path) m_targetfolder.append(pathInDocker);
		}

		assert isInCopyPath(pathInDocker, copyBasePath) : MessageFormat.format("The path {0} is not part of {1}", //$NON-NLS-1$
					pathInDocker.toString(), copyBasePath.toString());

		return (Path) targetBasePath.append(pathInDocker.removeFirstSegments(copyBasePath.segmentCount()));
	}

	/**
	 * Check whether a path is within a path that is copied and not exited, due to a
	 * ".." path (e.g. by a symlink).
	 *
	 * @param dockerpath The path to copy
	 * @param basepath   The path of the tar to copy
	 * @return Whether the path is within the path to copy
	 */
	private boolean isInCopyPath(IPath dockerpath, Path basepath) {
		return m_mirror || basepath.isPrefixOf(dockerpath);
	}

	/**
	 * Map the integer posix-file-mode to Java permissions
	 *
	 * @param mode posix file mode
	 * @return Java permission set
	 */
	private static Set<PosixFilePermission> toPerms(int mode) {
		Set<PosixFilePermission> perms = new HashSet<>();
		if ((mode & 0400) != 0) {
			perms.add(PosixFilePermission.OWNER_READ);
		}
		if ((mode & 0200) != 0) {
			perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if ((mode & 0100) != 0) {
			perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		if ((mode & 0040) != 0) {
			perms.add(PosixFilePermission.GROUP_READ);
		}
		if ((mode & 0020) != 0) {
			perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if ((mode & 0010) != 0) {
			perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		if ((mode & 0004) != 0) {
			perms.add(PosixFilePermission.OTHERS_READ);
		}
		if ((mode & 0002) != 0) {
			perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if ((mode & 0001) != 0) {
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}
		return perms;
	}

	/**
	 * A blocking input stream that waits until data is available.
	 */
	private static class BlockingInputStream extends InputStream {
		private InputStream in;

		public BlockingInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			return in.read();
		}
	}

	/**
	 * Wrapper for a tar-symlink
	 *
	 */
	private final class SymLink {
		public final Path m_filename;
		public final Path m_targetRel; // The absolute or relative target - depending on the symlink
		public final Path m_targetAbs; // The absolute path to the target
		public final boolean m_isAbs;
		public final Path m_basePath;
		public final Path m_hostBasePath;
		public final boolean m_isDirectory;

		/**
		 * Create a Symlink.
		 *
		 * This should only be used in mirror-mode and is likely to cause a
		 * null-ptr-exception. It however does so reliably.
		 *
		 * @param filename     The path of the symlink
		 * @param target       Where the symlink points to (in the source FS)
		 * @param hostBasePath
		 * @param isDirectory
		 */
		public SymLink(final Path filename, final Path target, Path hostBasePath, final boolean isDirectory) {
			m_basePath = null; // Can cause a exception in some methods, but these shouldn't be used when using
								// this ctor.
			m_filename = filename;
			m_targetRel = target;
			m_targetAbs = target;
			m_isAbs = true;
			m_hostBasePath = hostBasePath;
			m_isDirectory = isDirectory;
		}

		/**
		 *
		 * @param filename   The absolute path of the symlink within the docker image
		 * @param basePath   The base path that is being copied, and the symlink is
		 *                   relative to
		 * @param hostFolder The folder the basepath is copied to on the host
		 * @param te         The tar-archive entry that is a symlink
		 */
		public SymLink(final Path filename, final Path basePath, Path hostFolder, final TarArchiveEntry te) {
			assert te.isSymbolicLink() || te.isLink() : "This must only be called with symbolic links"; //$NON-NLS-1$

			m_isDirectory = te.isDirectory();
			this.m_basePath = basePath;
			this.m_filename = filename;
			this.m_hostBasePath = hostFolder;

			if (te.isLink()) { // Links are relative to the tar-root
				m_targetRel = (Path) basePath.append(new Path(te.getLinkName()).removeFirstSegments(1));
				m_targetAbs = m_targetRel;
				m_isAbs = true;
				return;
			}
			m_targetRel = new Path(te.getLinkName());
			IPath dltf = m_targetRel;
			m_isAbs = dltf.isAbsolute();
			if (!m_isAbs) {
				dltf = basePath;
				dltf = dltf.append(new Path(te.getName()).removeFirstSegments(1));
				dltf = dltf.removeLastSegments(1);
				dltf = dltf.append(te.getLinkName());
			}
			m_targetAbs = (Path) dltf;

		}

		/**
		 * The path of the symlink on the host
		 *
		 * @return the absolute host path
		 */
		public Path hostPath() {
			return toHost(m_filename, m_basePath, m_hostBasePath);
		}

		/**
		 * Get the absolute path of the target on the host system
		 *
		 * @return path on the host system
		 */
		public Path hostTarget() {
			return toHost(m_targetAbs, m_basePath, m_hostBasePath);
		}

		/**
		 * Check whether the target of the symlink is within the tar being copied
		 *
		 * @return true if the target is within the tar
		 */
		public boolean targetInBase() {
			return isInCopyPath(m_targetAbs, m_basePath);
		}

		/**
		 * Check whether the link target exists on the host
		 *
		 * @return true if the target exists on the host
		 */
		public boolean targetExists() {
			return hostTarget().toFile().exists();
		}

		/**
		 * Create the symlink on the host system The target must exist before the
		 * symlink can be created. This is a requirement for Windows/NTFS
		 *
		 * @return false if the target of the symlink does not exist.
		 * @throws IOException
		 */
		public boolean create() throws IOException {
			// The path in the docker

			if (!targetExists()) {
				return false;
			}

			try {
				if (m_isAbs) {
					Files.createSymbolicLink(hostPath().toFile().toPath(), hostTarget().toFile().toPath());
				} else {
					Files.createSymbolicLink(hostPath().toFile().toPath(), m_targetRel.toFile().toPath());
				}
			} catch (java.nio.file.FileAlreadyExistsException e) {
				// Just skip
			} catch (FileSystemException e) {
				String msg = JobMessages.getFormattedString("CopyFromDockerJob.create.failed.symlink", //$NON-NLS-1$
						m_filename.toFile().getAbsolutePath());
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					msg += '\n'; // $NON-NLS-1$
					msg += JobMessages.getString("CopyFromDockerJob.symlink.windows.permissions"); //$NON-NLS-1$
				}
				Activator.logErrorMessage(msg, e);
			}
			return true;
		}
	}

	/**
	 * Map the symlink-directory-structure to the host.
	 *
	 * When creating a tar, the symlinks are followed by docker. Thus in mirror-mode
	 * all links and the necessary folders must be recreated. Otherwise there are
	 * likely to be side effects due to recursions.
	 *
	 * Note: The performance of the current implementation is not the best and could
	 * improved by using readlink.
	 *
	 * @param containerId The container-id to get the path from
	 * @param path        The absolute path in the docker image
	 * @param hostDir     The host dir to map the root of the docker image to
	 * @return The final path on the host
	 * @throws DockerException This is thrown if something goes wrong with the
	 *                         docker
	 * @throws IOException
	 */
	Path mkdirSyms(final String containerId, final Path path, final Path hostDir) throws DockerException, IOException {

		assert m_mirror;

		// TODO: Handle path not a dir
		for (int i = 0; i < path.segmentCount(); i++) {
			// Create Dir - TODO Do chmod?
			IPath curpath = path.uptoSegment(i);

			// Get listing
			List<ContainerFileProxy> dir = m_connection.readContainerDirectory(containerId, curpath.toString());
			if (dir.isEmpty()) {
				Activator.logWarningMessage(MessageFormat.format("Could not get dirlist of {0}", curpath.toString())); //$NON-NLS-1$
			}
			String nextp = path.segments()[i];

			// Get next element
			ContainerFileProxy e = dir.stream().filter(ent -> nextp.equals(ent.getName())).findAny().orElse(null);
			if (e == null) {
				Activator.logWarningMessage(JobMessages.getFormattedString("CopyFromDockerJob.docker.failed.find.file", //$NON-NLS-1$
						new String[] { nextp, curpath.toString(), path.toString() }));
				return null;
			}

			// If the entry is not a symlink create the path and continue with the next
			// segment.
			if (!e.isLink()) {
				toHost(path.uptoSegment(i + 1), Path.ROOT, hostDir).toFile().mkdir();
				continue;
			}

			Path linkTarget = e.getLink().startsWith("/") ? new Path(e.getLink()) : (Path) curpath.append(e.getLink()); //$NON-NLS-1$

			SymLink sl = new SymLink((Path) (path.uptoSegment(i + 1)), linkTarget, hostDir, e.isFolder());

			Path rv = (Path) linkTarget.append(path.removeFirstSegments(i + 1));

			// If the target of the link does not exist is must be created, before creating
			// the symlink
			if (!sl.targetExists()) {
				rv = mkdirSyms(containerId, rv, hostDir);
			}

			if (rv != null) {
				sl.create();
			}
			return rv;
		}

		return path;
	}

	/**
	 * Copy a Tar-Stream to the host
	 *
	 * If there was an error creating a file or folder the operation will create an
	 * log entry, try to continue and return false.
	 *
	 * @param tarPath        The path the tar was created from
	 * @param hostpath       The path the tar should be extracted to
	 * @param tarStream      The tar-input-stream
	 * @param symlinkBacklog The backlog of symlinks, that can only be created after
	 *                       the tar was extracted.
	 * @param monitor        The progress-monitor
	 * @return false if there was an error creating a file or folder
	 * @throws IOException If something went wrong creating files or directories
	 */
	private boolean copyTar(Path tarPath, Path hostpath, InputStream tarStream, Set<SymLink> symlinkBacklog,
			final IProgressMonitor monitor) throws IOException {

		{
			File targetdir = toHost(tarPath, tarPath, hostpath).removeLastSegments(1).toFile();
			if (!targetdir.exists() && !targetdir.mkdirs()) {
				throw new IOException(JobMessages.getFormattedString("CopyFromDockerJob.create.failed.dir", //$NON-NLS-1$
						toHost(tarPath, tarPath, hostpath).toString()));
			}
		}

		boolean successful = true;
		/*
		 * The input stream from copyContainer might be incomplete or non-blocking so we
		 * should wrap it in a stream that is guaranteed to block until data is
		 * available.
		 */
		// TODO Root
		try (TarArchiveInputStream k = new TarArchiveInputStream(new BlockingInputStream(tarStream))) {
			TarArchiveEntry te = null;

			while ((te = k.getNextTarEntry()) != null) {

				Path copypath = new Path(te.getName());
				Path path = (Path) tarPath.append(copypath.removeFirstSegments(1));
				File f = toHost(path, tarPath, hostpath).toFile();
				int mode = te.getMode();

				// isDirectory checks whether the entry has a trailing slash.
				// Thus all other possible checks must come first...

				String basemsg = JobMessages.getString("CopyFromDockerJob.tar.copy"); //$NON-NLS-1$

				if (te.isCharacterDevice()) {
					Activator.logWarningMessage(basemsg
							+ MessageFormat.format(JobMessages.getString("CopyFromDockerJob.tar.copy.no.chardev"), //$NON-NLS-1$
									tarPath.append(te.getName()).toString()));
					successful = false;
					continue;
				}
				if (te.isBlockDevice()) {
					Activator.logWarningMessage(basemsg
							+ MessageFormat.format(JobMessages.getString("CopyFromDockerJob.tar.copy.no.blockdev"), //$NON-NLS-1$
									tarPath.append(te.getName()).toString()));
					successful = false;
					continue;
				}
				if (te.isFIFO()) {
					Activator.logWarningMessage(
							basemsg + JobMessages.getFormattedString("CopyFromDockerJob.tar.copy.no.fifo", //$NON-NLS-1$
									tarPath.append(te.getName()).toString()));
					successful = false;
					continue;
				}

				if (te.isSymbolicLink() || te.isLink()) {
					if (te.isLink()) {
						Activator.logWarningMessage(basemsg + MessageFormat
								.format(JobMessages.getString("CopyFromDockerJob.create.failed.link.symlink"), //$NON-NLS-1$
										path.toString(), tarPath.append(te.getLinkName()).removeFirstSegments(1))
								.toString());
					}

					SymLink sl = new SymLink(path, tarPath, hostpath, te);
					// Only create if the target does not exit this tar.
					// If it can not be created, put it to the backlog.
					// The backlog will be handled later
					if (!(sl.targetInBase() && sl.create())) {
						symlinkBacklog.add(sl);
					}
					continue;
				}

				if (te.isDirectory()) {
					if (f.exists()) {
						if (!f.isDirectory()) {
							Activator.logWarningMessage(
									JobMessages.getFormattedString("CopyFromDockerJob.create.failed.dir.already.exists", //$NON-NLS-1$
											f.getAbsolutePath()));
							successful = false;
						}
						continue;

					}
					if (!f.mkdir()) {
						Activator
								.logWarningMessage(JobMessages.getFormattedString("CopyFromDockerJob.create.failed.dir", //$NON-NLS-1$
										f.getAbsolutePath()));
						successful = false;
						continue;
					}
					if (!isWin && !te.isSymbolicLink()) {
						Files.setPosixFilePermissions(f.toPath(), toPerms(mode));
					}
					continue;
				}

				if (te.isFile()) {
					if (f.exists()) {
						// This might have been previously been copied from a sub folder of ours
						if (checkWasCopied(path))
							continue;

						if (f.isDirectory()) {
							Activator.logWarningMessage(
									JobMessages.getFormattedString("CopyFromDockerJob.create.failed.file.dir.exists", //$NON-NLS-1$
											f.getAbsolutePath()));
							successful = false;
							continue;
						} else {
							// This can happen if a previous copy operation failed.
							Activator.logWarningMessage(MessageFormat.format(
									JobMessages.getString("CopyFromDockerJob.create.failed.file.overwritten"), //$NON-NLS-1$
									f.getAbsolutePath()));

						}
					} else {
						try {
							if (!f.createNewFile()) {
								Activator.logWarningMessage(
										JobMessages.getFormattedString("CopyFromDockerJob.create.failed", //$NON-NLS-1$
												f.getAbsolutePath()));
								successful = false;
								continue;
							}
						} catch (IOException e) {
							Activator.logErrorMessage(JobMessages.getFormattedString("CopyFromDockerJob.create.failed", //$NON-NLS-1$
									f.getAbsolutePath()), e);
							successful = false;
							continue;
						}
					}

					if (!isWin) { // && !te.isSymbolicLink()
						Files.setPosixFilePermissions(f.toPath(), toPerms(mode));
					}

					try (FileOutputStream os = new FileOutputStream(f)) {
						byte[] barray = new byte[4096];
						int result = -1;
						while ((result = k.read(barray, 0, barray.length)) > -1) {
							if (monitor.isCanceled()) {
								throw new RuntimeException(JobMessages.getString("CopyFromDockerJob.cancel.user")); //$NON-NLS-1$
							}
							os.write(barray, 0, result);
						}
					}
					continue;
				}
				// None of the previous checks matched
				throw new RuntimeException("Unsupported Tar file entry that should not exist"); //$NON-NLS-1$
			} // Tar-entry-loop
		} // Try
		return successful;
	}

	/**
	 * Get the current image id
	 *
	 * @return The id as string
	 */
	private String getImageId() {
		if (m_imageId != null) {
			return m_imageId;
		}

		if (m_containerId != null) {
			IDockerContainerInfo containerinfo = m_connection.getContainerInfo(m_containerId);
			m_imageId = containerinfo.image();

		} else if (m_image != null) {
			var dockerImage = m_connection.getImageByTag(m_image);

			// Fall back to short-id
			if (dockerImage == null) {
				var images = m_connection.getImages();
				var oImage = images.stream().filter(f -> ((DockerImage) f).shortId().equals(m_image)).findFirst();
				if (oImage.isPresent()) {
					dockerImage = oImage.get();
				}
			}

			if (dockerImage == null) {
				throw new RuntimeException(
						JobMessages.getFormattedString("CopyFromDockerJob.docker.no.image", m_image)); //$NON-NLS-1$
			}
			m_imageId = dockerImage.id();
		} else {
			assert false : "Neither container nor image set. Broken init?"; //$NON-NLS-1$
		}
		return m_imageId;
	}

	/**
	 * Get a reference to the set of paths that are already mirrored to a targetPath
	 *
	 * This can only be used in mirror mode
	 *
	 * @param targetPath The path to get the set for
	 * @return The list of paths mirrored to that folder
	 */
	private static HashSet<Path> getCopiedSet(Path targetPath) {
		synchronized (m_lockObjectManagedInit) {
			HashSet<Path> copiedList = m_pathToCopiedList.get(targetPath);
			if (copiedList == null) {
				File dirFile = targetPath.append(COPY_STATE_FILE).toFile();

				if (dirFile.exists()) {
					try (FileInputStream f = new FileInputStream(dirFile)) {
						try (ObjectInputStream ois = new ObjectInputStream(f)) {
							@SuppressWarnings("unchecked")
							HashSet<String> temp = (HashSet<String>) ois.readObject();
							copiedList = temp.stream().map(x -> new Path(x))
									.collect(Collectors.toCollection(HashSet::new));
							m_pathToCopiedList.put(targetPath, copiedList);
						} catch (ClassNotFoundException | FileNotFoundException e) {
							// should never happen so print stack trace
							e.printStackTrace();
						}
					} catch (IOException e) {
						// will handle this below
					}
				}
			}
			if (copiedList == null) {
				copiedList = new HashSet<>(0);
				m_pathToCopiedList.put(targetPath, copiedList);
			}
			return copiedList;
		}

	}

	private void initmirror() throws FileNotFoundException, IOException, InterruptedException {

		assert m_mirror;

		String dockerImageId = getImageId();

		// TODO call in Constructor

		// if there is a .image_id file, check the image id to ensure
		// the user hasn't loaded a new version which may have
		// different header files installed.
		File imageFile = m_targetfolder.append(IMAGE_ID_FILE).toFile();

		// This must all happen under Lock
		synchronized (m_lockObjectManagedInit) {
			// pathToImageIdMap is only changed under lockObjectManagedInit, so accessing
			// without lockObject is fine
			String imageId = m_targetpathToImageIdMap.getOrDefault(m_targetfolder, ""); //$NON-NLS-1$

			// No need to lock m_lockObject, as long m_copyingMap is not accessed
			m_copyingMap = m_targetpathToCopyingMap.get(m_targetfolder);
			if (m_copyingMap == null) {
				m_copyingMap = new HashMap<>();
				m_targetpathToCopyingMap.put(m_targetfolder, m_copyingMap);
			}

			m_copiedList = getCopiedSet(m_targetfolder);

			// Read from file if possible
			if (imageId.isEmpty() && imageFile.exists()) {
				try (BufferedReader bufferReader = new BufferedReader(new FileReader(imageFile));) {
					imageId = bufferReader.readLine();
				}
			}

			if (!dockerImageId.equals(imageId)) {
				// if image id has changed...all bets are off
				// and we must reload all directories

				// Check if someone else is copying - otherwise wait.
				while (true) {
					Job j = null;
					synchronized (m_lockObject) {
						if (m_copyingMap.isEmpty()) {
							m_copiedList.clear();
							break;
						}
						j = m_copyingMap.values().iterator().next();
					}
					m_monitor.subTask(JobMessages.getString("CopyFromDockerJob.waiting.for.job")); //$NON-NLS-1$
					j.join(0, m_monitor);

				}
				// Delete old files
				if (m_targetfolder.toFile().exists()) {
					var paths = Files.walk(m_targetfolder.toFile().toPath()).collect(Collectors.toList());
					Collections.reverse(paths);
					paths.stream().forEach(p -> p.toFile().delete());
				}

				imageFile.getParentFile().mkdirs();
				try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(imageFile));) {
					bufferedWriter.write(dockerImageId);
					bufferedWriter.newLine();
				}
			}
			m_targetpathToImageIdMap.put(m_targetfolder, dockerImageId);
		}
	}

	/**
	 * Write the state of the mirror to the disk to be loaded when eclipse is
	 * started the next time.
	 */
	private void saveMirrorState() {
		assert m_mirror;

		if (m_copiedList != null) {
			synchronized (m_lockObject) {
				File dirFile = m_targetfolder.append(COPY_STATE_FILE).toFile();
				Set<String> writelist = m_copiedList.stream().map(x -> x.toString()).collect(Collectors.toSet());
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirFile))) {
					oos.writeObject(writelist);
				} catch (IOException e) {
					Activator.logErrorMessage(JobMessages.getString("CopyFromDockerJob.failed.save.state"), e); //$NON-NLS-1$
				}
			}
		}

	}

	/**
	 * Check if a path was already copied to the host
	 *
	 * @param path The path to check
	 * @return true if it was already copied
	 */
	private boolean checkIfAlreadyCopied(Path path) {

		assert m_mirror;

		synchronized (m_lockObjectManagedInit) {
			String dockerImageId = getImageId();
			assert dockerImageId.equals(m_targetpathToImageIdMap.get(m_targetfolder)):"Mirroring multiple Images to the same folder is not possible"; //$NON-NLS-1$
		}
		synchronized (m_lockObject) {
			return m_copiedList.stream().anyMatch(other -> other.isPrefixOf(path));
		}
	}

	/**
	 * Check if a sourcePath is currently being or was copied
	 *
	 * This must only be used in mirror-mode
	 *
	 * @param sourcePath       The path to check
	 * @param addToCopyingList Add to the list of paths currently being copied, if
	 *                         this is not already the case
	 * @param wait             if the path is being copied, wait until the job is
	 *                         finished
	 * @return true if the path is currently being copied
	 * @throws OperationCanceledException
	 * @throws InterruptedException
	 */
	private boolean needsBeingCopied(Path sourcePath, boolean addToCopyingList, boolean wait)
			throws OperationCanceledException, InterruptedException {

		assert m_mirror;

		List<Job> jAbove = null;
		List<Job> jBelow = null;
		boolean rv;

		synchronized (m_lockObject) {
			// Check if a super-folder was already copied
			boolean wascopied = m_copiedList.stream().anyMatch(p -> p.isPrefixOf(sourcePath));

			// Return directly as everything is already there
			// But we might have to wait for y job running in a subfolder
			if (wascopied && !wait) {
				return false;
			}

			// Check if another job copies something including sourcePath - this may also be
			// sourcePath
			jAbove = m_copyingMap.entrySet().stream().filter(other -> other.getKey().isPrefixOf(sourcePath))
					.map(Map.Entry::getValue).collect(Collectors.toList());

			// Check if some other job copies something sourcePath includes - this may also
			// be sourcePath
			jBelow = m_copyingMap.entrySet().stream().filter(other -> sourcePath.isPrefixOf(other.getKey()))
					.map(Map.Entry::getValue).collect(Collectors.toList());


			if (jAbove.isEmpty() && !wascopied) {
				if (addToCopyingList)
					m_copyingMap.put(sourcePath, this);
				rv = true;
			} else {
				rv = false;
			}
		}

		if (wait) {
			// The same job can be in Above and in Below - if it is us - but that shouldn't
			// be an issue
			for (var j : jAbove)
				j.join(0, m_monitor);
			for (var j : jBelow)
				j.join(0, m_monitor);
		}
		return rv;
	}

	/**
	 * Check if a sourcePath has already been copied
	 *
	 * This must only be used in mirror-mode
	 *
	 * @param sourcePath The path to check
	 * @return true if the path has previously been copied
	 * @throws OperationCanceledException
	 * @throws InterruptedException
	 */
	private boolean checkWasCopied(Path sourcePath) {

		assert m_mirror;

		synchronized (m_lockObject) {
			return m_copiedList.stream().anyMatch(p -> p.isPrefixOf(sourcePath));
		}
	}

	/**
	 * Move a path from the list of paths that are being copied to the list of
	 * copied paths
	 *
	 * @param sourcePath The path that was successfully copied to the host
	 */
	private void copySuccess(Path sourcePath) {
		assert m_mirror;

		synchronized (m_lockObject) {
			final var j = m_copyingMap.remove(sourcePath);
			assert  j == this : "Sychronisation is broken"; //$NON-NLS-1$ (safety)
			m_copiedList.add(sourcePath);
		}
	}

	/**
	 * Remove the path from the list of paths being copied
	 *
	 * @param sourcePath the path to remove
	 */
	private void copyFailed(Path sourcePath) {
		assert m_mirror;

		synchronized (m_lockObject) {
			Job j = m_copyingMap.remove(sourcePath);
			assert j == this : "Sychronisation is broken"; //$NON-NLS-1$
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		this.m_monitor = monitor;
		// The behavior of mirroring and Copying differ too strong, thus splitting up in
		// two functions.
		if (m_mirror) {
			return runMirror();
		} else {
			return runCopy();
		}
	}

	/**
	 * Copy in mirror mode. This will recreate the folder structure recreating
	 * symlinks
	 *
	 * @return Ok, or Cancel if the job was canceled by the user. If something went
	 *         wrong Error will be returned.
	 */
	private IStatus runMirror() {

		Path currentVolume = null;

		// Windows can only create symlinks to existing files, thus we need a backlog
		Set<SymLink> symlinkBacklog = new HashSet<>();

		try {
			initmirror();

			// Check if the volumes have already been copied

			HashMap<Path, Path> filtered_volumes = new HashMap<>();
			for (Entry<Path, Path> p : m_pathsToCopy.entrySet()) {
				if (!checkIfAlreadyCopied(p.getKey())) {
					filtered_volumes.put(p.getKey(), p.getValue());
				} else {
					m_monitor.worked(1);
				}
			}
			m_pathsToCopy = filtered_volumes;

		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (Exception e) {
			Activator.logErrorMessage("Failed to initialize mirror", e); //$NON-NLS-1$
			return Status.error("Failed to initialize", e); //$NON-NLS-1$
		}

		if (m_pathsToCopy.isEmpty()) {
			m_monitor.done();
			return Status.OK_STATUS;
		}

		if (m_image != null) {
			m_monitor.beginTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyImage", m_image), //$NON-NLS-1$
					m_pathsToCopy.size() + 2);
		} else {
			m_monitor.beginTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyContainer", m_image), //$NON-NLS-1$
					m_pathsToCopy.size() + 2);
		}

		try (final CloseableContainer container = (m_containerId == null ? new CloseableContainer(m_connection, m_image)
				: null); Closeable dcToken = m_connection.getOperationToken()) {

			if (container != null) {
				m_containerId = container.containerId;
				container.start();
			}

			// copy each volume if it exists and is not copied over yet
			for (Entry<Path, Path> volume : m_pathsToCopy.entrySet()) {

				Path srcdir = volume.getKey();
				Path hostDir = volume.getValue();

				currentVolume = srcdir;

				m_monitor.worked(1);
				// TODO better message
				m_monitor
						.subTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyPath", srcdir.toString())); //$NON-NLS-1$

				if (m_monitor.isCanceled()) {
					m_monitor.done();
					return Status.CANCEL_STATUS;
				}

				if (!needsBeingCopied(srcdir, true, false)) {
					continue;
				}
				Path realDir = mkdirSyms(m_containerId, srcdir, hostDir);
				if (realDir == null) {
					continue;
				}
				if (!srcdir.equals(realDir)) {
					copySuccess(srcdir);
					srcdir = realDir;
					// The realdir might be already copied.
					if (!needsBeingCopied(srcdir, true, false))
						continue;
				}

				InputStream in = m_connection.copyContainer(dcToken, m_containerId, srcdir.toString());
				if (copyTar(srcdir, hostDir, in, symlinkBacklog, m_monitor)) {
					copySuccess(srcdir);
				} else {
					copyFailed(srcdir);
				}
			}

			// After everything has been copied the symlinks can be created
			for (SymLink sl : symlinkBacklog) {
				if (needsBeingCopied(sl.m_targetAbs, false, true)) {
					Path get = sl.m_targetAbs;
					if (!sl.m_isDirectory) {
						get = (Path) get.removeLastSegments(1);
					}
					Set<Path> dir = new HashSet<>();
					dir.add(get);

					CopyFromDockerJob job = new CopyFromDockerJob(m_connection, CopyType.ContainerMirror, m_containerId,
							dir, m_targetfolder);
					job.schedule();
					try {
						job.join(0, m_monitor);
						if (!job.getResult().isOK()) {
							Activator.logWarningMessage(JobMessages
									.getFormattedString("CopyFromDockerJob.create.failed.symlink.get", dir.toString())); //$NON-NLS-1$
							continue;
						}
					} catch (Exception e) {
						Activator.logErrorMessage(JobMessages
								.getFormattedString("CopyFromDockerJob.create.failed.symlink.get", dir.toString()), e); //$NON-NLS-1$
						continue;
					}
				}

				if (!sl.create()) {
					Activator.logWarningMessage(JobMessages.getFormattedString("CopyFromDockerJob.docker.failed.get", //$NON-NLS-1$
							sl.m_targetAbs.toString()));
				}
			}
			saveMirrorState();
		} catch (OperationCanceledException e) {
			copyFailed(currentVolume);
			return Status.CANCEL_STATUS;
		} catch (DockerException | InterruptedException e) {
			// No, need to translate - the relevant message is extracted from the docker-lib
			Activator.logErrorMessage(MessageFormat.format("Docker Connection Error: {0}", e.getMessage()), e); //$NON-NLS-1$
			copyFailed(currentVolume);
		} catch (Exception e) {
			Activator.logErrorMessage(JobMessages.getFormattedString("CopyFromDockerJob.copy.failed", //$NON-NLS-1$
					new String[] { currentVolume.toString(), m_image }), e);
			copyFailed(currentVolume);
		} finally {
			m_monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Copy a folders or files from the image to the host. If symlinks point outside
	 * the source folder, they are resolved and the actual files are copied up to a
	 * depth of MAXLINKDEPTH.
	 *
	 * @return Ok, or Cancel if the job was canceled by the user. If something went
	 *         wrong Error will be returned.
	 */
	private IStatus runCopy() {

		Path currentVolume = null;

		if (m_image != null) {
			m_monitor.beginTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyImage", m_image), //$NON-NLS-1$
					m_pathsToCopy.size() + 2);
		} else {
			m_monitor.beginTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyContainer", m_image), //$NON-NLS-1$
					m_pathsToCopy.size() + 2);
		}

		try (final CloseableContainer container = (m_containerId == null ? new CloseableContainer(m_connection, m_image)
				: null); Closeable dcToken = m_connection.getOperationToken()) {

			if (container != null) {
				m_containerId = container.containerId;
				container.start();
			}

			// copy each volume if it exists and is not copied over yet
			for (Entry<Path, Path> pathToCopy : m_pathsToCopy.entrySet()) {

				Path srcdir = pathToCopy.getKey();
				Path hostDir = pathToCopy.getValue();

				currentVolume = srcdir;

				m_monitor.worked(1);
				// TODO better message
				copyFolderFromContainer(dcToken, srcdir, (Path) hostDir.append(srcdir.lastSegment()), 0);
			}
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (DockerException | InterruptedException e) {
			Activator.logErrorMessage(MessageFormat.format("Docker Connection Error: {0}", e.getMessage()), e); //$NON-NLS-1$
		} catch (Exception e) {
			Activator.logErrorMessage(JobMessages.getFormattedString("CopyFromDockerJob.copy.failed", //$NON-NLS-1$
					new String[] { currentVolume.toString(), m_image }), e);
		} finally {
			m_monitor.done();
		}
		return Status.OK_STATUS;
	}

	/**
	 * Copy a folder recursively following symlinks
	 *
	 * @param dcToken   The closable docker connection Token
	 * @param srcdir    The path in the container
	 * @param hostDir   The path on the host
	 * @param linkdepth The current link depth
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void copyFolderFromContainer(Closeable dcToken, Path srcdir, Path hostDir, int linkdepth)
			throws DockerException, InterruptedException, IOException {

		assert !m_mirror;

		if (m_monitor.isCanceled()) {
			return;
		}

		m_monitor.subTask(JobMessages.getFormattedString("CopyFromDockerJob.title.copyPath", srcdir.toString())); //$NON-NLS-1$

		Set<SymLink> symlinkBacklog = new HashSet<>();

		try (InputStream in = m_connection.copyContainer(dcToken, m_containerId, srcdir.toString())) {
			copyTar(srcdir, hostDir, in, symlinkBacklog, m_monitor);
		}

		for (SymLink sl : symlinkBacklog) {
			if (!sl.targetInBase()) {
				// The link goes outside what was just copied
				if (linkdepth < MAXLINKDEPTH) {
					copyFolderFromContainer(dcToken, sl.m_targetAbs, sl.hostPath(), linkdepth + 1);
				} else {
					Activator.logWarningMessage(JobMessages.getFormattedString("CopyFromDockerJob.link.depth", //$NON-NLS-1$
							new String[] { "" + MAXLINKDEPTH, sl.m_filename.toString() })); //$NON-NLS-1$
				}
			} else {
				// If creating a symlink fails, there is nothing we can do - besides copying the
				// file, but that can be implemented in future versions
				sl.create();
			}

		}
	}
}
