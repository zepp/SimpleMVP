/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation designates how to run presenter handlers
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MvpHandler {

    /**
     * if true, runs handler using mode otherwise in the main thread
     */
    boolean executor() default true;

    /**
     * if true, synchronize presenter before running handler
     */
    boolean sync() default true;
}
