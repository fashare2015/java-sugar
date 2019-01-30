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
