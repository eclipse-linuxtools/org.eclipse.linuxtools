class Packager:
    
    def __init__(self, id, name, mail, dist, packages):
        self.id = id
        self.name = name
        self.mail = mail
        self.dist = dist
        self.packages = packages

class DistribRelease:
    
    def __init__(self, name, tag, startupJar, bundlesDirs, mockConfig):
        self.name = name
        self.tag = tag
        self.startupJar = startupJar 
        self.bundlesDirs = bundlesDirs
        self.mockConfig = mockConfig
        
    def __str__(self): 
        return "Name: " + self.name \
            + "\nTag: " + self.tag \
            + "\nStartup Jar: " + self.startupJar \
            + "\nBundles Directory: " + self.bundlesDirs \
            + "\nMock Config: " +  self.mockConfig


