README

Sample execution of build on build.eclipse.org:

cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD; cvs up -Pd;

# get releng from CVS
cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD/tools/scripts/; ./start.sh \
  -projectid technology.linuxtools -version 0.7.0 \
  -projRelengRoot ':pserver:anonymous@dev.eclipse.org:/cvsroot/technology' \
  -projRelengPath 'org.eclipse.dash/athena/org.eclipse.dash.commonbuilder/org.eclipse.linuxtools.releng' \
  -basebuilderBranch R35_M2 -javaHome /usr/lib/jvm/java \
  -URL http://download.eclipse.org/eclipse/downloads/drops/R-3.4-200806172000/eclipse-SDK-3.4-linux-gtk.tar.gz \
  -URL http://download.eclipse.org/tools/cdt/releases/ganymede/dist/cdt-master-5.0.0.zip \
  -antTarget run -buildType N 2>&1 | tee /tmp/buildlog_linuxtools_`date +%H%M%S`.txt
  
# get releng from SVN
cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD/tools/scripts/; ./start.sh \
  -projectid technology.linuxtools -version 0.7.0 \
  -projRelengRoot 'http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/trunk' \
  -projRelengPath 'org.eclipse.linuxtools.releng' \
  -basebuilderBranch R35_M2 -javaHome /usr/lib/jvm/java \
  -URL http://download.eclipse.org/eclipse/downloads/drops/R-3.4-200806172000/eclipse-SDK-3.4-linux-gtk.tar.gz \
  -URL http://download.eclipse.org/tools/cdt/releases/ganymede/dist/cdt-master-5.0.0.zip \
  -antTarget run -buildType N 2>&1 | tee /tmp/buildlog_linuxtools_`date +%H%M%S`.txt
  