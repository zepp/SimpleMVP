/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.common;

/**
 * This interface describes interface to be used by presenter to interact with MvpView
 *
 * @param <S> state type
 */
public interface MvpViewHandle<S extends MvpState> {

    /**
     * This method posts new state to parent view
     *
     * @param state
     */
    void post(S state);


    /**
     * This method terminates parent view
     */
    void finish();
}
