/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */

package com.simplemvp.common;

/**
 * This is an another functional interface that is close to the {@link Runnable} interface but
 * allows to throw exception on execution.
 */
@FunctionalInterface
public interface Executable {
    void execute() throws Exception;
}
