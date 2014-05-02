/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.daemon;

import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;

/**
 * This class represents an event used to configure the OProfile
 * daemon.
 */
public class OprofileDaemonEvent {
    public static final int COUNT_UNINITIALIZED = 0;
    public static final int COUNT_INVALID = -1;

    /**
     *  The event to collect on this counter
     */
    private OpEvent event;

    /**
     *  Boolean variable to enable/disable Profile kernel
     */
    private boolean profileKernel;

    /**
     *  Boolean variable to enable/disable Profile userspace
     */
    private boolean profileUser;

    /**
     *  Reset counter value
     */
    private int count;

    public OprofileDaemonEvent() {
        profileKernel = true;
        profileUser = true;
        count = COUNT_UNINITIALIZED;
        event = null;
    }

    /**
     * Set the event to collect
     * @param event the OProfile event
     */
    public void setEvent(OpEvent event) {
        this.event = event;
    }

    /**
     * Get the event to collect
     * @returns the OProfile event
     */
    public OpEvent getEvent() {
        return event;
    }

    /**
     * Set whether to profile the kernel
     * @param profileKernel whether to enable kernel profiling
     */
    public void setProfileKernel(boolean profileKernel) {
        this.profileKernel = profileKernel;
    }

    /**
     * Get whether to profile the kernel
     * @return whether to profile the kernel
     */
    public boolean getProfileKernel() {
        return profileKernel;
    }

    /**
     * Set whether to profile userspace
     * @param profileUser whether to profile userspace
     */
    public void setProfileUser(boolean profileUser) {
        this.profileUser = profileUser;
    }

    /**
     * Get whether to profile userspace
     * @return whether to profile userspace
     */
    public boolean getProfileUser() {
        return profileUser;
    }

    /**
     * Set the reset count
     * @param count the new count
     */
    public void setResetCount(int count) {
        this.count = count;
    }

    /**
     * Get the reset count
     * @return the reset count
     */
    public int getResetCount() {
        // FIXME: This isn't quite in the right place...
        if (count == COUNT_UNINITIALIZED) {
            // This is what Oprofile does in oprof_start.cpp:
            double speed = Oprofile.getCpuFrequency();
            if (speed == 0.0) {
                count = event.getMinCount() * 30;
            } else {
                count = (int) speed * 20;
            }
        }

        return count;
    }
}
