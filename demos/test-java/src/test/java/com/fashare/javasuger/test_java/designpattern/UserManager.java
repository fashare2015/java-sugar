package com.fashare.javasuger.test_java.designpattern;

import com.fashare.javasuger.annotation.designpattern.Singleton;
import com.fashare.javasuger.test_java.lang.User;

/**
 * Created by apple on 2019/1/16.
 */

@Singleton
class UserManager {
    User mUser = new User();

    @Singleton
    static class PhoneManager {
        User.Phone mPhone = new User.Phone();
    }

    static class NoAnnotationManager {
        User.NoAnnotation mNoAnnotation = new User.NoAnnotation();
    }
}
