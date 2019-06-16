/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

public abstract class MvpState implements Cloneable {
    // исходное состояние, которое еще не было отображено
    private boolean isInitial = true;

    // состояние изменено
    private boolean isChanged = false;

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean isChanged) {
        this.isChanged |= isChanged;
    }

    void clearChanged() {
        this.isChanged = false;
    }

    public boolean isInitial() {
        return isInitial;
    }

    void clearInitial() {
        isInitial = false;
    }

    @Override
    protected MvpState clone() throws CloneNotSupportedException {
        return (MvpState) super.clone();
    }

    @Override
    public String toString() {
        return "MvpState { " + "initial: " + isInitial + ", changed: " + isChanged + '}';
    }
}
