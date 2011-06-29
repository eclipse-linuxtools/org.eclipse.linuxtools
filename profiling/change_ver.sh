#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 old_ver new_ver"
	exit 1
fi
old_ver=$(echo "$1" | sed 's/\./\\./g')
new_ver=$(echo "$2" | sed 's/\./\\./g')

find -name 'pom.xml' -o -name 'feature.xml' -o -name 'MANIFEST.MF' | xargs sed -i "s/$old_ver/$new_ver/g"
