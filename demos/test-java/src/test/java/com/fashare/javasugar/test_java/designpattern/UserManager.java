package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singleton;
import com.fashare.javasugar.annotation.lang.Instances;
import com.fashare.javasugar.test_java.lang.User;

/**
 * Created by apple on 2019/1/16.
 */

@Singleton
class UserManager {
    User mUser = Instances.get(User.class);

    @Singleton
    static class PhoneManager {
        User.Phone mPhone = Instances.get(User.Phone.class);
    }

    static class NoAnnotationManager {
        User.NoAnnotation mNoAnnotation = new User.NoAnnotation();
    }
}
