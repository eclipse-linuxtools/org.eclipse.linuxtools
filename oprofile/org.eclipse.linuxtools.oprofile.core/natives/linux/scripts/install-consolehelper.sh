#!/bin/bash
#
# This script installs the files necessary for root authentication when using
# Eclipse-OProfile.
#

### check install requirements ###

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

#need oprofile package to use plugin at all
RET=0
test -x /usr/bin/oprofiled 
RET=$(($RET + $?))
test -x /usr/bin/opcontrol
RET=$(($RET + $?))
if [ $RET -ne 0 ]; then
 echo >&2 "Error: required binaries do not exist, OProfile not installed?"
 exit 1
fi

# need consolehelper and consolehelper-gtk to run opcontrol as
# root from within eclipse
if [ ! -x /usr/bin/consolehelper ]; then
  echo >&2 "Error: /usr/bin/consolehelper does not exist, run install-noconsolehelper.sh instead."
  exit 1
fi
# consolehelper-gtk is required for consolehelper to pop up the
# GUI dialog and ask for the root password.
if [ ! -x /usr/bin/consolehelper-gtk ]; then
  echo >&2 "Error: /usr/bin/consolehelper-gtk does not exist."
  echo >&2 "On RHEL/Fedora you can install it by: yum install usermode-gtk."
  exit 1
fi

### install ###

#create the sym link to consolehelper
test -L ./opcontrol || { rm -f ./opcontrol && ln -s /usr/bin/consolehelper opcontrol ; }
if [ $? -ne 0 ]; then
  echo >&2 "Error: cannot create opcontrol wrapper in `pwd`"
  exit 1
fi

test -f /etc/security/console.apps/opcontrol || install -D -m 644 opcontrol-wrapper.security /etc/security/console.apps/opcontrol
test -f /etc/pam.d/opcontrol || install -D -m 644 opcontrol-wrapper.pamd /etc/pam.d/opcontrol

echo Eclipse-OProfile plugin install successful.
