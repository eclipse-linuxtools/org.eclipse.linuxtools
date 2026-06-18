#!/bin/bash

# Final changes for Linux Tools release
# This script replicates commit e3f8e5b54e934abb52a306c746dc2e7be04e2ebf and
# is written by Claude AI and Jeff Johnston <jjohnstn@redhat.com>
#
# The script automatically discovers version numbers from pom.xml files:
#   - Main version from root pom.xml (e.g., 8.23.0-SNAPSHOT -> 8.23.0)
#   - Docker version from containers/pom.xml (e.g., 5.23.0-SNAPSHOT -> 5.23.0)
#
# Changes applied:
#   1. Updates parent versions from -SNAPSHOT to release versions in <parent> blocks
#   2. Updates p2.inf files to use stable update sites (updates-nightly -> update)
#   3. Adds <version>X.Y.Z-SNAPSHOT</version> elements to feature and plugin pom.xml files
#      immediately after <packaging> tags where not already present (keeping SNAPSHOT suffix)

set -e  # Exit on error

REPO_ROOT=`pwd`

# Discover version numbers from pom.xml files
echo "Discovering version numbers from pom.xml files..."

# Extract main version from root pom.xml (format: <version>8.23.0-SNAPSHOT</version>)
MAIN_VERSION_SNAPSHOT=$(grep -A 2 '<artifactId>linuxtools-parent</artifactId>' "$REPO_ROOT/pom.xml" | grep '<version>' | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
# Remove -SNAPSHOT suffix to get release version
MAIN_VERSION="${MAIN_VERSION_SNAPSHOT%-SNAPSHOT}"

# Extract docker version from containers/pom.xml (format: <version>5.23.0-SNAPSHOT</version>)
DOCKER_VERSION_SNAPSHOT=$(grep -A 2 '<artifactId>org.eclipse.linuxtools.docker</artifactId>' "$REPO_ROOT/containers/pom.xml" | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
# Remove -SNAPSHOT suffix to get release version
DOCKER_VERSION="${DOCKER_VERSION_SNAPSHOT%-SNAPSHOT}"

echo "  Main version: $MAIN_VERSION_SNAPSHOT -> $MAIN_VERSION"
echo "  Docker version: $DOCKER_VERSION_SNAPSHOT -> $DOCKER_VERSION"
echo ""

# Validate that versions were extracted
if [ -z "$MAIN_VERSION" ] || [ -z "$DOCKER_VERSION" ]; then
    echo "ERROR: Failed to extract version numbers from pom.xml files"
    echo "  Main version: '$MAIN_VERSION'"
    echo "  Docker version: '$DOCKER_VERSION'"
    exit 1
fi

echo "Starting final changes for Linux Tools $MAIN_VERSION release..."

# Function to update p2.inf files - change updates-nightly to update
update_p2_inf_linuxtools() {
    local file="$1"
    echo "Updating $file (updates-nightly -> update)"
    sed -i 's|updates-nightly|update|g' "$file"
}

# Function to update p2.inf files for docker - change updates-docker-nightly to update-docker
update_p2_inf_docker() {
    local file="$1"
    echo "Updating $file (updates-docker-nightly -> update-docker)"
    sed -i 's|updates-docker-nightly|update-docker|g' "$file"
}

# Function to update the version of the parent in main pom.xml
update_parent_version_top() {
    local file="$1"
    echo "Updating parent version in $file ($MAIN_VERSION_SNAPSHOT -> $MAIN_VERSION)"
    sed -i "/<version>/,/<\/version>/ s|$MAIN_VERSION_SNAPSHOT|$MAIN_VERSION|" "$file"
}

# Function to update parent version from SNAPSHOT to release version
update_parent_version_main() {
    local file="$1"
    echo "Updating parent version in $file ($MAIN_VERSION_SNAPSHOT -> $MAIN_VERSION)"
    sed -i "/<parent>/,/<\/parent>/ s|<version>$MAIN_VERSION_SNAPSHOT</version>|<version>$MAIN_VERSION</version>|" "$file"
}

# Function to update parent version for containers (from SNAPSHOT to release version)
update_parent_version_docker() {
    local file="$1"
    echo "Updating parent version in $file ($DOCKER_VERSION_SNAPSHOT -> $DOCKER_VERSION)"
    sed -i "/<parent>/,/<\/parent>/ s|<version>$DOCKER_VERSION_SNAPSHOT</version>|<version>$DOCKER_VERSION</version>|" "$file"
}

# Function to add version element after packaging in feature pom.xml files
# Adds the SNAPSHOT version (original version before release) as the artifact version
add_feature_version_main() {
    local file="$1"
    echo "Adding feature version element in $file (version: $MAIN_VERSION_SNAPSHOT)"
    # Check if version line already exists after packaging (not in parent block)
    # If packaging line exists but no version after it, add the SNAPSHOT version
    if grep -q '<packaging>eclipse-feature</packaging>' "$file"; then
        # Only add if there's no version line immediately after packaging
        if ! grep -A 1 '<packaging>eclipse-feature</packaging>' "$file" | grep -q '<version>'; then
            sed -i "/<packaging>eclipse-feature<\/packaging>/a\  <version>$MAIN_VERSION_SNAPSHOT</version>" "$file"
        fi
    fi
}

# Function to add version element after packaging in docker feature pom.xml files
# Adds the SNAPSHOT version (original version before release) as the artifact version
add_feature_version_docker() {
    local file="$1"
    echo "Adding feature version element in $file (version: $DOCKER_VERSION_SNAPSHOT)"
    # Check if version line already exists after packaging (not in parent block)
    # If packaging line exists but no version after it, add the SNAPSHOT version
    if grep -q '<packaging>eclipse-feature</packaging>' "$file"; then
        # Only add if there's no version line immediately after packaging
        if ! grep -A 1 '<packaging>eclipse-feature</packaging>' "$file" | grep -q '<version>'; then
            sed -i "/<packaging>eclipse-feature<\/packaging>/a\  <version>$DOCKER_VERSION_SNAPSHOT</version>" "$file"
        fi
    fi
}

# Function to add version element after packaging in plugin pom.xml files
# Adds the SNAPSHOT version (original version before release) as the artifact version
add_plugin_version_docker() {
    local file="$1"
    echo "Adding plugin version element in $file (version: $DOCKER_VERSION_SNAPSHOT)"
    # Check if version line already exists after packaging (not in parent block)
    # If packaging line exists but no version after it, add the SNAPSHOT version
    if grep -q '<packaging>eclipse-plugin</packaging>' "$file"; then
        # Only add if there's no version line immediately after packaging
        if ! grep -A 1 '<packaging>eclipse-plugin</packaging>' "$file" | grep -q '<version>'; then
            sed -i "/<packaging>eclipse-plugin<\/packaging>/a\	<version>$DOCKER_VERSION_SNAPSHOT</version>" "$file"
        fi
    fi
}

cd "$REPO_ROOT"

# Update root pom.xml
echo "=== Updating root pom.xml ==="
update_parent_version_top "pom.xml"

# ==================== CHANGELOG ====================
echo "=== Processing changelog module ==="
update_parent_version_main "changelog/pom.xml"

# changelog features
update_p2_inf_linuxtools "changelog/org.eclipse.linuxtools.changelog-feature/p2.inf"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog-feature/pom.xml"
add_feature_version_main "changelog/org.eclipse.linuxtools.changelog-feature/pom.xml"

update_p2_inf_linuxtools "changelog/org.eclipse.linuxtools.changelog.c-feature/p2.inf"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.c-feature/pom.xml"
add_feature_version_main "changelog/org.eclipse.linuxtools.changelog.c-feature/pom.xml"

update_p2_inf_linuxtools "changelog/org.eclipse.linuxtools.changelog.java-feature/p2.inf"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.java-feature/pom.xml"
add_feature_version_main "changelog/org.eclipse.linuxtools.changelog.java-feature/pom.xml"

# changelog plugins
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.core/pom.xml"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.cparser/pom.xml"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.doc/pom.xml"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.javaparser/pom.xml"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.tests/pom.xml"
update_parent_version_main "changelog/org.eclipse.linuxtools.changelog.ui.tests/pom.xml"

# ==================== CONTAINERS ====================
echo "=== Processing containers module ==="
sed -i "s|<version>$DOCKER_VERSION_SNAPSHOT</version>|<version>$DOCKER_VERSION</version>|" "containers/pom.xml"
sed -i "/<parent>/,/<\/parent>/ s|<version>$MAIN_VERSION_SNAPSHOT</version>|<version>$MAIN_VERSION</version>|" "containers/pom.xml"

# docker features
update_p2_inf_docker "containers/org.eclipse.linuxtools.docker-feature/p2.inf"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker-feature/pom.xml"
add_feature_version_docker "containers/org.eclipse.linuxtools.docker-feature/pom.xml"

update_parent_version_docker "containers/org.eclipse.linuxtools.docker.editor.ls-feature/pom.xml"
add_feature_version_docker "containers/org.eclipse.linuxtools.docker.editor.ls-feature/pom.xml"

update_p2_inf_docker "containers/org.eclipse.linuxtools.jdt.docker.launcher-feature/p2.inf"
update_parent_version_docker "containers/org.eclipse.linuxtools.jdt.docker.launcher-feature/pom.xml"
add_feature_version_docker "containers/org.eclipse.linuxtools.jdt.docker.launcher-feature/pom.xml"

# docker plugins
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.core/pom.xml"
add_plugin_version_docker "containers/org.eclipse.linuxtools.docker.core/pom.xml"

update_parent_version_docker "containers/org.eclipse.linuxtools.docker.ui/pom.xml"
add_plugin_version_docker "containers/org.eclipse.linuxtools.docker.ui/pom.xml"

update_parent_version_docker "containers/org.eclipse.linuxtools.docker.docs/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.editor.ls.tests/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.editor.ls/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.integration.tests/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.reddeer/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.docker.ui.tests/pom.xml"
update_parent_version_docker "containers/org.eclipse.linuxtools.jdt.docker.launcher/pom.xml"

# ==================== GCOV ====================
echo "=== Processing gcov module ==="
update_parent_version_main "gcov/pom.xml"

update_p2_inf_linuxtools "gcov/org.eclipse.linuxtools.gcov-feature/p2.inf"
update_parent_version_main "gcov/org.eclipse.linuxtools.gcov-feature/pom.xml"
add_feature_version_main "gcov/org.eclipse.linuxtools.gcov-feature/pom.xml"

update_parent_version_main "gcov/org.eclipse.linuxtools.gcov.core/pom.xml"
update_parent_version_main "gcov/org.eclipse.linuxtools.gcov.docs/pom.xml"
update_parent_version_main "gcov/org.eclipse.linuxtools.gcov.launch/pom.xml"
update_parent_version_main "gcov/org.eclipse.linuxtools.gcov.test/pom.xml"

# ==================== GPROF ====================
echo "=== Processing gprof module ==="
update_parent_version_main "gprof/pom.xml"

update_p2_inf_linuxtools "gprof/org.eclipse.linuxtools.gprof-feature/p2.inf"
update_parent_version_main "gprof/org.eclipse.linuxtools.gprof-feature/pom.xml"
add_feature_version_main "gprof/org.eclipse.linuxtools.gprof-feature/pom.xml"

update_parent_version_main "gprof/org.eclipse.linuxtools.gprof/pom.xml"
update_parent_version_main "gprof/org.eclipse.linuxtools.gprof.docs/pom.xml"
update_parent_version_main "gprof/org.eclipse.linuxtools.gprof.launch/pom.xml"
update_parent_version_main "gprof/org.eclipse.linuxtools.gprof.test/pom.xml"

# ==================== JAVADOCS ====================
echo "=== Processing javadocs module ==="
update_parent_version_main "javadocs/pom.xml"

update_parent_version_main "javadocs/org.eclipse.linuxtools.javadocs-feature/pom.xml"
add_feature_version_main "javadocs/org.eclipse.linuxtools.javadocs-feature/pom.xml"

update_parent_version_main "javadocs/org.eclipse.linuxtools.javadocs/pom.xml"
update_parent_version_main "javadocs/org.eclipse.linuxtools.javadocs.tests/pom.xml"

# ==================== LIBHOVER ====================
echo "=== Processing libhover module ==="
update_parent_version_main "libhover/pom.xml"

update_p2_inf_linuxtools "libhover/org.eclipse.linuxtools.cdt.libhover-feature/p2.inf"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover-feature/pom.xml"
add_feature_version_main "libhover/org.eclipse.linuxtools.cdt.libhover-feature/pom.xml"

update_p2_inf_linuxtools "libhover/org.eclipse.linuxtools.cdt.libhover.devhelp-feature/p2.inf"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.devhelp-feature/pom.xml"
add_feature_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.devhelp-feature/pom.xml"

update_p2_inf_linuxtools "libhover/org.eclipse.linuxtools.cdt.libhover.newlib-feature/p2.inf"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.newlib-feature/pom.xml"
add_feature_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.newlib-feature/pom.xml"

update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.devhelp/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.devhelp.tests/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.glibc/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.library.docs/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.libstdcxx/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.newlib/pom.xml"
update_parent_version_main "libhover/org.eclipse.linuxtools.cdt.libhover.tests/pom.xml"

# ==================== MAN ====================
echo "=== Processing man module ==="
update_parent_version_main "man/pom.xml"

update_p2_inf_linuxtools "man/org.eclipse.linuxtools.man-feature/p2.inf"
update_parent_version_main "man/org.eclipse.linuxtools.man-feature/pom.xml"
add_feature_version_main "man/org.eclipse.linuxtools.man-feature/pom.xml"

update_parent_version_main "man/org.eclipse.linuxtools.man.core/pom.xml"
update_parent_version_main "man/org.eclipse.linuxtools.man.help/pom.xml"

# ==================== PERF ====================
echo "=== Processing perf module ==="
update_parent_version_main "perf/pom.xml"

update_p2_inf_linuxtools "perf/org.eclipse.linuxtools.perf-feature/p2.inf"
update_parent_version_main "perf/org.eclipse.linuxtools.perf-feature/pom.xml"
add_feature_version_main "perf/org.eclipse.linuxtools.perf-feature/pom.xml"

update_p2_inf_linuxtools "perf/org.eclipse.linuxtools.perf.remote-feature/p2.inf"
update_parent_version_main "perf/org.eclipse.linuxtools.perf.remote-feature/pom.xml"
add_feature_version_main "perf/org.eclipse.linuxtools.perf.remote-feature/pom.xml"

update_parent_version_main "perf/org.eclipse.linuxtools.perf/pom.xml"
update_parent_version_main "perf/org.eclipse.linuxtools.perf.doc/pom.xml"
update_parent_version_main "perf/org.eclipse.linuxtools.perf.swtbot.tests/pom.xml"
update_parent_version_main "perf/org.eclipse.linuxtools.perf.tests/pom.xml"

# ==================== PROFILING ====================
echo "=== Processing profiling module ==="
update_parent_version_main "profiling/pom.xml"

update_p2_inf_linuxtools "profiling/org.eclipse.linuxtools.dataviewers-feature/p2.inf"
update_parent_version_main "profiling/org.eclipse.linuxtools.dataviewers-feature/pom.xml"
add_feature_version_main "profiling/org.eclipse.linuxtools.dataviewers-feature/pom.xml"

update_p2_inf_linuxtools "profiling/org.eclipse.linuxtools.profiling-feature/p2.inf"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling-feature/pom.xml"
add_feature_version_main "profiling/org.eclipse.linuxtools.profiling-feature/pom.xml"

update_p2_inf_linuxtools "profiling/org.eclipse.linuxtools.profiling.remote-feature/p2.inf"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.remote-feature/pom.xml"
add_feature_version_main "profiling/org.eclipse.linuxtools.profiling.remote-feature/pom.xml"

update_parent_version_main "profiling/org.eclipse.linuxtools.binutils/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.dataviewers/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.dataviewers.charts/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.dataviewers.piechart/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.docs/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.launch/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.launch.ui.rdt.proxy/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.provider.tests/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.tests/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.ui/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.profiling.ui.capability/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.rdt.proxy/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.rdt.proxy.tests/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.remote.proxy.tests/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.ssh.proxy/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.tools.launch.core/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.tools.launch.core.tests/pom.xml"
update_parent_version_main "profiling/org.eclipse.linuxtools.tools.launch.ui/pom.xml"

# ==================== RELENG ====================
echo "=== Processing releng module ==="
update_parent_version_main "releng/pom.xml"
update_parent_version_main "releng/org.eclipse.linuxtools.docker-site/pom.xml"
update_parent_version_main "releng/org.eclipse.linuxtools.releng-site/pom.xml"

# ==================== RPM ====================
echo "=== Processing rpm module ==="
update_parent_version_main "rpm/pom.xml"

update_p2_inf_linuxtools "rpm/org.eclipse.linuxtools.rpm-feature/p2.inf"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm-feature/pom.xml"
add_feature_version_main "rpm/org.eclipse.linuxtools.rpm-feature/pom.xml"

update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.core/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.core.tests/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.rpmlint/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.ui/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.ui.editor/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.ui.editor.doc/pom.xml"
update_parent_version_main "rpm/org.eclipse.linuxtools.rpm.ui.editor.tests/pom.xml"

# ==================== SYSTEMTAP ====================
echo "=== Processing systemtap module ==="
update_parent_version_main "systemtap/pom.xml"

update_p2_inf_linuxtools "systemtap/org.eclipse.linuxtools.callgraph-feature/p2.inf"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph-feature/pom.xml"
add_feature_version_main "systemtap/org.eclipse.linuxtools.callgraph-feature/pom.xml"

update_p2_inf_linuxtools "systemtap/org.eclipse.linuxtools.systemtap-feature/p2.inf"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap-feature/pom.xml"
add_feature_version_main "systemtap/org.eclipse.linuxtools.systemtap-feature/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph.core/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph.docs/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph.launch/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph.launch.tests/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.callgraph.tests/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.graphing.core/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.graphing.core.tests/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.graphing.ui/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.structures/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.structures.tests/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.ui.consolelog/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.ui.consolelog.tests/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.ui.doc/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.ui.ide/pom.xml"
update_parent_version_main "systemtap/org.eclipse.linuxtools.systemtap.ui.ide.tests/pom.xml"

# ==================== VAGRANT ====================
echo "=== Processing vagrant module ==="
sed -i "s|<version>$DOCKER_VERSION_SNAPSHOT</version>|<version>$DOCKER_VERSION</version>|" "vagrant/pom.xml"
sed -i "/<parent>/,/<\/parent>/ s|<version>$MAIN_VERSION_SNAPSHOT</version>|<version>$MAIN_VERSION</version>|" "vagrant/pom.xml"

update_p2_inf_docker "vagrant/org.eclipse.linuxtools.vagrant-feature/p2.inf"
update_parent_version_docker "vagrant/org.eclipse.linuxtools.vagrant-feature/pom.xml"
add_feature_version_docker "vagrant/org.eclipse.linuxtools.vagrant-feature/pom.xml"
update_parent_version_docker "vagrant/org.eclipse.linuxtools.vagrant.docs/pom.xml"
update_parent_version_docker "vagrant/org.eclipse.linuxtools.vagrant.core/pom.xml"
update_parent_version_docker "vagrant/org.eclipse.linuxtools.vagrant.ui/pom.xml"

# ==================== VALGRIND ====================
echo "=== Processing valgrind module ==="
update_parent_version_main "valgrind/pom.xml"

update_p2_inf_linuxtools "valgrind/org.eclipse.linuxtools.valgrind-feature/p2.inf"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind-feature/pom.xml"
add_feature_version_main "valgrind/org.eclipse.linuxtools.valgrind-feature/pom.xml"
update_p2_inf_linuxtools "valgrind/org.eclipse.linuxtools.valgrind.remote-feature/p2.inf"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.remote-feature/pom.xml"
add_feature_version_main "valgrind/org.eclipse.linuxtools.valgrind.remote-feature/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.cachegrind/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.cachegrind.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.core/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.core.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.doc/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.helgrind/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.helgrind.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.launch/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.massif/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.massif.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.memcheck/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.memcheck.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.tests/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.ui/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.ui.editor/pom.xml"
update_parent_version_main "valgrind/org.eclipse.linuxtools.valgrind.ui.tests/pom.xml"

echo ""
echo "==================================================================="
echo "Final changes for Linux Tools $MAIN_VERSION release completed successfully!"
echo "==================================================================="
echo ""
echo "Summary of changes:"
echo "  - Updated all parent versions from $MAIN_VERSION_SNAPSHOT to $MAIN_VERSION"
echo "  - Updated docker and vagrant parent versions from $DOCKER_VERSION_SNAPSHOT to $DOCKER_VERSION"
echo "  - Updated all p2.inf files to use stable update sites"
echo "  - Added version elements to feature pom.xml files"
echo ""
echo "These changes prepare the repository for the $MAIN_VERSION release."
