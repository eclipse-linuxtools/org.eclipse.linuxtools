# Eclipse Linux Tools – Copilot Instructions

## Build and Test

```bash
mvn clean verify
```

Tests require a virtual display (Xvnc) because they run in a UI harness. On CI this is handled automatically. Locally, use `Xvfb` or skip UI tests with `-DskipTests`.

## Architecture

This is a **multi-module Eclipse/OSGi plugin project** built with **Maven + Tycho**. It is not a standard Java application — every plugin is an OSGi bundle declared in `META-INF/MANIFEST.MF`, not just a Maven artifact.

### Top-level modules

| Directory     | Content |
|---------------|---------|
| `containers/` | Docker integration (UI, core, Dockerfile LS, tests) |
| `rpm/`        | RPM `.spec` editor, rpmlint integration, RPM core |
| `profiling/`  | Shared profiling launch framework, data viewers, remote/SSH proxy |
| `gcov/`       | GCov coverage analysis |
| `gprof/`      | GProf call-graph profiling |
| `valgrind/`   | Valgrind heap analysis |
| `perf/`       | Linux `perf` call profiling |
| `systemtap/`  | SystemTap IDE integration |
| `changelog/`  | ChangeLog editor/formatter |
| `libhover/`   | C library hover documentation |
| `lttng/`      | LTTng tracing support |
| `vagrant/`    | Vagrant VM integration |
| `releng/`     | Update sites, target platform, release engineering |

Each top-level directory is itself a Maven aggregator `pom.xml` listing its plugins, features, docs, and test plugins as `<module>` entries.

### Target platform

All dependencies are defined in:
```
releng/org.eclipse.linuxtools.target/linuxtools-latest.target
```
This file is auto-updated daily against the latest Eclipse Orbit and SimRel repositories. Do **not** hand-edit dependency versions for Eclipse platform dependencies in `pom.xml` — add them to the `.target` file instead.

### Plugin naming conventions

- **Core logic:** `org.eclipse.linuxtools.<component>.core`
- **UI:** `org.eclipse.linuxtools.<component>.ui`
- **Tests:** `org.eclipse.linuxtools.<component>.*.tests` — packaged as `eclipse-test-plugin`
- **Features:** `org.eclipse.linuxtools.<component>-feature` — packaged as `eclipse-feature`
- **Internal (non-API) packages:** `org.eclipse.linuxtools.internal.<component>.*`

Public API lives in `org.eclipse.linuxtools.<component>.*`; implementation details live under `org.eclipse.linuxtools.internal.<component>.*`.

### Two separate update sites

The `releng/` module produces two p2 repositories:
- `org.eclipse.linuxtools.releng-site` — all tools except containers
- `org.eclipse.linuxtools.docker-site` — Docker/container tools only

## Key Conventions

### Java and OSGi
- **Java 21** is required (`Bundle-RequiredExecutionEnvironment: JavaSE-21` in all manifests).
- Plugin dependencies are declared in `META-INF/MANIFEST.MF` via `Require-Bundle` or `Import-Package`, not Maven `<dependency>`. Tycho resolves the classpath from these.
- Each plugin `pom.xml` contains almost nothing — just the parent reference and `<packaging>eclipse-plugin</packaging>`.

### Tests
- Tests use **JUnit 5 (Jupiter)**: `org.junit.jupiter.api.Assertions`, `@Test`, `@BeforeEach`, `@AfterEach`.
- Test plugins run inside a live Eclipse workbench (`useUIHarness=true`, `useUIThread=true`). Tests can use Eclipse workspace and UI APIs directly.

### Copyright header
Every Java file must start with:
```java
/*******************************************************************************
 * Copyright (c) <year> <Author> and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    <Author> - initial API and implementation
 *******************************************************************************/
```

### Commits
Non-committers must add a `Signed-off-by` trailer and have a signed [Eclipse Contributor Agreement (ECA)](http://www.eclipse.org/legal/ECA.php) on file.

### Release branches
Stable branches are named `stable-<x>` (e.g., `stable-8.5`). The `master` branch always targets the next SNAPSHOT version.
