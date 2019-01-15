package com.fashare.javasuger.test_java.util;

import static org.junit.Assert.assertEquals;

/**
 * Created by apple on 2019/1/15.
 */

public class AssertUtil {
    public static void assertMethodExist(boolean isExist, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        boolean isMethodExist = true;
        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            isMethodExist = false;
        }
        assertEquals(isExist, isMethodExist);
    }
}
