package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.designpattern.Builder;
import com.fashare.javasugar.annotation.lang.Getter;
import com.fashare.javasugar.annotation.lang.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2019/1/15.
 */

@SuppressWarnings("unused")
@Builder
@Getter
@Setter
public abstract class User implements User$$IGetter, User$$ISetter {
    private String name = "fashare";
    private int id = 5;
    private List<String> phoneNums = Arrays.asList("10010", "10086");
    private Map<String, String> map = new HashMap<>();

    @Builder
    @Getter
    @Setter(returnThis = false)
    public static abstract class Phone implements User$$Phone$$IGetter, User$$Phone$$ISetter {
        private String os = "Android";
        private int price = 1234;
    }

    public static class NoAnnotation {
        private String name = "aaa";
    }
}
