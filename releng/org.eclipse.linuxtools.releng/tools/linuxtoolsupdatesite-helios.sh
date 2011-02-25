#!/bin/sh

# This script unpacks the latest build against Helios deps into our updates-helios update site.
# It is in aoverholt's crontab as the following entry:
# 30 * * * * /home/data/users/aoverholt/.bin/linuxtoolsupdatesite-helios.sh

buildURL=https://build.eclipse.org/hudson/job/cbi-linuxtools-Helios/lastSuccessfulBuild/artifact/build/*zip*/build.zip
downloadsDir=/home/data/users/aoverholt/downloads/technology/linuxtools
updateSite=${downloadsDir}/updates-helios
tmpDir=/home/data/users/aoverholt/tmp/helios
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
  unzip -q -d ${tmpDir} build.zip
  if [ -e ${updateZip} ]; then
    rm -rf ${updateSite}
    unzip -q -d ${updateSite} ${tmpDir}/build/*/*Update*.zip
    chgrp -R technology.linux-distros ${updateSite}
  fi
fi
