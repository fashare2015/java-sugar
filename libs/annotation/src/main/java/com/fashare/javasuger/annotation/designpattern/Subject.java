package com.fashare.javasuger.annotation.designpattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Subject {
    /**
     * class of observer
     */
//    Class<?> value();

    /**
     * 类似 aidl 里的 Stub
     */
    interface Stub {
//        void add();
        void add(Object observer);

        void remove(Object observer);

        void notify(Object event);
    }
}
