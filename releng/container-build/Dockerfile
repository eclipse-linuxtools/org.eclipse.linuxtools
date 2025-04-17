FROM eclipsecbi/fedora-gtk3-mutter:42-gtk3.24

# Back to root for install
USER 0

# Permit installation of docs
RUN sed -i '/tsflags=nodocs/ d' /etc/dnf/dnf.conf

RUN dnf -y update && dnf -y install \
    webkit2gtk3 npm tigervnc-server java-21-openjdk-devel \
    gcc-c++ perf systemtap rpmdevtools valgrind \
    gtk3-devel-docs gnome-shell ibus \
    && dnf clean all
    
RUN curl -L https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xzv 

ENV PATH=/apache-maven-3.9.9/bin:/usr/lib/jvm/java-21/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-21
ENV NO_AT_BRIDGE=1

#Back to named user
USER 10001
