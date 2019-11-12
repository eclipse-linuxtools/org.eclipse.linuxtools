#!/usr/bin/env sh
pid=`echo $$`
if [ `ls -l /proc/$pid/fd | grep -c nohup.out` -eq 0 ]
  then
    OPT="-i"
fi
$SHELL $OPT -l -c "env | grep DOCKER"
