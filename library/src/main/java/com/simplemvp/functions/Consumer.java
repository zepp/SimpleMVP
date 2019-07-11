/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.functions;

@FunctionalInterface
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);
}
