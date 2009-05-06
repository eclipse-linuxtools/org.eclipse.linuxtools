#!/usr/bin/python -t
import commands
import re
import entity
import utils
import logging
import os
import config
from sets import Set

SEVERYTY_NORMAL = 0;
SEVERYTY_HIGH = 1;
SEVERYTY = ["normal","high"]

class Bundle:

    def __init__(self, id, version, oldVersion, severity, desc, pkgName = ""):
        self.id = id
        self.version = version
        self.oldVersion = oldVersion
        self.severity = severity
        self.desc = desc
        
    def __str__(self): 
        return "Bundle id: " + self.id \
            + "\nNew version: " + self.version \
            + "\nCurrent version: " + self.oldVersion \
            + "\nSeverity: " + self.severity \
            + "\nDescription: " + self.desc

class Package:
    
    def __init__(self, packageName, refBundle = ""):
        self.packageName = packageName
        self.refBundle = refBundle
        self.bundles = []
    
    def __cmp__(self, other):
        return cmp(self.packageName, other.packageName)
        
    def __str__(self):
        str = "Package: %s \nBundles:\n" % self.packageName
        self.bundles = list(Set(self.bundles))
        for b in self.bundles:
            str += "%s %s => %s\n" % (b.id, b.oldVersion, b.version)
        return str 

class P2UpdateChecker:
    
    def __init__(self):
        logging.basicConfig(level=config.LOG_LEVEL,
                        format="%(asctime)s [%(levelname)s] %(message)s",
                        filename= "%s.log" % os.path.join(config.OUTPUT_DIR, config.APP_NAME),
                        filemode="aw")
        self.log = logging.getLogger(config.APP_NAME)
        console = logging.StreamHandler()
        console.setLevel(config.LOG_LEVEL)
        formatter = logging.Formatter('[%(levelname)s] %(message)s')
        console.setFormatter(formatter)
        logging.getLogger("").addHandler(console)
    
    def _getUpdateThroughP2(self, startupJar, debug):
        cmd = config.P2_UPDATE_CMD % startupJar  
        self.log.info("Get updates through P2: '%s'" % cmd)
        
        if not debug:
            updates = commands.getoutput(cmd)
        else:   
            updates = "org.python.pydev.templates - 1.3.22 - 0 - null\norg.python.pydev.test2 - 1.3.22 - 0 - null\norg.python.pydev.test3 - 1.3.22 - 0 - null";
        
        self.log.debug(updates)
        return updates.splitlines()
    
    def _getProvidesThroughOsgidepsScript(self, bundlesDirs, debug):
        cmd = config.OSGIDEPS_CMD % bundlesDirs
        self.log.info("Get provides through osgideps.pl script: '%s'" % cmd)
        if not debug:
            provides = commands.getoutput(cmd)
        else: 
            provides = "osgi(org.python.pydev.templates) = 1.3.18\nosgi(org.python.pydev.refactoring) = 1.3.18]"
        self.log.debug(provides)
        return provides.splitlines()
    
    def run(self, debug):
        lastStartupJar = ""
        lastBundlesDirs = ""
        
        for distRelease in config.DISTRIB.releases:
            self.log.info("TODO: init/update chroot for '%s'" %  distRelease.name)
            
            # we only run getUpdateThroughP2 when distRelease.startupJar have changes
            if distRelease.startupJar != lastStartupJar:
                updateList = self._getUpdateThroughP2(distRelease.startupJar, debug)
                lastStartupJar = distRelease.startupJar 
            else:
                self.log.info("get updates through P2 skipped.")
                
            # Compare provides only when there is updates available
            if len(updateList) > 2:
                # we only run getProvidesThroughOsgidepsScript when distRelease.bundlesDirs have changes
                if distRelease.bundlesDirs != lastBundlesDirs:
                    providelist = self._getProvidesThroughOsgidepsScript(distRelease.bundlesDirs, debug)
                    providelist = utils.removeDuplicate(providelist)
                    lastBundlesDirs = distRelease.bundlesDirs
                else:
                    self.log.info("get provides through osgideps.pl skipped.") 
    
                toUpdates = []
                output = ""
                for update in updateList:
                    try:
                        update = update.split('-')
                        currentUpdate = update[0].strip();
                        updateVersion = update[1].strip();
                        updateSeverity = update[2].strip();
                        if str(update[3]) == "null":
                            updateDescription = "no update description"
                        else:
                            updateDescription = str(update[3])
                    except:
                        currentUpdate = update
                    for provide in providelist:
                        try: 
                            m = re.search("(.*\()(.*)(\).*)", provide)
                            currentProvide = m.group(2)
                        except: 
                            currentProvide = ""
                            self.log.error("error getting bundle id for '%s' with RE: (.*\()(.*)(\).*)" % provide) 
                        try:
                            m = re.search("(.*[\ |\t|=]+)(.*)", provide)
                            provVersion = m.group(2)
                        except: 
                            provVersion = ""
                        # sanity check
                        if currentProvide == currentUpdate:
                            if updateSeverity != 0 or updateSeverity != 1:
                                updateSeverity = 0
                            toUpdates.append(Bundle(currentUpdate, updateVersion, provVersion, SEVERYTY[updateSeverity], updateDescription))
                
                toUpdates = utils.removeDuplicate(toUpdates)
                pkgsToUpdate = []
                for toUpdate in toUpdates:
                    if not debug:
                        status, pkgToUpdate = config.DISTRIB.getPackageName(toUpdate.id)
                    else:
                        status = 0 
                        pkgToUpdate = "eclipse-pydev"
                    pkgToUpdate = pkgToUpdate.strip()
                    if status != 0:
                        self.log.debug(pkgToUpdate)
                        continue
                    pkg = Package(pkgToUpdate)
                    if pkgsToUpdate.count(pkg) > 0:
                        pkg = pkgsToUpdate.pop(pkgsToUpdate.index(pkg))
                        pkg.bundles.append(toUpdate)   
                    else:
                        pkg.bundles.append(toUpdate) 
                    pkgsToUpdate.append(pkg)
                    self.log.info(pkg)
                     
                self.log.info("Run %s..." % config.PROCESS.DESCRIPTION)
                config.PROCESS.run(pkgsToUpdate, distRelease)
    
        self.log.info("Done.")
    

if __name__ == '__main__':
    P2UpdateChecker().run(config.APP_DEBUG)

