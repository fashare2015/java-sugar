package com.fashare.javasugar.annotation.lang;

public class Instances {

    @SuppressWarnings({"unchecked"})
    public static <T> T get(Class<T> singletonClazz) {
        try {
            return singletonClazz.newInstance();
        } catch (Exception ignored) {
        }
        return null;
    }
}
