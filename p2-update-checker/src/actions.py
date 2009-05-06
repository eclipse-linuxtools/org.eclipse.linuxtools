import commands
import string
from time import gmtime, strftime
import sys, smtplib
import os
import re
import utils
import shutil
import tempfile
import config

"""
Actions implement a unique interface, Python don't support interfaces natively but
some API use internal implementation - maybe we can revamp this in a more Python way.

Interface Action:
    
    DESCRIPTION = # a short description
    
    def run(self, update, packager, distRelease):
        # return str(status)  
"""


class WriteSpecfile:
    """This action add a changelog entry, update Version and Release tag,
    and update some defines"""
      
    DESCRIPTION = "Write Specfile Action" 
    
    def run(self, update, packager, distRelease):
        result = self._applyTemplate(update, packager, distRelease)
        self.outputFile = os.path.join(
                config.OUTPUT_DIR, packager.dist.ID, 
                packager.name.replace(" ", "_"), distRelease.tag, update.packageName + ".spec")
        
        utils.writeFile(self.outputFile, result, 'w')
        return "done"
        
    def _applyTemplate(self, update, packager, distRelease):
        content = packager.dist.getSpecfileContent(update.packageName, distRelease.tag)
        p = re.compile('%changelog')
        part = p.split(content)
        specContent = part[0].strip()
        specChangelogEntries = part[1].strip()
        
        # FIXME: It could be better to have a "reference" bundle (the one that give the package version) 
        major, minor, micro, qualifier = utils.parseBundleVersion(update.bundles[0].version)
        
        try:
            m = re.search("Epoch:(\ | \t).*([0-9].*)", specContent, re.IGNORECASE)
            epoch = m.group(2).strip()
            if epoch != "":
                epoch += ":"
        except:
            epoch = ""

        m = re.search("Version:(\ |\t)+", specContent, re.IGNORECASE)
        g = m.group(0)           
        specContent = re.sub("Version:.*", g + update.bundles[0].version, specContent, re.IGNORECASE)   
        
        m = re.search("Release:(\ |\t)+", specContent, re.IGNORECASE)
        g = m.group(0)
        specContent = re.sub("Release:(\ |\b|)+([0-9]|\.|_|[a-z])+", g + "1", specContent, re.IGNORECASE)
        
        specContent = self._setDefineValue(specContent, "major", major)
        specContent = self._setDefineValue(specContent, "minor", minor)
        specContent = self._setDefineValue(specContent, "micro", micro)
        specContent = self._setDefineValue(specContent, "qualifier", qualifier)
        
        # used in eclipse-pydev.spec
        specContent = self._setDefineValue(specContent, "maint", micro)
        # used in eclipse.spec
        specContent = self._setDefineValue(specContent, "eclipse_major", major)
        specContent = self._setDefineValue(specContent, "eclipse_minor", minor)
        specContent = self._setDefineValue(specContent, "eclipse_micro", micro)
               
        template = "${SPEC_CONTENT}\n\n" \
                + "%changelog\n" \
                + "* ${DATE} ${PACKAGER} <${PACKAGER_MAIL}> ${EPOCH}${VERSION}-1\n" \
                + "- bump to ${VERSION}\n\n" \
                + "${CHANGELOG_ENTRIES}"
                
        result = string.Template(template).safe_substitute(
                    SPEC_CONTENT=specContent,
                    DATE=strftime("%a %b %d %Y", gmtime()), 
                    VERSION=major + "." + minor + "." + micro, 
                    EPOCH=epoch,
                    PACKAGER=packager.name, 
                    PACKAGER_MAIL=packager.mail,
                    CHANGELOG_ENTRIES=specChangelogEntries) 
        return result
    
    def _setDefineValue(self, specContent, define, value):
        try:
            exp = "%define(\ |\t)+" + define + "(\ |\t)+"
            m = re.search(exp, specContent, re.IGNORECASE) 
            g = m.group(0)
            return re.sub("%define(\ |\t)+"+ define + ".*", g + value, specContent, re.IGNORECASE)
        except:
            return specContent
    

class SendMail:
    """This action send email about new updates available
       upstream for they packages"""
    
    DESCRIPTION = "Send Mail Action"
    
    def run(self, update, packager, distRelease):
        content = ""
        
        # one mail by update
        reminderStr = packager.id + "-" + distRelease.tag + "-" + update.packageName \
                + "-" +  update.bundles[0].version  
        reminders = []
        try:
            if not os.path.exists(config.REMINDER_FILE):
                utils.writeFile(config.REMINDER_FILE, "# Remove this file if you will re-send mail alerts\n", "w")
            content = utils.readFile(config.REMINDER_FILE);
            reminders = content.splitlines()
        except:
            pass
        for r in reminders:
            if r == reminderStr:
                return "skipped"
        content += reminderStr + "\n"
        utils.writeFile(config.REMINDER_FILE, content, 'w')
        
        toaddrs  = packager.mail + "\n"
        subject = "Updates are available for %s \n" % update.packageName
        msg = "To: " + toaddrs + "\n"\
            + "From: " + config.FROM_ADRS + "\n" \
            + "Subject: " + subject + "\n" \
            + "PACKAGE INFO:\n" \
            + str(update) \
            + "\nRELEASE INFO:\n" \
            + str(distRelease)
        server = smtplib.SMTP(config.SMTP_HOSTNAME)
        server.sendmail(config.FROM_ADRS, toaddrs, msg)
        server.quit()
        return "done"


class FedoraMakeSRPM:
    """This action must be run after WriteSpecfile"""
    
    DESCRIPTION = "Make SRPM Action"
    
    def run(self, update, packager, distRelease):
        self._prepare(update, packager, distRelease)
        return "done"
    
    def _prepare(self, update, packager, distRelease):
        # create tmp directory
        currentDir = commands.getoutput("pwd");
        tmpDir = tempfile.mkdtemp("%s-%s_srpm" % (config.APP_NAME, update.packageName))
        os.chdir(tmpDir)
        cvsDir = "%s/%s" % (update.packageName, distRelease.tag)
        
        # get sources
        status = os.system("CVSROOT=:pserver:anonymous@cvs.fedoraproject.org:/cvs/pkgs cvs co %s" % cvsDir)
        if status != 0:
            raise
        
        # os.chdir(cvsDir)
        status = os.system("make -C %s" % cvsDir)
        if status != 0:
            raise
                
        major, minor, micro, qualifier = utils.parseBundleVersion(update.bundles[0].version)
        
        specWriter = WriteSpecfile()
        specWriter.run(update, packager, distRelease)
        specContent = utils.readFile(specWriter.outputFile) 
        try:
            m = re.search("source[0|:].*[\ |\t]+(.*)", specContent, re.IGNORECASE)
            src_url = m.group(1)
            src_url = src_url.replace("%{major}", major)
            src_url = src_url.replace("%{minor}", minor)
            src_url = src_url.replace("%{micro}", micro)   
            # fix eclipse-pydev define??         
            src_url = src_url.replace("%{maint}", micro) 
            status = os.system("wget %s" % src_url)
            if status != 0:
                raise
            status = os.system("make -C %s srpm" % cvsDir)
            if status != 0:
                raise
        except:
            # try to grab sources using fetch-* scripts??  
            raise
        os.chdir(currentDir)
       
