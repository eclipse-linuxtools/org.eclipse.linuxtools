README

Sample execution of build on build.eclipse.org:

cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD; cvs up -Pd;

# get releng from CVS
cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD/tools/scripts/; ./start_logger.sh \
  -projectid technology.linuxtools -version 0.2.0 \
  -projRelengRoot ':pserver:anonymous@dev.eclipse.org:/cvsroot/technology' \
  -projRelengPath 'org.eclipse.dash/athena/org.eclipse.dash.commonbuilder/org.eclipse.linuxtools.releng' \
  -basebuilderBranch R35_M4 -javaHome /opt/public/common/ibm-java-jdk-ppc-60 \
  -URL http://download.eclipse.org/eclipse/downloads/drops/R-3.4.1-200809111700/eclipse-SDK-3.4.1-linux-gtk-ppc.tar.gz \
  -URL http://download.eclipse.org/tools/cdt/releases/ganymede/dist/cdt-master-5.0.0.zip \
  -URL http://download.eclipse.org/birt/downloads/drops/R-R1-2_3_1-200809221151/birt-report-framework-2_3_1.zip \
  -URL http://download.eclipse.org/modeling/emf/emf/downloads/drops/2.4.1/R200808251517/emf-runtime-2.4.1.zip \
  -antTarget run -buildType N 2>&1 &
  
# get releng from SVN
cd /opt/public/cbi/build/org.eclipse.dash.common.releng_HEAD/tools/scripts/; ./start_logger.sh \
  -projectid technology.linuxtools -version 0.2.0 \
  -projRelengRoot 'http://dev.eclipse.org/svnroot/technology/org.eclipse.linuxtools/releng/trunk' \
  -projRelengPath 'org.eclipse.linuxtools.releng' \
  -basebuilderBranch R35_M4 -javaHome /opt/public/common/ibm-java-jdk-ppc-60 \
  -URL http://download.eclipse.org/eclipse/downloads/drops/R-3.4.1-200809111700/eclipse-SDK-3.4.1-linux-gtk-ppc.tar.gz \
  -URL http://download.eclipse.org/tools/cdt/releases/ganymede/dist/cdt-master-5.0.0.zip \
  -URL http://download.eclipse.org/birt/downloads/drops/R-R1-2_3_1-200809221151/birt-report-framework-2_3_1.zip \
  -URL http://download.eclipse.org/modeling/emf/emf/downloads/drops/2.4.1/R200808251517/emf-runtime-2.4.1.zip \
  -antTarget run -buildType N 2>&1 &
  