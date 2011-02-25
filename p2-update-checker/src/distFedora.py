import fedora.client.pkgdb
import fedora.client.fas2
import entity
import logging
import os
import tempfile
import utils
import commands
import config

FAS_USER = "alcapcom"
FAS_PWD = "*********"

class Distrib:
    
    ID = "fedora"
    NAME = "Fedora Project"
    
    def __init__(self, releases):
        self.releases = releases
    
    def getPackageName(self, updateId):
        cmd = "rpm -q --whatprovides --qf '%{NAME} ' " + "osgi\(%s\)" % updateId
        s, o = commands.getstatusoutput(cmd)
        # FIXME: here we take the first package that provide the given osgi bundle.
        o.split(' ')[0]
        return s, o
    
    def getSpecfileContent(self, pkgName, tag):
        pwd = commands.getoutput("pwd");
        tmpDir = tempfile.mkdtemp("%s-%s_spec" % (config.APP_NAME, pkgName))
        os.chdir(tmpDir)
        specfile = os.path.join(pkgName, tag, pkgName + ".spec")
        cmd = "CVSROOT=:pserver:anonymous@cvs.fedoraproject.org:/cvs/pkgs cvs co %s" % specfile
        status = os.system(cmd)
        if status > 0:
            config.LOG.error("Error during '%s'" % cmd)
            return
        content = utils.readFile(specfile)
        os.chdir(pwd)
        os.system("rm -rf " + tmpDir)
        return content
    
    def makeChroot(self, distRelease):
        outputDir = os.path.join(config.OUTPUT_DIR, "mock", distRelease.tag)
        if os.path.exists(config.CHROOT_DIR % distRelease.mockConfig):
            cmd = "mock --update -r %s --resultdir=%s" % (distRelease.mockConfig, outputDir)
        else:
            cmd = "mock --init -r %s --resultdir=%s" % (distRelease.mockConfig, outputDir)
        status = os.system(cmd)
        if status > 0:
            config.LOG.error("Error during '%s'" % cmd)
            return 
        cmd = "mock --install -r %s eclipse\* --resultdir=%s" % (distRelease.mockConfig, outputDir)
        status = os.system(cmd)
        if status > 0:
            config.LOG.error("Error during '%s'" % cmd)
            return 
        return  

# TODO: add co-maintainers             
class Process:
    
    DESCRIPTION = "Fedora Process"
    
    def run(self, pkgsToUpdate, distRelease):
        pkgdb = fedora.client.pkgdb.PackageDB()
        for pkgToUpdate in pkgsToUpdate:
            pkgName = pkgToUpdate.packageName.strip()
            if distRelease.tag == "devel":
                tag = "F-devel"
            else:
                tag = distRelease.tag
            pkg_info = pkgdb.get_package_info(pkgName, tag)
            pkg_info = pkg_info['packageListings'][0]
            owner = pkg_info['owneruser']
            
            fas = fedora.client.fas2.AccountSystem()
            fas.username = FAS_USER
            fas.password = FAS_PWD
            person = fas.person_by_username(owner)
            
            packager = entity.Packager(owner, person["human_name"] , person["email"], config.DISTRIB, pkgName)
            for action in config.ACTIONS:
                actionOutput = action.run(pkgToUpdate, packager, distRelease)
                loginfo = "%s (%s) for %s [%s]" % (action.DESCRIPTION, pkgToUpdate.packageName, packager.name, actionOutput)
                config.LOG.info(loginfo)  
