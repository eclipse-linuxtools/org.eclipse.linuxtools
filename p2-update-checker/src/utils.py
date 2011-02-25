import os
import logging

log = logging.getLogger()

def readFile(filename):
    """Read a given file"""
    try:
        fileHandle = open (filename, 'r')
        text = fileHandle.read()
        fileHandle.close()
    except OSError, (errno, strerror):
        log.error("'" + filename + "' " + strerror)
    return text

def writeFile(filename, textToWrite, mode):
    """Write a given string to file"""
    try:
        fileHandle = open (filename, mode)
        fileHandle.write(textToWrite)
        fileHandle.close()
    except IOError, (errno, strerror):
        try:
            os.system("mkdir -p " + os.path.dirname(filename))
            fileHandle = open (filename, 'w')
            fileHandle.write(textToWrite)
            fileHandle.close()
        except IOError, (errno, strerror):    
            log.error("'" + filename + "' " + strerror)

def removeDuplicate(list):
    """remove duplicate from a given list"""
    keys = {}
    for i in list:
        keys[i] = 1
    return keys.keys()  

def parseBundleVersion(version):
    """parse OSGI version"""
    version = version.split(".")
    major = version[0]
    minor = version[1]
    micro = version[2]
    try:
        qualifier = version[4]
    except:
        qualifier = ""
    return major, minor, micro, qualifier