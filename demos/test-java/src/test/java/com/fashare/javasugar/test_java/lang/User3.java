package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Getter;
import com.fashare.javasugar.annotation.lang.Setter;

/**
 * 测试异常情况, 有 Runnable, 不去掉 abstract
 */
@SuppressWarnings("unused")
@Getter
@Setter
abstract class User3 implements User3$$IGetter, User3$$ISetter, Runnable {
    private String name = "fashare";
    private int id = 5;
}

/**
 * 子类实现了 Runnable, 去掉 abstract
 */
@SuppressWarnings("unused")
abstract class User4 extends User3 {
    @Override
    public void run() {
        // ignore
    }
}

/**
 * 子类实现了 Runnable, 但是自身有抽象方法, 不去掉 abstract
 */
@SuppressWarnings("unused")
abstract class User5 extends User3 {
    @Override
    public void run() {
        // ignore
    }

    abstract void test();
}
