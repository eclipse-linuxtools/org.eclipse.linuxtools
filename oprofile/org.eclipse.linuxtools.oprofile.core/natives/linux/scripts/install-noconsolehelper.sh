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

echo Copy the following line for the sudoers file, replacing "<username>" with your username:
echo "<username> ALL=(ALL) NOPASSWD : /usr/bin/opcontrol"
read -p 'Running visudo, paste the above line in the editor, save it and exit. Press ENTER to continue.'
visudo

#create opcontrol sudo wrapper
if [ -L opcontrol ]; then
  read -p "Symlink 'opcontrol' exists. Do you want to remove it and continue? (y/n) " REMOVE_OLD
  if [ "${REMOVE_OLD}_" = "y_" ]; then
    rm ./opcontrol
  else
    echo >&2 "Installation cancelled on user request."
    exit 1
  fi
fi
echo '#!/bin/sh' > opcontrol
echo 'sudo /usr/bin/opcontrol ${1+"$@"}' >> opcontrol
chmod +x ./opcontrol

echo Eclipse-OProfile plugin install successful.
