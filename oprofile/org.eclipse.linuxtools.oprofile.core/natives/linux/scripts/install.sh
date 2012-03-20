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

# need pkexec to run opcontrol as
# root from within eclipse
if [ ! -x /usr/bin/pkexec ]; then
  echo >&2 "Error: /usr/bin/pkexec does not exist."
  echo >&2 "On RHEL/Fedora you can install it by: yum install polkit."
  exit 1
fi

### install ###

test -f /usr/share/polkit-1/actions/org.eclipse.linuxtools.oprofile.policy || install -D -m 644 org.eclipse.linuxtools.oprofile.policy /usr/share/polkit-1/actions/org.eclipse.linuxtools.oprofile.policy

test -L ./opcontrol || rm -f ./opcontrol
test -f ./opcontrol || rm -f ./opcontrol

echo '#!/bin/sh' > opcontrol || exit 1
echo 'exec pkexec /usr/bin/opcontrol ${1+"$@"}' >> opcontrol
chmod +x ./opcontrol

echo Eclipse-OProfile plugin install successful.
