/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Handling {
    boolean offload() default true;
}
