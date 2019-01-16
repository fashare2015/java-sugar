package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Getter;
import com.fashare.javasugar.annotation.lang.Setter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by apple on 2019/1/15.
 */

@Setter
@Getter
public class User {
    private String name = "fashare";
    private int id = 5;
    private List<String> phoneNums = Arrays.asList("10010", "10086");

    @Setter
    @Getter
    public static class Phone {
        private String os = "Android";
        private int price = 1234;
    }

    public static class NoAnnotation {
        private String name = "aaa";
    }
}
