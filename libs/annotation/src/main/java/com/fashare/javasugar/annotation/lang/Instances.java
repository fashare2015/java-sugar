package com.fashare.javasugar.annotation.lang;

import java.lang.reflect.Constructor;

public class Instances {

    @SuppressWarnings({"unchecked"})
    public static <T> T get(Class<T> singletonClazz) {
        try {
            Constructor<T> constructor = singletonClazz.getDeclaredConstructor();
            constructor.setAccessible(true);    // 防止 package/privete 调用 newInstance() 失败
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(singletonClazz.getSimpleName() + ".<init>() not found", e);
        }
//        return null;
    }
}
