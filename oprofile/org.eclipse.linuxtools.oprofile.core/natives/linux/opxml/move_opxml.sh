#!/bin/sh
RELEASE_VER=0.3.0

case `uname -m` in 
 	'i386') \
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.x86_${RELEASE_VER}.*/os/linux/x86 
 	;; 
 	'i586') 
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.x86_${RELEASE_VER}.*/os/linux/x86 
 	;; 
 	'i686') 
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.x86_${RELEASE_VER}.*/os/linux/x86 
 	;; 
 	'x86_64') 
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.x86_64_${RELEASE_VER}.*/os/linux/x86_64 
 	;; 
 	'ppc') 
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.ppc_${RELEASE_VER}.*/os/linux/ppc 
 	;; 
 	'ppc64') 
 	mv -f opxml ../../../../org.eclipse.linuxtools.oprofile.core.linux.ppc_${RELEASE_VER}.*/os/linux/ppc 
 	;; 
 	*) 
 	echo Could not detect system architecture -- please move the opxml binary into the appropriate org.eclipse.linuxtools.oprofile.core.linux.[x86,x86_64,ppc]/os/linux/[x86,x86_64,ppc] directory. 
	exit 1
 	;; 
 	esac && echo Success!