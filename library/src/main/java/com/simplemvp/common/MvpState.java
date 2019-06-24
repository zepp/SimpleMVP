/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

/**
 * Base class that describes state of presenter & view
 */
public abstract class MvpState implements Cloneable {
    /**
     * revision number of the state
     */
    private int revision;

    /**
     * flag that indicates that object is changed
     */
    private boolean isChanged = false;

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean isChanged) {
        this.isChanged |= isChanged;
    }

    public void clearChanged() {
        this.isChanged = false;
    }

    public boolean isInitial() {
        return revision == 0;
    }

    @Override
    public synchronized MvpState clone() throws CloneNotSupportedException {
        MvpState state = (MvpState) super.clone();
        revision++;
        return state;
    }


}
