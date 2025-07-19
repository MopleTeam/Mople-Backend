package com.mople.core.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.METHOD)
public @interface BusinessLogicLogging {}
