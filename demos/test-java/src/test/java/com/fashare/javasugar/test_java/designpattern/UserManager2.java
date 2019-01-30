package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singleton;
import com.fashare.javasugar.annotation.lang.Instances;
import com.fashare.javasugar.test_java.lang.User;

/**
 * Created by apple on 2019/1/16.
 */

@Singleton
class UserManager2 {
    User mUser = Instances.get(User.class);

    private static final UserManager2 sInstance = new UserManager2();

    /**
     * test case: getInstance() 已存在
     */
    @SuppressWarnings("unused")
    public static UserManager2 getInstance() {
        return sInstance;
    }
}

/**
 * test case: getInstance(User) 带参数
 */
@Singleton
class UserManager3 {
    User mUser;

    @Singleton.Main
    private UserManager3(User user) {
        mUser = user;
    }
}

/**
 * test case: getInstance(a, b, c ..) 多参数
 */
@SuppressWarnings("unused")
@Singleton
class UserManager4 {
    @SuppressWarnings("FieldCanBeLocal")
    private User mUser;

    @Singleton.Main
    private UserManager4(User user,
                         String a1,
                         String a2,
                         String a3,
                         String a4,
                         String a5,
                         String a6) {
        mUser = user;
    }
}
