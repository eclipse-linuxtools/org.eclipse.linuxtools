%define _unpackaged_files_terminate_build 0
Summary: None - Eclipse-generated spec file
Name: helloworld
Version: 2
Release: 2
License: GPL
Group: Applications/Internet
Source: helloworld-%{version}.tar.bz2
Requires: tar
BuildRoot: %{_tmppath}/%{name}-root
%description

Basic spec file for rpm build in Eclipse for helloworld

%prep
%setup -q
%build

make

%install rm -rf $RPM_BUILD_ROOT
%makeinstall RPM_BUILD_ROOT=$RPM_BUILD_ROOT
%clean
rm -rf $RPM_BUILD_ROOT
%files
%defattr(-,root,root)
/usr/local/bin/helloworld
%changelog
* Tue Sep 07 2004 Rick Moseley <rmoseley@redhat.com>
- Original
