#!/bin/bash
# Run a command under a mutter window manager.
#
# mutter 50+ dropped its X11 window manager mode (--replace on an existing X
# server such as Xvnc), so start it as a headless Wayland compositor instead.
# It provides Xwayland on DISPLAY and manages its X11 clients, and GTK is forced
# to the X11 backend because SWTBot needs X11.
#
# Usage: with-mutter COMMAND [ARGUMENT...]
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-$(mktemp -d /tmp/xdg-runtime-XXXXXX)}"
chmod 700 "$XDG_RUNTIME_DIR"
export GDK_BACKEND=x11
exec dbus-run-session -- mutter --wayland --headless \
    --virtual-monitor "${MUTTER_MONITOR:-1440x900}" -- "$@"
