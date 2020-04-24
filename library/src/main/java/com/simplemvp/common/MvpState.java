/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.common;

import androidx.annotation.NonNull;

/**
 * Base class that describes state of presenter & view
 */
public abstract class MvpState implements Cloneable {
    /**
     * revision number that represents how much time state has been cloned.
     */
    private int revision;

    /**
     * flag that indicates that state has been changed
     */
    private boolean isChanged = false;

    public final boolean isChanged() {
        return isChanged;
    }

    public final void setChanged(boolean isChanged) {
        this.isChanged |= isChanged;
    }

    public final void clearChanged() {
        this.isChanged = false;
    }

    public final boolean isInitial() {
        return revision == 0;
    }

    public final int getRevision() {
        return revision;
    }

    public void afterCommit() {
    }

    @Override
    @NonNull
    public synchronized MvpState clone() throws CloneNotSupportedException {
        MvpState state = (MvpState) super.clone();
        revision++;
        return state;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {" +
                "revision=" + revision +
                ", isChanged=" + isChanged +
                '}';
    }
}
