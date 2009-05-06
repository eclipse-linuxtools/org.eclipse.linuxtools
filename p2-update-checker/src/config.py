import distFedora
import actions
import entity
import logging

APP_NAME = "p2UpdateChecker"
APP_DEBUG = False

LOG = logging.getLogger(APP_NAME)
LOG_LEVEL = logging.DEBUG

P2_UPDATE_CMD = "java -cp %s org.eclipse.core.launcher.Main -application  org.eclipse.equinox.p2.director.app.application -updatelist | uniq"
OSGIDEPS_CMD = "find %s -type f | egrep '*.jar|*.MF' | /usr/lib/rpm/osgideps.pl -p | uniq"

OUTPUT_DIR = "output"

# Send Mail action configuration
REMINDER_FILE = OUTPUT_DIR + "/alert_reminder.log"
FROM_ADRS = "noreply@fedoraproject.org"
SMTP_HOSTNAME= "localhost"

# CHROOT="/var/lib/mock/fedora-rawhide-i386/root"
CHROOT = ""
RELEASES = []
RELEASES.append(entity.DistribRelease(
    "rawhide", 
    "devel", 
    "%s/usr/lib64/eclipse/startup.jar" % CHROOT, 
    "%s/usr/share/eclipse/dropins/ %s/usr/lib64/eclipse/dropins/" % (CHROOT, CHROOT),
    "fedora-rawhide-i386"))

# Distrib config
DISTRIB = distFedora.Distrib(RELEASES)
PROCESS = distFedora.Process()
ACTIONS = [actions.WriteSpecfile(), actions.SendMail()]
