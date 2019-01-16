package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.test_java.lang.User;
import com.fashare.javasugar.test_java.util.AssertUtil;
import com.fashare.javasugar.annotation.designpattern.Singletons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static com.fashare.javasugar.test_java.designpattern.UserManager.*;
import static org.junit.Assert.assertNull;

/**
 * Created by apple on 2019/1/16.
 */

public class TestSingleton {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getter_exist() {
        AssertUtil.assertMethodExist(true, UserManager.class, "getInstance");

        User user = Singletons.get(UserManager.class).mUser;
        assertEquals(user.getName(), "fashare");
    }

    @Test
    public void inner_getter_exist() {
        AssertUtil.assertMethodExist(true, PhoneManager.class, "getInstance");

        User.Phone phone = Singletons.get(PhoneManager.class).mPhone;
        assertEquals(phone.getOs(), "Android");
    }

    @Test
    public void inner_getter_not_exist() {
        AssertUtil.assertMethodExist(false, NoAnnotationManager.class, "getInstance");

        assertNull(Singletons.get(NoAnnotationManager.class));
    }
}
