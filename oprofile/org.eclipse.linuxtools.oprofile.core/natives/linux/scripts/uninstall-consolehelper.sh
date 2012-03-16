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

rm -f /etc/security/console.apps/opcontrol
rm -f /etc/pam.d/opcontrol
rm -f ./opcontrol

echo Eclipse-OProfile plugin uninstall successful.
