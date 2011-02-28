#!/bin/sh
#
# This script uninstalls the files necessary for root authentication when using
# Eclipse-OProfile.

#needs to be run as root
if [ `id -u` -ne 0 ]; then
  echo Error: script must be run as the root user
  exit 1
fi

#need to be in scripts dir
if [ $(basename $(pwd)) != scripts ]; then
  echo Error: script must be run with pwd in script dir
  exit 1
fi

rm -f ./opcontrol

read -p 'Running visudo; remove the line: <username> ALL=NOPASSWD : /usr/bin/opcontrol'
visudo

echo Eclipse-OProfile plugin uninstall successful.
