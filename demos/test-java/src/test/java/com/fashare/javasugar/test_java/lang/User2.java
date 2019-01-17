package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Getter;
import com.fashare.javasugar.annotation.lang.Setter;

/**
 * 测试异常情况
 */
@Getter
@Setter
public abstract class User2 implements User2$$IGetter, User2$$ISetter {
    private String name = "fashare";
    private int id = 5;

    /**
     * 已存在 getName()
     */
    @Override
    public String getName() {
        return "getName existed";
    }

    /**
     * 已存在 setId()
     */
    @Override
    public User2 setId(int id) {
        this.id = 999;
        return this;
    }

    // 测试生成的类 User2$$IGetter 不会和内部类解糖后命名冲突
    public interface IGetter {
        String getName();
    }

    public interface ISetter {
        String getName();
    }
}
