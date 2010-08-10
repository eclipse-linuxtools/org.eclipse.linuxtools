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
 echo Error: required binaries do not exist, OProfile not installed?
 exit 1
fi

#need consolehelper to run opcontrol as root from within eclipse
test -x /usr/bin/consolehelper 
if [ $? -ne 0 ]; then
  echo Error: /usr/bin/consolehelper does not exist, run install-noconsolehelper.sh instead 
  exit 1
fi


### install ###

#create the sym link to consolehelper
test -L ./opcontrol || { rm -f ./opcontrol && ln -s /usr/bin/consolehelper opcontrol ; }
if [ $? -ne 0 ]; then
  echo Error: cannot create opcontrol wrapper in `pwd`
  exit 1
fi

#check for opxml executable, make sure it is u+x
ALLOPXML=`find ../../../../org.eclipse.linuxtools.oprofile.core.linux.* -name opxml -type f | wc -l`
EXECOPXML=`find ../../../../org.eclipse.linuxtools.oprofile.core.linux.* -name opxml -type f -perm -u+x | wc -l`
if [ $ALLOPXML -eq 0 ]; then
  echo Error: cannot find opxml binary, required plugin missing
  exit 1
elif [ $EXECOPXML -ne $ALLOPXML ]; then
  #they exist, but aren't executable, run chmod u+x on them
  find ../../../.. -name opxml -type f -exec chmod u+x '{}' \;
fi

##this will have to be loaded every time the user restarts their
##computer anyway, should load it now?
#load the oprofile module 
#test /dev/oprofile/cpu_type
#if [ $? -ne 0 ]; then
#  opcontrol --init
#fi

test -f /etc/security/console.apps/opcontrol || install -D -m 644 opcontrol-wrapper.security /etc/security/console.apps/opcontrol
test -f /etc/pam.d/opcontrol || install -D -m 644 opcontrol-wrapper.pamd /etc/pam.d/opcontrol

echo Eclipse-OProfile plugin install successful.
