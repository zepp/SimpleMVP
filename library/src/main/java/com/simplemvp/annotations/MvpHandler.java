/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
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
     * if true run handler using {@link java.util.concurrent.ExecutorService} otherwise
     * invoke handler directly on the main thread.
     */
    boolean executor() default true;
}
