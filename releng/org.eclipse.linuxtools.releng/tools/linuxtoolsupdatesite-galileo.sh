#!/bin/sh

# This script unpacks the latest nightly build into our updates-nightly update site.
# It is in aoverholt's crontab as the following entry:
# 0 * * * * /home/data/users/aoverholt/.bin/linuxtoolsupdatesite-galileo.sh
#
# To clean up old builds, this is in aoverholt's crontab:
# # Clean up old N builds
# 0 4 1 * * find /home/data/users/aoverholt/downloads/technology/linuxtools -maxdepth 1 -type d -name "N200*" -mtime +14 -exec rm -rf {} \;

buildURL=https://build.eclipse.org/hudson/job/cbi-linuxtools-Galileo/lastSuccessfulBuild/artifact/build/*zip*/build.zip
downloadsDir=/home/data/users/aoverholt/downloads/technology/linuxtools
updateSite=${downloadsDir}/updates-nightly
tmpDir=/home/data/users/aoverholt/tmp
updateZip=build/*/*Update*.zip

if [ -z ${tmpDir} ]; then
  echo "Must set tmpDir!"
  exit 1
fi

if [ -z ${updateSite} ]; then
  echo "Must set updateSite!"
  exit 1
fi

cd ${tmpDir}
mv build.zip{,.old}
wget -q --no-check-certificate "${buildURL}"
if [ $? != 0 ]; then
  echo "wget failed"
  exit 1
fi
if [ -e build.zip ]; then
  rm -rf ${tmpDir}/build.zip.old ${tmpDir}/build
  unzip -d ${tmpDir} -q build.zip
  if [ -e ${updateZip} ]; then
    rm -rf ${updateSite}
    unzip -q -d ${updateSite} ${tmpDir}/build/*/*Update*.zip
    chgrp -R technology.linux-distros ${updateSite}
  fi
fi
