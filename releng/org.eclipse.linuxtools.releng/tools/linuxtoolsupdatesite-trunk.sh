#!/bin/sh

# This script unpacks the latest build against Ganymede deps into our updates-nightly update site.
# It is in aoverholt's crontab as the following entry:
# 0 * * * * /home/data/users/aoverholt/.bin/linuxtoolsupdatesite-trunk.sh

dropLocation=/opt/users/hudsonbuild/.hudson/jobs/cbi-linuxtools-Galileo/lastSuccessful/archive/build/
downloadsDir=/home/data/users/aoverholt/downloads/technology/linuxtools
updateSite=${downloadsDir}/updates-nightly

rsync -a ${dropLocation} ${downloadsDir}

chown -R aoverholt:linuxtoolsadmin ${downloadsDir}
chmod -R 755 ${downloadsDir}

timestamp=$(date +"%Y-%M-%d %H:%M:%S")

cd ${updateSite}

if [ ! -e site.xml ]; then
  svn export http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/trunk/org.eclipse.linuxtools.updatesite/updates-nightly/site.xml
fi
if [ ! -e associateSites.xml ]; then
  svn export http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/trunk/org.eclipse.linuxtools.updatesite/updates-nightly/associateSites.xml
fi

# 20 is for 2009, 2010, etc.
latest=$(ls -1 ${downloadsDir} | grep ^[NS]20 | cut -c2- | sort | tail -n 1)

if [ -e ${downloadsDir}/*${latest}/linuxtools-Update*.zip ]; then

# Clean out old
#rm -f old.tar.bz2
#tar jcf old.tar.bz2 *
rm -rf features plugins pack.properties *.xml

unzip -q -n ${downloadsDir}/*${latest}/linuxtools-Update*.zip
chown -R aoverholt:linuxtoolsadmin *
chmod -R 755 *
fi