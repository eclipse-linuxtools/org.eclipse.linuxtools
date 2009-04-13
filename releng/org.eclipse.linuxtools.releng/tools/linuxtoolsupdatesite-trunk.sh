#!/bin/sh

# This script unpacks the latest 0.2 candidate build into our updates-nightly update site.
# It is in aoverholt's crontab as the following entry:
# 0 * * * * /home/data/users/aoverholt/.bin/linuxtoolsupdatesite-trunk.sh

dropLocation=/opt/users/hudsonbuild/.hudson/jobs/cbi-linuxtools-0.2.x-Ganymede-RCbuilds/lastSuccessful/archive/build/
downloadsDir=~/linuxtoolsdownloads
updateSite=${downloadsDir}/updates-nightly

rsync -a ${dropLocation} ${downloadsDir}

timestamp=$(date +"%Y-%M-%d %H:%M:%S")

cd ${updateSite}

if [ ! -e site.xml ]; then
  svn export http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/branches/0.2.0/org.eclipse.linuxtools.updatesite/updates-nightly/site.xml
fi
if [ ! -e associateSites.xml ]; then
  svn export http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/branches/0.2.0/org.eclipse.linuxtools.updatesite/updates-nightly/associateSites.xml
fi

# 20 is for 2009, 2010, etc.
latest=$(ls -1 ${downloadsDir} | grep ^[NS]20 | cut -c2- | sort | tail -n 1)

if [ -e ${downloadsDir}/*${latest}/linuxtools-Master*.zip ]; then

# Clean out old
#rm -f old.tar.bz2
#tar jcf old.tar.bz2 *
rm -rf features plugins pack.properties

unzip -q -n ${downloadsDir}/*${latest}/linuxtools-Master*.zip
mv eclipse/* .
rmdir eclipse

# Update site.xml with the versions we have in this build
for f in `ls features/*.jar`; do version=$(echo $f | sed -e 's:features/.*_::' -e 's:\.jar::'); name=$(echo $f | sed -e 's:features/::' -e 's:\.jar::' -e 's:_::' -e "s:$version::"); sed -i -e "/${name}/ s/_.*\.jar/_${version}\.jar/" -e "/${name}/ s/version=\".*\"/version=\"${version}\"/" site.xml; done

fi