
PACKAGERS = []
PACKAGERS.append(entity.Packager("userid", "username", "mail", DISTRIB, "eclipse-pydev"))

"""
A Simple process implementation
"""
class ExampleProcess:
    
    DESCRIPTION = "Example process"
    
    def run(self, pkgsToUpdate, distRelease):
        for pkgToUpdate in pkgsToUpdate:
            for packager in PACKAGERS:
                packagerPackages = packager.packages.split(" ");
                for pkg in packagerPackages:
                    if pkg.strip() == pkgToUpdate.packageName.strip():
                        for action in ACTIONS: 
                            actionOutput = action.run(pkgToUpdate, packager, distRelease)
                            msg = "%s (%s) for %s [%s]" % (action.DESCRIPTION, pkgToUpdate.packageName, packager.name, actionOutput)
                            log.info(msg)
                            
class ExampleDistrib:
    
    ID = "Example"
    NAME = "Example Distrib"
    
    def __init__(self, releases):
        self.releases = releases
    
    def getPackageName(self, update_id):
        pass 
    
    def getSpecfileContent(self, pkgName, releaseTag):
        """Implement this fct only if you want to use actions.WriteSpecfile()"""
        pass 
    
    def makeChroot(self, distRelease):
        pass
