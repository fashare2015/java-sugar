package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Getter;
import com.fashare.javasugar.annotation.lang.Setter;

@Getter
@Setter
public abstract class User2 implements User2$$IGetter, User2$$ISetter {
    private String name = "fashare";
    private int id = 5;

    @Override
    public String getName() {
        return "getName existed";
    }

    @Override
    public User2 setId(int id) {
        this.id = 999;
        return this;
    }
}
