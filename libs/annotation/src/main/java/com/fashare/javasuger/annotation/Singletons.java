package com.fashare.javasuger.annotation;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Singletons {
    private static HashMap<String, Method> sMethodCache = new HashMap<>();

    @SuppressWarnings({"unchecked"})
    public static <T> T get(Class<T> singletonClazz) {
        try {
            Method method = getMethod(singletonClazz, "getInstance");
            if (method != null) {
                return (T) method.invoke(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static <T> Method getMethod(Class<T> singletonClazz, String methodName) {
        String key = singletonClazz.getCanonicalName() + methodName;
        Method method = sMethodCache.get(key);
        if (method == null) {
            try {
                method = singletonClazz.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException ignored) {
            }

            if (method != null) {
                sMethodCache.put(key, method);
            }
        }

        return method;
    }
}
