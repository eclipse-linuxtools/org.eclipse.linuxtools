/*******************************************************************************
 * Copyright (c) 2022, 2023 Mathema and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.testCategory.NativeLinuxDocker;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob;
import org.eclipse.linuxtools.internal.docker.ui.jobs.CopyFromDockerJob.CopyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ NativeLinuxDocker.class })
public class CopyFromDockerJobTest {

	static final String connectionUri = "unix:///var/run/docker.sock";
	static final ILog logger = Platform.getLog(CopyFromDockerJobTest.class);

	static final String dockerfileCopyTestEnv = "./projects/CopyTestEnv";
	String imgCopyTestEnv = null;

	static final String dockerfileCopyDeleteTest = "./projects/CopyDeleteTest";

	java.nio.file.Path localTempWork = null;
	DockerConnection connection = null;

	static String buildImage(String file) throws DockerException, InterruptedException {

		var dockerfileP = new Path(new java.io.File(file).getAbsolutePath());
		logger.info("Building: " + dockerfileP.toPortableString());
		Assertions.assertThat(dockerfileP.toFile().exists()).isTrue();
		Assertions.assertThat(dockerfileP.toFile().isDirectory()).isTrue();
		Assertions.assertThat(dockerfileP.append("Dockerfile").toFile().exists()).isTrue();
		Assertions.assertThat(dockerfileP.append("Dockerfile").toFile().isFile()).isTrue();

		var cnn = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
		Assertions.assertThat(cnn).isNotNull();

		var res = cnn.buildImage(dockerfileP, null, cnn.getDefaultBuildImageProgressHandler("Tmp", 0));
		logger.info("Built: " + res);
		Assertions.assertThat(res).isNotNull();
		return res;

	}

	@Before
	public void buildCopyTestEnv() throws DockerException, InterruptedException {
		imgCopyTestEnv = buildImage(dockerfileCopyTestEnv);
	}

	@Before
	public void createTemp() throws IOException {
		localTempWork = Files.createTempDirectory("eclipse-docker-");
		logger.info("Tempdir: " + localTempWork);
	}

	@Before
	public void setConnection() {
		connection = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
		Assertions.assertThat(connection).isNotNull();
	}

	@After
	public void cleanTemp() throws IOException {
		var paths = Files.walk(localTempWork).toList();
		// Must delete recursively
		Collections.reverse(paths);
		paths.stream().forEach(p -> p.toFile().delete());
	}

	private java.nio.file.Path stripTemp(java.nio.file.Path p) {
		if (p.isAbsolute())
			return localTempWork.relativize(p);
		return p;
	}

	private List<String> getTempLs() throws IOException {
		var paths = Files.walk(localTempWork).sorted().toList();

		var sep = File.separatorChar;

		return paths.stream().map(f -> {
			if (Files.isSymbolicLink(f)) {
				try {
					return "S " + sep + stripTemp(f) + " -> " + stripTemp(Files.readSymbolicLink(f));
				} catch (IOException e) {
					return "--ERROR--";
				}
			} else if (Files.isRegularFile(f, LinkOption.NOFOLLOW_LINKS)) {
				return "R " + sep + stripTemp(f);
			} else if (Files.isDirectory(f, LinkOption.NOFOLLOW_LINKS)) {
				return "D " + sep + stripTemp(f);
			} else {
				return "U " + sep + stripTemp(f);
			}

		}).map(f -> f.replace(sep, '/')).toList();

	}

	@SuppressWarnings("unused")
	private void printTempLS() throws IOException {
		getTempLs().forEach(s -> System.out.println('"' + s + "\","));
	}

	private void CopyFolder(String path, CopyType type) throws Exception, InterruptedException {
		CopyFolder(path, type, imgCopyTestEnv);
	}

	private void CopyFolder(String path, CopyType type, String image) throws Exception, InterruptedException {
		var fls = Set.of(new Path(path));

		final var j = new CopyFromDockerJob(connection, type, image, fls,
				new Path(localTempWork.toString()));

		j.schedule();
		if (!j.join(60000, null)) {
			j.cancel();
			Assertions.assertThat(j.join(10000, null)).isTrue();
		}
	}

	@Test
	public void FolderFromImage() throws Exception {
		CopyFolder("/test/a/b", CopyType.Image);

		// printTempLS();
		final var exp = List.of("D /", "D /b", "D /b/c", "R /b/c/h1.txt", "D /b/d", "R /b/d/h2.txt");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImage() throws Exception {
		CopyFolder("/test/a/b", CopyType.ImageMirror);
		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/a", "D /test/a/b",
				"D /test/a/b/c", "R /test/a/b/c/h1.txt", "D /test/a/b/d", "R /test/a/b/d/h2.txt");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void FolderFromImageSymlinks() throws Exception {
		CopyFolder("/test/sl", CopyType.Image);
		// printTempLS();
		final var exp = List.of("D /", "D /sl", "D /sl/link-c-abs", "R /sl/link-c-abs/h1.txt", "D /sl/link-d-rel",
				"R /sl/link-d-rel/h2.txt");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImageSymlinks() throws Exception {
		CopyFolder("/test/sl", CopyType.ImageMirror);
		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/a", "D /test/a/b",
				"D /test/a/b/c", "R /test/a/b/c/h1.txt", "D /test/a/b/d", "R /test/a/b/d/h2.txt", "D /test/sl",
				"S /test/sl/link-c-abs -> test/a/b/c", "S /test/sl/link-d-rel -> ../a/b/d");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void FolderFromImageRecurseSymlinks() throws Exception {
		CopyFolder("/test/sl-rec", CopyType.Image);

		// printTempLS();
		final var exp = List.of("D /", "D /sl-rec", "D /sl-rec/subdir", "S /sl-rec/subdir/link-up -> ..");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImageRecurseSymlinks() throws Exception {
		CopyFolder("/test/sl-rec", CopyType.ImageMirror);
		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/sl-rec",
				"D /test/sl-rec/subdir",
				"S /test/sl-rec/subdir/link-up -> ..");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void FolderFromImageSymlinksDepth() throws Exception {
		CopyFolder("/test/sl-depth/l0", CopyType.Image);

		// printTempLS();
		final var exp = List.of("D /", "D /l0", "R /l0/F0", "D /l0/L0", "R /l0/L0/F1", "D /l0/L0/L1", "R /l0/L0/L1/F2",
				"D /l0/L0/L1/L2", "R /l0/L0/L1/L2/F3", "D /l0/L0/L1/L2/L3", "R /l0/L0/L1/L2/L3/F4",
				"D /l0/L0/L1/L2/L3/L4", "R /l0/L0/L1/L2/L3/L4/F5", "D /l0/L0/L1/L2/L3/L4/L5",
				"R /l0/L0/L1/L2/L3/L4/L5/F6", "D /l0/L0/L1/L2/L3/L4/L5/L6", "R /l0/L0/L1/L2/L3/L4/L5/L6/F7",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7", "R /l0/L0/L1/L2/L3/L4/L5/L6/L7/F8", "D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/F9", "D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/F10", "D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/F11", "D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/F12", "D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/F13",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/F14",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/F15",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/F16",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/F17",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17/F18",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17/L18",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17/L18/F19",
				"D /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17/L18/L19",
				"R /l0/L0/L1/L2/L3/L4/L5/L6/L7/L8/L9/L10/L11/L12/L13/L14/L15/L16/L17/L18/L19/F20");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImageSymlinksDepth() throws Exception {
		CopyFolder("/test/sl-depth/l0", CopyType.ImageMirror);
		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/sl-depth",
				"D /test/sl-depth/l0", "R /test/sl-depth/l0/F0", "S /test/sl-depth/l0/L0 -> ../l1",
				"D /test/sl-depth/l1", "R /test/sl-depth/l1/F1", "S /test/sl-depth/l1/L1 -> ../l2",
				"D /test/sl-depth/l10", "R /test/sl-depth/l10/F10", "S /test/sl-depth/l10/L10 -> ../l11",
				"D /test/sl-depth/l11", "R /test/sl-depth/l11/F11", "S /test/sl-depth/l11/L11 -> ../l12",
				"D /test/sl-depth/l12", "R /test/sl-depth/l12/F12", "S /test/sl-depth/l12/L12 -> ../l13",
				"D /test/sl-depth/l13", "R /test/sl-depth/l13/F13", "S /test/sl-depth/l13/L13 -> ../l14",
				"D /test/sl-depth/l14", "R /test/sl-depth/l14/F14", "S /test/sl-depth/l14/L14 -> ../l15",
				"D /test/sl-depth/l15", "R /test/sl-depth/l15/F15", "S /test/sl-depth/l15/L15 -> ../l16",
				"D /test/sl-depth/l16", "R /test/sl-depth/l16/F16", "S /test/sl-depth/l16/L16 -> ../l17",
				"D /test/sl-depth/l17", "R /test/sl-depth/l17/F17", "S /test/sl-depth/l17/L17 -> ../l18",
				"D /test/sl-depth/l18", "R /test/sl-depth/l18/F18", "S /test/sl-depth/l18/L18 -> ../l19",
				"D /test/sl-depth/l19", "R /test/sl-depth/l19/F19", "S /test/sl-depth/l19/L19 -> ../l20",
				"D /test/sl-depth/l2", "R /test/sl-depth/l2/F2", "S /test/sl-depth/l2/L2 -> ../l3",
				"D /test/sl-depth/l20", "R /test/sl-depth/l20/F20", "S /test/sl-depth/l20/L20 -> ../l21",
				"D /test/sl-depth/l21", "R /test/sl-depth/l21/F21", "S /test/sl-depth/l21/L21 -> ../l22",
				"D /test/sl-depth/l22", "R /test/sl-depth/l22/F22", "S /test/sl-depth/l22/L22 -> ../l23",
				"D /test/sl-depth/l23", "R /test/sl-depth/l23/F23", "S /test/sl-depth/l23/L23 -> ../l24",
				"D /test/sl-depth/l24", "R /test/sl-depth/l24/F24", "S /test/sl-depth/l24/L24 -> ../l25",
				"D /test/sl-depth/l25", "R /test/sl-depth/l25/F25", "S /test/sl-depth/l25/L25 -> ../l26",
				"D /test/sl-depth/l26", "R /test/sl-depth/l26/F26", "S /test/sl-depth/l26/L26 -> ../l27",
				"D /test/sl-depth/l27", "R /test/sl-depth/l27/F27", "S /test/sl-depth/l27/L27 -> ../l28",
				"D /test/sl-depth/l28", "R /test/sl-depth/l28/F28", "S /test/sl-depth/l28/L28 -> ../l29",
				"D /test/sl-depth/l29", "R /test/sl-depth/l29/F29", "S /test/sl-depth/l29/L29 -> ../l30",
				"D /test/sl-depth/l3", "R /test/sl-depth/l3/F3", "S /test/sl-depth/l3/L3 -> ../l4",
				"D /test/sl-depth/l30", "R /test/sl-depth/l30/F30", "D /test/sl-depth/l4", "R /test/sl-depth/l4/F4",
				"S /test/sl-depth/l4/L4 -> ../l5", "D /test/sl-depth/l5", "R /test/sl-depth/l5/F5",
				"S /test/sl-depth/l5/L5 -> ../l6", "D /test/sl-depth/l6", "R /test/sl-depth/l6/F6",
				"S /test/sl-depth/l6/L6 -> ../l7", "D /test/sl-depth/l7", "R /test/sl-depth/l7/F7",
				"S /test/sl-depth/l7/L7 -> ../l8", "D /test/sl-depth/l8", "R /test/sl-depth/l8/F8",
				"S /test/sl-depth/l8/L8 -> ../l9", "D /test/sl-depth/l9", "R /test/sl-depth/l9/F9",
				"S /test/sl-depth/l9/L9 -> ../l10");
		// Windows sorts differently
		Assertions.assertThat(getTempLs()).containsExactlyInAnyOrderElementsOf(exp);
	}

	@Test
	public void FolderFromImageMany() throws Exception {
		CopyFolder("/test/manyfiles", CopyType.Image);

		// printTempLS();
		var exp = new Vector<String>();
		exp.add("D /");
		exp.add("D /manyfiles");
		var files = IntStream.range(1, 10001).mapToObj(n -> "/manyfiles/F" + n).sorted().map(f -> "R " + f)
				.toList();
		exp.addAll(files);
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImageMany() throws Exception {
		CopyFolder("/test/manyfiles", CopyType.ImageMirror);
		// printTempLS();
		var exp = new Vector<String>();
		exp.addAll(List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/manyfiles"));

		var files = IntStream.range(1, 10001).mapToObj(n -> "/test/manyfiles/F" + n).sorted().map(f -> "R " + f)
				.toList();
		exp.addAll(files);

		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Ignore
	@Test
	// This needs additional support
	public void FolderFromImageSymlinksBack() throws Exception {
		CopyFolder("/test/sl-back/l0", CopyType.Image);

		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/sl-back");
		Assertions.assertThat(getTempLs()).isEqualTo(exp);
	}

	@Test
	public void MirrorFromImageSymlinksBack() throws Exception {
		CopyFolder("/test/sl-back/l0", CopyType.ImageMirror);
		// printTempLS();
		final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/sl-back",
				"D /test/sl-back/l0", "R /test/sl-back/l0/F0", "S /test/sl-back/l0/L0 -> ../l1", "D /test/sl-back/l1",
				"R /test/sl-back/l1/F1", "S /test/sl-back/l1/L1 -> ../l2", "S /test/sl-back/l1/LB1 -> ../l0",
				"D /test/sl-back/l10", "R /test/sl-back/l10/F10", "S /test/sl-back/l10/L10 -> ../l11",
				"S /test/sl-back/l10/LB10 -> ../l9", "D /test/sl-back/l11", "R /test/sl-back/l11/F11",
				"S /test/sl-back/l11/L11 -> ../l12", "S /test/sl-back/l11/LB11 -> ../l10", "D /test/sl-back/l12",
				"R /test/sl-back/l12/F12", "S /test/sl-back/l12/L12 -> ../l13", "S /test/sl-back/l12/LB12 -> ../l11",
				"D /test/sl-back/l13", "R /test/sl-back/l13/F13", "S /test/sl-back/l13/L13 -> ../l14",
				"S /test/sl-back/l13/LB13 -> ../l12", "D /test/sl-back/l14", "R /test/sl-back/l14/F14",
				"S /test/sl-back/l14/L14 -> ../l15", "S /test/sl-back/l14/LB14 -> ../l13", "D /test/sl-back/l15",
				"R /test/sl-back/l15/F15", "S /test/sl-back/l15/L15 -> ../l16", "S /test/sl-back/l15/LB15 -> ../l14",
				"D /test/sl-back/l16", "R /test/sl-back/l16/F16", "S /test/sl-back/l16/L16 -> ../l17",
				"S /test/sl-back/l16/LB16 -> ../l15", "D /test/sl-back/l17", "R /test/sl-back/l17/F17",
				"S /test/sl-back/l17/L17 -> ../l18", "S /test/sl-back/l17/LB17 -> ../l16", "D /test/sl-back/l18",
				"R /test/sl-back/l18/F18", "S /test/sl-back/l18/L18 -> ../l19", "S /test/sl-back/l18/LB18 -> ../l17",
				"D /test/sl-back/l19", "R /test/sl-back/l19/F19", "S /test/sl-back/l19/L19 -> ../l20",
				"S /test/sl-back/l19/LB19 -> ../l18", "D /test/sl-back/l2", "R /test/sl-back/l2/F2",
				"S /test/sl-back/l2/L2 -> ../l3", "S /test/sl-back/l2/LB2 -> ../l1", "D /test/sl-back/l20",
				"R /test/sl-back/l20/F20", "S /test/sl-back/l20/L20 -> ../l21", "S /test/sl-back/l20/LB20 -> ../l19",
				"D /test/sl-back/l21", "R /test/sl-back/l21/F21", "S /test/sl-back/l21/L21 -> ../l22",
				"S /test/sl-back/l21/LB21 -> ../l20", "D /test/sl-back/l22", "R /test/sl-back/l22/F22",
				"S /test/sl-back/l22/L22 -> ../l23", "S /test/sl-back/l22/LB22 -> ../l21", "D /test/sl-back/l23",
				"R /test/sl-back/l23/F23", "S /test/sl-back/l23/L23 -> ../l24", "S /test/sl-back/l23/LB23 -> ../l22",
				"D /test/sl-back/l24", "R /test/sl-back/l24/F24", "S /test/sl-back/l24/L24 -> ../l25",
				"S /test/sl-back/l24/LB24 -> ../l23", "D /test/sl-back/l25", "R /test/sl-back/l25/F25",
				"S /test/sl-back/l25/L25 -> ../l26", "S /test/sl-back/l25/LB25 -> ../l24", "D /test/sl-back/l26",
				"R /test/sl-back/l26/F26", "S /test/sl-back/l26/L26 -> ../l27", "S /test/sl-back/l26/LB26 -> ../l25",
				"D /test/sl-back/l27", "R /test/sl-back/l27/F27", "S /test/sl-back/l27/L27 -> ../l28",
				"S /test/sl-back/l27/LB27 -> ../l26", "D /test/sl-back/l28", "R /test/sl-back/l28/F28",
				"S /test/sl-back/l28/L28 -> ../l29", "S /test/sl-back/l28/LB28 -> ../l27", "D /test/sl-back/l29",
				"R /test/sl-back/l29/F29", "S /test/sl-back/l29/L29 -> ../l30", "S /test/sl-back/l29/LB29 -> ../l28",
				"D /test/sl-back/l3", "R /test/sl-back/l3/F3", "S /test/sl-back/l3/L3 -> ../l4",
				"S /test/sl-back/l3/LB3 -> ../l2", "D /test/sl-back/l30", "R /test/sl-back/l30/F30",
				"S /test/sl-back/l30/LB30 -> ../l29", "D /test/sl-back/l4", "R /test/sl-back/l4/F4",
				"S /test/sl-back/l4/L4 -> ../l5", "S /test/sl-back/l4/LB4 -> ../l3", "D /test/sl-back/l5",
				"R /test/sl-back/l5/F5", "S /test/sl-back/l5/L5 -> ../l6", "S /test/sl-back/l5/LB5 -> ../l4",
				"D /test/sl-back/l6", "R /test/sl-back/l6/F6", "S /test/sl-back/l6/L6 -> ../l7",
				"S /test/sl-back/l6/LB6 -> ../l5", "D /test/sl-back/l7", "R /test/sl-back/l7/F7",
				"S /test/sl-back/l7/L7 -> ../l8", "S /test/sl-back/l7/LB7 -> ../l6", "D /test/sl-back/l8",
				"R /test/sl-back/l8/F8", "S /test/sl-back/l8/L8 -> ../l9", "S /test/sl-back/l8/LB8 -> ../l7",
				"D /test/sl-back/l9", "R /test/sl-back/l9/F9", "S /test/sl-back/l9/L9 -> ../l10",
				"S /test/sl-back/l9/LB9 -> ../l8");
		// Windows sorts differently
		Assertions.assertThat(getTempLs()).containsExactlyInAnyOrderElementsOf(exp);
	}

	/**
	 * When the image is updated the old files should be removed
	 */
	@Test
	public void MirrorDelete() throws Exception {
		{
			CopyFolder("/test/a/b", CopyType.ImageMirror);
			// printTempLS();
			final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/a", "D /test/a/b",
					"D /test/a/b/c", "R /test/a/b/c/h1.txt", "D /test/a/b/d", "R /test/a/b/d/h2.txt");
			Assertions.assertThat(getTempLs()).isEqualTo(exp);
		}
		// Same Docker image - files should be added
		{
			CopyFolder("/test/sl/", CopyType.ImageMirror);
			// printTempLS();
			final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/a", "D /test/a/b",
					"D /test/a/b/c", "R /test/a/b/c/h1.txt", "D /test/a/b/d", "R /test/a/b/d/h2.txt", "D /test/sl",
					"S /test/sl/link-c-abs -> test/a/b/c", "S /test/sl/link-d-rel -> ../a/b/d");
			Assertions.assertThat(getTempLs()).isEqualTo(exp);

		}
		// Different Docker image - fold files should be removed
		{
			var imgCopyDeleteTest = buildImage(dockerfileCopyDeleteTest);
			CopyFolder("/test", CopyType.ImageMirror, imgCopyDeleteTest);
			final var exp = List.of("D /", "R /.copyState", "R /.image_id", "D /test", "D /test/deletetest",
					"R /test/deletetest/hello1.txt", "R /test/deletetest/hello2.txt");
			Assertions.assertThat(getTempLs()).isEqualTo(exp);
		}
	}

}
