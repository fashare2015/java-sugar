package com.fashare.javasuger.annotation;

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
    public interface Stub<T> {
        void add(T observer);

        void remove(T observer);

        void notifyObservers(Object event);
    }
}
