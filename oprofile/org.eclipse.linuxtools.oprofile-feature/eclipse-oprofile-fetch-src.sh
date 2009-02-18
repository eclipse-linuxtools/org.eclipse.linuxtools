#!/bin/sh
usage='usage: $0 <tag>'
name=eclipse-oprofile
tag=RELEASE_0_1
tar_name=$name-fetched-src-0.1.0

fetch_cmd="svn export svn://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/oprofile/tags/$tag/ $tar_name"

if [ "x$tag"x = 'xx' ]; then
   echo >&2 "$usage"
   exit 1
fi

rm -fr $tar_name

# Fetch plugins
$fetch_cmd

# create archive
pushd $tar_name
tar -cjf ../$tar_name.tar.bz2 *
popd
