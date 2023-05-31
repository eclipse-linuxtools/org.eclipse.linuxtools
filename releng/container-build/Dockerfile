FROM eclipsecbi/fedora-gtk3-mutter:38-gtk3.24

# Back to root for install
USER 0

ARG REMOTING_VERSION=3.27

# https://github.com/jenkinsci/docker-jnlp-slave/
ARG JNLP_AGENT_SCRIPT_VERSION=3.27-1

RUN curl --create-dirs -sSLo /usr/share/jenkins/slave.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${REMOTING_VERSION}/remoting-${REMOTING_VERSION}.jar \
  && chmod 755 /usr/share/jenkins \
  && chmod 644 /usr/share/jenkins/slave.jar

ADD https://raw.githubusercontent.com/jenkinsci/docker-jnlp-slave/${JNLP_AGENT_SCRIPT_VERSION}/jenkins-slave /usr/local/bin/jenkins-slave
RUN chmod 555 /usr/local/bin/jenkins-slave && \
  chgrp 0 /usr/local/bin/jenkins-slave && \
  mkdir -p /home/jenkins/.jenkins && \
  mkdir -p /home/jenkins/agent

ENTRYPOINT [ "uid_entrypoint", "jenkins-slave" ]

# Permit installation of docs
RUN sed -i '/tsflags=nodocs/ d' /etc/dnf/dnf.conf

RUN dnf -y update && dnf -y install \
    webkit2gtk3 npm \
    gcc-c++ perf systemtap rpmdevtools valgrind \
    gtk3-devel-docs createrepo_c \
    && dnf clean all

#Back to named user
USER 1000

ENV JENKINS_AGENT_WORKDIR=${HOME}/agent
ENV JNLP_PROTOCOL_OPTS="-XshowSettings:vm -Xmx256m -Djdk.nativeCrypto=false -Dsun.zip.disableMemoryMapping=true -Dorg.jenkinsci.remoting.engine.JnlpProtocol3.disabled=true"

