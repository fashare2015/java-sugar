package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singletons;
import com.fashare.javasugar.annotation.lang.Instances;
import com.fashare.javasugar.test_java.lang.User;
import com.fashare.javasugar.test_java.util.AssertUtil;

import org.junit.Test;

import static com.fashare.javasugar.test_java.designpattern.UserManager.NoAnnotationManager;
import static com.fashare.javasugar.test_java.designpattern.UserManager.PhoneManager;
import static org.junit.Assert.assertEquals;

/**
 * Created by apple on 2019/1/16.
 */

public class TestSingleton {

    @Test
    public void getter_exist() {
        AssertUtil.assertMethodExist(true, UserManager.class, "getInstance");

        User user = Singletons.get(UserManager.class).mUser;
        assertEquals(user.getName(), "fashare");
        assertEquals(Singletons.get(UserManager.class) == Singletons.get(UserManager.class), true);
    }

    @Test
    public void inner_getter_exist() {
        AssertUtil.assertMethodExist(true, PhoneManager.class, "getInstance");

        User.Phone phone = Singletons.get(PhoneManager.class).mPhone;
        assertEquals(phone.getOs(), "Android");
        assertEquals(Singletons.get(PhoneManager.class) == Singletons.get(PhoneManager.class), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inner_getter_not_exist() {
        AssertUtil.assertMethodExist(false, NoAnnotationManager.class, "getInstance");

        Singletons.get(NoAnnotationManager.class);
    }

    @Test
    public void UserManager2_getter_exist() {
        AssertUtil.assertMethodExist(true, UserManager2.class, "getInstance");

        User user = Singletons.get(UserManager2.class).mUser;
        assertEquals(user.getName(), "fashare");
        assertEquals(Singletons.get(UserManager2.class) == Singletons.get(UserManager2.class), true);
    }

    @Test
    public void UserManager3_getter_exist() {
        AssertUtil.assertMethodExist(true, UserManager3.class, "getInstance", User.class);

        User outUser = Instances.get(User.class);
        User user = Singletons.get(UserManager3.class, outUser).mUser;

        assertEquals(user == outUser, true);
        assertEquals(user.getName(), "fashare");
        assertEquals(Singletons.get(UserManager3.class, outUser) == Singletons.get(UserManager3.class, outUser), true);
    }
}
