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

echo Copy the following line for the sudoers file, replacing "<username>" with your username:
echo "<username> ALL=(ALL) NOPASSWD : /usr/bin/opcontrol"
read -p 'Running visudo, paste the above line in the editor, save it and exit. Press ENTER to continue.'
visudo

#create opcontrol sudo wrapper
echo '#!/bin/sh' > opcontrol
echo 'sudo /usr/bin/opcontrol ${1+"$@"}' >> opcontrol
chmod +x ./opcontrol

echo Eclipse-OProfile plugin install successful.
