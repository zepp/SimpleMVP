/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */
package com.simplemvp.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@interface Proxify {
    /**
     * Run method on main thread looper
     */
    boolean looper() default true;

    /**
     * Check that view is alive and throw error otherwise
     */
    boolean alive() default true;
}
