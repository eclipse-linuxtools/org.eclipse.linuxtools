%define src_repo_tag   0.1.0
%define eclipse_base   %{_libdir}/eclipse
%define install_loc    %{_libdir}/eclipse/dropins/oprofile
%define qualifier	   200901141551
%define ver_qual	   %{src_repo_tag}.%{qualifier}

# All arches line up between Eclipse and Linux kernel names except i386 -> x86
%ifarch %{ix86}
%define eclipse_arch    x86
%else
%define eclipse_arch   %{_arch}
%endif


Name:           eclipse-oprofile
Version:        0.1.0
Release:        1%{?dist}
Summary:        OProfile Integration (Incubation)

Group:          Development/Tools
License:        EPL
URL:            http://www.eclipse.org/linuxtools/projectPages/oprofile/
## sh %{name}-fetch-src.sh
Source0:        %{name}-fetched-src-%{src_repo_tag}.tar.bz2
Source1:        %{name}-fetch-src.sh
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

ExcludeArch: ppc64

BuildRequires: eclipse-pde >= 1:3.4.0
BuildRequires: eclipse-cdt >= 5.0.1
BuildRequires: eclipse-linuxprofilingframework >= 0.1.0
BuildRequires: oprofile >= 0.9.3
BuildRequires: oprofile-devel >= 0.9.3
BuildRequires: binutils-devel >= 2.18.50.0.6
Requires: eclipse-platform >= 3.4.0
Requires: eclipse-cdt >= 5.0.1
Requires: eclipse-linuxprofilingframework >= 0.1.0
Requires: oprofile >= 0.9.3
Requires: usermode >= 1.98

%description
Plugins to bring OProfile-based profiling into the workbench.

%prep
%setup -q -c
#remove binaries
rm -f org.eclipse.linuxtools.oprofile.core.linux.*/os/linux/*/opxml

%build
#build binaries
cd org.eclipse.linuxtools.oprofile.core/natives/linux/opxml
make

mv opxml %{_builddir}/%{name}-%{version}/org.eclipse.linuxtools.oprofile.core.linux.%{eclipse_arch}/os/linux/%{eclipse_arch}

cd %{_builddir}/%{name}-%{version}

%{eclipse_base}/buildscripts/pdebuild -f org.eclipse.linuxtools.oprofile.feature \
									-d "cdt linuxprofilingframework" \
									-a "-DjavacSource=1.5 -DjavacTarget=1.5"

%install
%{__rm} -rf %{buildroot}
install -d -m 755 %{buildroot}%{install_loc}

%{__unzip} -q -d %{buildroot}%{install_loc} \
     build/rpmBuild/org.eclipse.linuxtools.oprofile.feature.zip 

### install.sh stuff ###
%define corepath %{buildroot}%{install_loc}/eclipse/plugins/org.eclipse.linuxtools.oprofile.core_%{ver_qual}

#create opcontrol wrapper
ln -s ../../../../../../../../../../../usr/bin/consolehelper %{corepath}/natives/linux/scripts/opcontrol

#install opcontrol wrapper permission files
install -d -m 755 %{buildroot}%{_sysconfdir}/security/console.apps
install -D -m 644 org.eclipse.linuxtools.oprofile.core/natives/linux/scripts/opcontrol-wrapper.security \
					%{buildroot}%{_sysconfdir}/security/console.apps/opcontrol
install -d -m 755 %{buildroot}%{_sysconfdir}/pam.d
install -D -m 644 org.eclipse.linuxtools.oprofile.core/natives/linux/scripts/opcontrol-wrapper.pamd \
					%{buildroot}%{_sysconfdir}/pam.d/opcontrol

#remove install/uninstall script (used in update site only)
rm -f %{corepath}/natives/linux/scripts/install.sh
rm -f %{corepath}/natives/linux/scripts/uninstall.sh

#remove opxml source (rpmlint warnings)
rm -rf %{corepath}/natives/linux/opxml
rm -rf %{corepath}/natives/linux/scripts/.svnignore


%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root,-)
%{install_loc}
%doc org.eclipse.linuxtools.oprofile-feature/epl-v10.html
%attr(755,root,root) %{install_loc}/eclipse/plugins/org.eclipse.linuxtools.oprofile.core.linux.%{eclipse_arch}_%{ver_qual}/os/linux/%{eclipse_arch}/opxml
%{_sysconfdir}/security/console.apps/opcontrol
%{_sysconfdir}/pam.d/opcontrol

%changelog
* Thu Feb 12 2009 Kent Sebastian <ksebasti@redhat.com> 0.1.0-1
- Initial packaging.
