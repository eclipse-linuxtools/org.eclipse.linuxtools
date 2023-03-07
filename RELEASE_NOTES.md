# Eclipse Linux Tools: Release notes

This page describes the noteworthy improvements provided by each release of Eclipse Linux Tools.

## Next release...

## 8.10.0

### Docker Tooling Aarch64 Support

Docker Tooling has switched over to use a new jnr stack that supports Aarch64 which is
needed to run on modern macOS machines.

## 8.9.0

### Libhover devhelp support enhanced

Libhover devhelp support now handles multiple devhelp directories specified by the preferences
as devhelp books may be distributed over multiple directories.  In addition, the help pages
for Libhover devhelp have been improved.

### Docker Tooling pullImage rework

A public api in Docker Tooling for pulling images has been reworked so it does not expose
an org.mandas.docker-client exception which means that the consumer must add org.mandas.docker-client
to its requirements.  A new wrapped exception is now supported and the old API has been deprecated.

## 8.8.0

### Move Linux Tools plug-ins to Java 17

Linux Tools plug-ins now require Java 17

### Docker Image Pull Cancellation

A Docker pull of an Image can now be cancelled.  This support was added
as part of a CDT enhancement to allow Images to be specified by name and
pulled if not already in the local Image repository.

### Gcov Support

The Linux Tools Gcov plug-in now support GCC 12.

## 8.7.0

### Update Dockerfile LS to 0.9.0

Changelog available [here](https://github.com/rcjsuen/dockerfile-language-server-nodejs/blob/master/CHANGELOG.md#090---2022-05-04).

### Support Dockerfile.* file types

Dockerfile editor now recognizes Dockerfile.* type too.

## 8.6.0

### Update Dockerfile LS to 0.7.3

Changelog available [here](https://github.com/rcjsuen/dockerfile-language-server-nodejs/blob/master/CHANGELOG.md#073---2021-12-12).
