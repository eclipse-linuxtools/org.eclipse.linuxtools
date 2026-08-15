FROM eclipsecbi/fedora-gtk3-mutter:43-gtk3.24

# Back to root for install
USER 0

# Permit installation of docs
RUN sed -i '/tsflags=nodocs/ d' /etc/dnf/dnf.conf

RUN dnf -y update && dnf -y install \
    webkit2gtk4.1 npm tigervnc-server java-21-openjdk-devel \
    gcc-c++ perf systemtap rpmdevtools valgrind \
    gtk3-devel-docs gnome-shell ibus \
    && dnf clean all
    
# gdk-pixbuf 2.44+ decodes images through glycin, which runs every loader in a
# bwrap sandbox. That sandbox cannot be set up inside a container, so GTK3 aborts
# on the first icon load. Wrap bwrap to run glycin loaders directly.
COPY glycin-bwrap-wrapper.sh /usr/local/bin/glycin-bwrap-wrapper.sh
RUN mv /usr/bin/bwrap /usr/bin/bwrap.real \
    && install -m 0755 /usr/local/bin/glycin-bwrap-wrapper.sh /usr/bin/bwrap

RUN curl -L https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz | tar -xzv

ENV PATH=/apache-maven-3.9.16/bin:/usr/lib/jvm/java-21/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-21
ENV NO_AT_BRIDGE=1

#Back to named user
USER 10001
