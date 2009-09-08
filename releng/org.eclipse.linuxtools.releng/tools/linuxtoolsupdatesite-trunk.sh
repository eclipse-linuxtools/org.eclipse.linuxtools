#!/bin/sh

# This script unpacks the latest nightly build into our updates-nightly update site.
# It is in aoverholt's crontab as the following entry:
# 0 * * * * /home/data/users/aoverholt/.bin/linuxtoolsupdatesite-trunk.sh
#
# To clean up old builds, this is in aoverholt's crontab:
# # Clean up old N builds
# 0 4 1 * * find /home/data/users/aoverholt/downloads/technology/linuxtools -maxdepth 1 -type d -name "N200*" -mtime +14 -exec rm -rf {} \;

dropLocation=/opt/users/hudsonbuild/.hudson/jobs/cbi-linuxtools-Galileo/lastSuccessful/archive/build/
downloadsDir=/home/data/users/aoverholt/downloads/technology/linuxtools
updateSite=${downloadsDir}/updates-nightly

rsync -a ${dropLocation} ${downloadsDir}

chown -R aoverholt:linuxtoolsadmin ${downloadsDir}
chmod -R 755 ${downloadsDir}

timestamp=$(date +"%Y-%M-%d %H:%M:%S")

cd ${updateSite}

# 20 is for 2009, 2010, etc.
latest=$(ls -1 ${downloadsDir} | grep ^[NS]20 | cut -c2- | sort | tail -n 1)

if [ -e ${downloadsDir}/*${latest}/linuxtools-Update*.zip ]; then

# Clean out old
#rm -f old.tar.bz2
#tar jcf old.tar.bz2 *
rm -rf features plugins pack.properties *.xml *.jar

unzip -q -n ${downloadsDir}/*${latest}/linuxtools-Update*.zip
chown -R aoverholt:linuxtoolsadmin *
chmod -R 755 *

fi
