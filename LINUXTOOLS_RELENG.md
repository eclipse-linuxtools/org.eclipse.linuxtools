test change
p2 Repositories
===============

-   Nightly (what Git master targets):
    <http://download.eclipse.org/linuxtools/updates-nightly>
-   Latest release is always
    <http://download.eclipse.org/linuxtools/update>

Release HOWTO
=============

As code is being worked on
--------------------------

1.  Write
    [N&N](https://github.com/eclipse-linuxtools/org.eclipse.linuxtools/blob/master/RELEASE_NOTES.md)
    items as new noteworthy stuff is committed.
2.  Ensure the [project
    plan](http://eclipse.org/projects/project-plan.php?projectid=tools.linuxtools)
    is kept up to date with release deliverables and dates. The next
    release should always be set up after the previous one completes. To
    add a new release, go to the [project plan web
    page](http://eclipse.org/projects/project-plan.php?projectid=tools.linuxtools)
    and expand the Committer Tools section. There, you will find Create
    New Release under the Releases group. Create the release and set the
    release date according to the release train final release date. The
    release should be major if APIs are being removed or changed.
    Otherwise a minor release or bug-fix release is sufficient. A
    bug-fix only release does not require a review.

ASAP when a release date is known
---------------------------------

1.  Let project committers and contributors know the cut-off date for
    accepting patches from non-committers. Reminders on the dates from
    the table should be send to the mailing list too.

Proposed freeze periods:

  Phase                                                   Period
  ------------------------------------------------------- ------------------------------------
  Feature Complete (all new functionality is in git)      3 weeks prior to release date
  Code Freeze (approval by one other committer)           last 3 weeks prior to release date
  Strict Code Freeze (approval by the release engineer)   last 10 days prior to release date

3 weeks before the planned release date or RC2 milestone
--------------------------------------------------------

1.  Let the project know that the date has now arrived that new
    contributions from non-committers cannot be accepted for this
    release.
2.  Email the EMO (emo@eclipse.org) to request a release review and
    he/she will create a bug
3.  If this is the December release
    1.  Go over [IP Contribution
        Review](http://eclipse.org/projects/tools/ip_contribution_review.php?id=tools.linuxtools)
        to ensure patches have iplog+ where necessary (note: I've gone
        over everything earlier than bug \#315815 and concluded we don't
        need to do anything -- Andrew Overholt)
    2.  Submit IP log with the [IP log
        tool](http://www.eclipse.org/projects/ip_log.php?projectid=tools.linuxtools).

4.  Prepare the release review documentation and edit the online release
    plan for the specified release. A lot of information can be copied
    over from the previous release online documentation. In the past,
    this was done as a "docuware" document. Those older documents can be
    found in our website repository's 'doc' sub-directory. The release
    documentation should have a pointer to our N&N file:
    <https://github.com/eclipse-linuxtools/org.eclipse.linuxtools/blob/master/RELEASE_NOTES.md>.

2 weeks before the planned release date or RC2 milestone
--------------------------------------------------------

1.  Create a *stable-x* branch (ex. stable-5.9) on
    <https://github.com/eclipse-linuxtools/org.eclipse.linuxtools> using
    the branch pull-down
2.  On the *stable-x* branch
    1.  modify **Jenkinsfile** so the branch used is *stable-x* and the
        build output is written to **updates-nightly-<release>** (e.g.
        8.5) and '''updates-docker-nightly-<release> (e.g. 5.5) instead
        of *updates-nightly* and *updates-docker-nightly* (reserved for
        the bleeding-edge *master*)
    2.  modify the top-level **pom.xml** so the mirror-repo-name
        properties have values of **update-<version>** (e.g.
        update-8.5.0) and **update-docker-<dockerversion>** (e.g.
        update-docker-5.5.0) instead of *updates-nightly* and
        *updates-docker-nightly* (reserved for the bleeding-edge
        *master*)

3.  On master, update the version number in the pom.xml files (ex. mvn3
    org.eclipse.tycho:tycho-versions-plugin:0.17.0:set-version
    -DnewVersion=\${to-be-released-version + 1}-SNAPSHOT; commit and
    push the resulting changes) Note that you will have to manually
    change the *containers* and *vagrant* directories to change their
    pom versions
    1.  Check if all versions were updated correctly. Usually maven
        updates more files than we want. We need in this step to update
        the versions from the pom.xml file from the root directory and
        from pom.xml files located in 1st level of directories. We don't
        need to update the version in plugins and features in this step.

4.  On master, update the linuxtools-eX.X target file when the new
    release is started to point to new I-build.
5.  **Ensure the repos listed in our parent pom.xml contain the
    dependencies of the correct versions against which we should be
    building/testing** \<--- this is important!
6.  Commit any work not targetted at the release to master
7.  Commit changes specifically targetted towards the release **only**
    on the release branch (ex. stable-0.9)
8.  Commit changes targetted at **both** the release **and** trunk on
    master and then
    [EGit/User\_Guide\#Cherry\_Picking](EGit/User_Guide#Cherry_Picking "wikilink")
    the commit(s) from master to the branch

10 days before the planned release date or RC2 milestone and this is the December release
-----------------------------------------------------------------------------------------

1.  After IP log has been cleared, save a copy in the website repository
    (the legal team usually emails a PDF or HTML snapshot to the IP log
    approval requestor).
2.  Email [our PMC](https://dev.eclipse.org/mailman/listinfo/toolsy-pmc)
    to request approval for release. A previous request looks like
    [this](http://dev.eclipse.org/mhonarc/lists/technology-pmc/msg03256.html).
    This is only required for a major (x.0) or minor ({\*.x) release.
    1.  Include links to the HTML for the release documentation, IP log
        and N&N

3.  Await PMC approval on tools-pmc mailing list. For the Tools PMC,
    there is no single approval. What happens is that one or more
    committers will +1 the request and if no one disputes, then consider
    these +1s as approvals.
4.  Once the PMC has approved the release, email the resulting release
    review docuware HTML, the IP log snapshot, and a link to the PMC
    approval email to emo@eclipse.org. If the emo has opened a release
    tracker bug, you may consider the release approved if the bug has
    been closed and you have sent the appropriate documentation. In most
    cases, you will already have supplied the docuware and ip log to the
    bug.
    1.  The EMO may need a week to review the documents, so this step
        should attempt to finish at least one week to the release day.
        The emo team usually start the review process at Wednesdays. So
        it is a good idea to finish this step before the Wednesday that
        it at least one week from the release date. If this won't be
        possible, ask them about deadlines.

A few days before the release or RC2 milestone
----------------------------------------------

1.  All component leads should now be satisfied that the branch is
    stable and all unit tests should be passing
2.  Remove the "-SNAPSHOT" from pom.xml versions (find . -name pom.xml |
    xargs sed -i '/-parent/,+3 s/-SNAPSHOT//1')
    -   We used to use mvn3
        org.eclipse.tycho:tycho-versions-plugin:set-version
        -DnewVersion=\${to-be-released-version} but the maven command
        removes -SNAPSHOT from feature and plugin versions and their
        corresponding .qualifier references in feature.xml and
        MANIFEST.MF. Regardless, you should look at the build to make
        sure all features/plug-ins have date stamps.

3.  Ensure all p2.inf files found in features, point to the Linux Tools
    stable update site. To do a mass change, one could do: find . -name
    p2.inf | xargs sed -i -e 's/updates-nightly/update/g'; find . -name
    p2.inf | xargs sed -i -e 's/updates-docker-nightly/update-docker/g'.
    The master branch will point to updates-nightly and
    updates-docker-nightly by default. A branch already used for a
    stable release will likely already be pointed to the correct update
    site and no action needs to be taken. Look in an existing feature
    such as autotools/org.eclipse.linuxtools.autotools-feature for
    confirmation.
    -   In the case of a release which will be contributed to a
        simultaneous release, this step needs to be done in advance of
        the RC4 contribution build.

4.  With the version set properly in the repository's pom.xml files,
    push a build in Jenkins. Note that if the release is also targeted
    for a simultaneous release, then the last build submitted for RC2
    must be the release build.
5.  Verify the p2 repository
    -   A handy way to list the contents of a p2 repository is: eclipse
        -consolelog -nosplash -application
        org.eclipse.equinox.p2.director -repository <url> -list
    -   Ensure all qualifiers are the same and are what you expect
    -   Using the p2 UI, ensure the IUs are categorized properly

6.  Perform a quick smoke test to ensure the build is acceptable. Zero
    code changes should have happened since the SHA-1 that was used for
    the previous build.
7.  Ensure
    [Equinox/p2/p2.mirrorsURL](Equinox/p2/p2.mirrorsURL "wikilink") has
    been automatically set in artifacts.xml (in artifacts.jar) and that
    the p2.index file exists.
8.  If things are acceptable with the signed build, tag the Git repo:
    -   use a tag of the format vMajor.Minor.Micro (ex. v0.7.1)
    -   use the -a flag on the tag
    -   specify --tags on the git push command to ensure that the tag is
        visible to others
    -   do not tag until the IP Log is completed and the repository has
        been tested
    -   if you tag too early, you can tag again using -a -f to force the
        re-tag, however, if you re-tag, you need to send a note out to
        indicate this has happened
    -   once complete, an announcement note should be sent out to tell
        the list that the tag has been created and for what commit SHA-1
        the tag is for.

9.  Lock the Hudson job that was used for the release build to prevent
    automatic deletion (ex. build \#263) and add a description (ex.
    "0.7.1 release")
10. Save the archive of the entire build somewhere before it gets
    over-written (it will not be stored across the next build)
    -   ex. wget --no-check-certificate
        <https://ci.eclipse.org/linuxtools/job/linuxtools/job/stable-8.6/4/artifact/*zip*/archive.zip>

11. Get source tarballs:
    1.  Go to
        <https://github.com/eclipse-linuxtools/org.eclipse.linuxtools/releases/tag/vX.Y.Z>
    2.  Download the .tar.gz file for the matching tag to the Linux
        Tools downloads area (need to have special access rights)
    3.  rename the tarball to linuxtools-\${version}.tar.gz
    4.  move the source tarball to
        <http://download.eclipse.org/technology/linuxtools/>\${version}-sources/,
        copying and modifying index.php from a previous release
    5.  chmod 755 the source tarball

Release day
-----------

1.  Put an archive of the entire build into the downloads directory
    (\~/downloads/technology/linuxtools) (done via Jenkins job)
    1.  Unzip the archive.zip saved after last successful build
    2.  cd to sub-directory containing actualy repository and re-zip
        contents to linuxtools-RELEASE.zip in the downloads directory
    3.  Run md5sum linuxtools-RELEASE-incubation.zip
        \>linuxtools-RELEASE.zip.md5.
    4.  move the previous linuxtools-RELEASE.zip and md5 to
        /home/data/httpd/archive.eclipse.org/linuxtools
    5.  chgrp technology.linux-distros linuxtools-RELEASE.zip\*
    6.  chmod 755 linuxtools-RELEASE.zip\*

2.  Update the link on downloads.php to point to the correct listing of
    source tarballs for this release
3.  Update the "Unit test results" link on downloads.php to point to the
    Hudson job that was used
4.  Add a news item to the main wiki page
5.  Ensure download links -- including the zip of the repository -- are
    correct
6.  Re-verify the p2 repository
    1.  A handy way to list the contents of a p2 repository is: eclipse
        -consolelog -nosplash -application
        org.eclipse.equinox.p2.director -repository <url> -list
    2.  Ensure all qualifiers are the same and are what you expect
    3.  Using the p2 UI, ensure the IUs are categorized properly

7.  Copy our p2 repository to a versioned copy to archive it (ex. cp -rp
    updates-nightly-8.5.0 update-8.5.0)
8.  Update any relevant Eclipse Marketplace pages
    1.  <https://marketplace.eclipse.org/content/eclipse-docker-tooling>
    2.  <https://marketplace.eclipse.org/content/eclipse-vagrant-tooling>

9.  Announce release on mailing list and blog(s)
10. Mark release as complete in list of releases in portal
11. Add next version to list of releases in portal

Day after the release
---------------------

1.  Relax :)
2.  Decide whether or not older releases should be [moved to
    archive.eclipse.org](IT_Infrastructure_Doc#Move_files_to_archive.eclipse.org.3F "wikilink")
    (take note of [p2.mirrorsURL
    modification](Equinox/p2/p2.mirrorsURL#Moving_a_repo_to_archive.eclipse.org "wikilink"))
3.  Switch the Jenkins job back to use the master or stable-\*\*\*
    branch
4.  Start on the next release

Simultaneous Release Inclusion
==============================

As of Helios, Linux Tools is a part of the annual Eclipse simultaneous
release. The simultaneous release aggregator takes content from the p2
repos that are listed in our b3aggrcon files. Builds that we would like
to promote to the simultaneous release must:

-   be signed
-   not necessarily be in category.xml
-   exist in the p2 repository listed in the linuxtools.b3aggrcon file
    with the *exact same* feature versions/qualifiers

Note also that categories for the main simultaneous release are
different from our p2 repository categories (which are set in our
[category.xml](https://github.com/eclipse-linuxtools/org.eclipse.linuxtools/blob/v8.6.0/releng/org.eclipse.linuxtools.releng-site/category.xml)).

The builds *must* remain in the p2 repo until we change the feature
versions/qualifiers in the b3aggrcon file. This will prevent future
aggregation runs from failing. More information can be found here:

-   [Simrel/Contributing\_to\_Simrel\_Aggregation\_Build](Simrel/Contributing_to_Simrel_Aggregation_Build "wikilink")
    \<-- This one contains the git location for org.eclipse.simrel.build
    where the linuxtools.b3aggrcon file lives in the master branch and
    will need to be updated to new qualifiers [and versions] when we
    want to include a new release in Simrel

The [cross-project-issues-dev mailing
list](https://dev.eclipse.org/mailman/listinfo/cross-project-issues-dev)
**must** be monitored by project leads and release engineers. This
mailing list can help with any problems with the simultaneous release as
well as with EPP packages. David Williams leads the simultaneous release
creation and Markus Knauer coordinates the EPP packages including "ours"
([Indigo](http://www.eclipse.org/downloads/packages/release/indigo/rc1)).

Note that during the "quiet week" between RC2 and the final release, our
release bits must be put into their final place but not be made
"visible". Read the [Final Daze](Indigo/Final_Daze "wikilink") document
for guidelines and pointers to FAQs on how to make things invisible,
etc.

Adding a new component
======================

Our build process is pom.xml-driven but we have a mirrored feature
structure for use by p2 repos. In order for a new component to be built,
it needs to fit into the hierarchy somewhere. When adding a new
top-level sub-project feature (sub-features of existing features should
get added to their containing feature), follow these steps:

Code-level checklist
--------------------

1.  Create your feature and containing plugins
    1.  Name the feature(s) org.eclipse.linuxtools.mycoolstuff and put
        it in Git as org.eclipse.linuxtools.mycoolstuff-feature
        (replacing "mycoolstuff", obviously)
    2.  Name the plugin(s) org.eclipse.linuxtools.mycoolstuff.core,
        org.eclipse.linuxtools.mycoolstuff.ui, etc.
    3.  **Do not put \_ characters in the bundle ID** (this breaks the
        Maven signing plugin we're using)
    4.  Ensure your packages are all in the org.eclipse.linuxtools
        namespace (and in the .mycoolstuff.core, .mycoolstuff.ui
        packages where appropriate)
    5.  Ensure your strings are externalized
    6.  Ensure your feature and plugin provider fields are set to
        "Eclipse Linux Tools" (no quotes)

2.  Either copy over existing pom.xml files and manually edit them or
    [generate using
    Tycho](http://mattiasholmqvist.se/2010/02/building-with-tycho-part-1-osgi-bundles/)
    and manually edit
3.  Copy over existing p2.inf file from any other feature and add p2.inf
    to your build.properties binary files
4.  Create your JUnit test plugins
    1.  Name your test plugin the same as your functional plugin but
        with a ".tests" tacked onto the end
    2.  Ensure your test bundle's pom.xml looks like an existing test
        bundle's pom.xml

5.  Enable API Tools on non-example/test/feature plugins

Git-level checklist
-------------------

1.  If this is a new sub-project, create a directory to contain it, like
    "gcov" or "gprof"
2.  Check all of your new stuff into Git master

Build-related checklist
-----------------------

1.  Add any new top-level feature to the top-level pom.xml in your Git
    clone
2.  If your sub-project has dependencies outside the existing ones
    (BIRT, EMF, DTP, CDT, GEF), notify the mailing list and project
    leads
    1.  Hopefully this will have been caught in the CQ (legal review)

3.  Ensure your
    [BREEs](http://wiki.eclipse.org/index.php/Execution_Environments)
    are correct in your plugin MANIFEST.MF files
4.  Ensure the version on your feature ends with ".qualifier" (without
    the quotation marks)
5.  Ensure the versions in your MANIFEST.MF and feature.xml files match
    those in your pom.xml files
6.  Add new features to our p2 repo's
    [category.xml](http://git.eclipse.org/c/linuxtools/org.eclipse.linuxtools.git/tree/releng/org.eclipse.linuxtools.releng-site/category.xml)
    file
7.  Ensure your pom.xml files have the same source plugin and feature
    bits as the others
8.  Run a full local build from the top-level of your git clone with
    **mvn -fae clean install** to ensure the build still works

Building locally
================

-   ensure you have a [recent Maven 3
    release](http://maven.apache.org/download.html) on your path
-   clone our [Git
    repository](https://github.com/eclipse-linuxtools/org.eclipse.linuxtools.git)
    (perhaps read our [Git
    instructions](http://wiki.eclipse.org/Linux_Tools_Project/Git))
-   cd into your clone and run *mvn -fae clean install*
-   You can add the parameter "-Dmaven.test.skip=true" for debug
    purposes, but remember that no contribution that breaks the unit
    tests will be accepted.
-   Some components have dependencies between then, for example Valgrind
    plug-ins. In that case, always build the whole Valgrind suite
    instead of one particular plug-in.
-   follow [this
    guide](http://community.jboss.org/wiki/RemoteDebuggingforEclipseTestPlug-inRunningbyTycho/version/4)
    for debugging tests being run by Tycho

Adding a new test plugin to those that are run during the automated build
=========================================================================

-   Create test plugin(s) (ex.
    org.eclipse.linuxtools.mycoolfeature.ui.tests)
-   Copy an existing test plugin's pom.xml (this is used when the
    automated build is run)
-   Add your test plugin to the parent pom.xml
-   Check your plugin(s) into Git
-   Verify that your tests are built/run with a local build (see
    instructions on this page)

The next time a build happens, your test plugin(s) will be built and
run. If you need a build pushed sooner than the next 6 hour mark when
our scheduled builds happen, speak with the project leads via
linuxtools-dev@eclipse.org.
