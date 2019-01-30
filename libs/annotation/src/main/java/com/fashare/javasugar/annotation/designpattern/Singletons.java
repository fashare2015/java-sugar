package com.fashare.javasugar.annotation.designpattern;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class Singletons {
    private static HashMap<String, Method> sMethodCache = new HashMap<>();

    @SuppressWarnings({"unchecked"})
    public static <T> T get(Class<T> singletonClazz, Object... params) {
        try {
            Method method = getMethod(singletonClazz, "getInstance", params);
            if (method != null) {
                return (T) method.invoke(null, params);
            }
        } catch (Exception ignored) {
        }
//        return null;
        throw new IllegalArgumentException(singletonClazz.getSimpleName() + String.format(".getInstance(%s) not found", Arrays.toString(params)));
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Method getMethod(Class<T> singletonClazz, String methodName, Object... params) {
        StringBuilder sb = new StringBuilder(singletonClazz.getCanonicalName() + methodName);
        Class<?>[] classes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> clazz = params[i].getClass();
            sb.append(clazz.getCanonicalName());
            classes[i] = clazz;
        }
        String key = sb.toString();

        Method method = sMethodCache.get(key);
        if (method == null) {
            try {
                method = singletonClazz.getDeclaredMethod(methodName, classes);
                method.setAccessible(true);
            } catch (NoSuchMethodException ignored) {
            }

            if (method != null) {
                sMethodCache.put(key, method);
            }
        }

        return method;
    }
}
