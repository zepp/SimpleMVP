/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

interface DisposableListener {
    /**
     * disposes the listener and disconnects it from view
     */
    void dispose();
}
