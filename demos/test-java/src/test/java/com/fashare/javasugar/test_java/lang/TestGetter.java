package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Instances;
import com.fashare.javasugar.test_java.util.AssertUtil;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by apple on 2019/1/15.
 */

public class TestGetter {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getter_exist() {
        AssertUtil.assertMethodExist(true, User.class, "getName");
        AssertUtil.assertMethodExist(true, User.class, "getId");
        AssertUtil.assertMethodExist(true, User.class, "getPhoneNums");

//        User user = new User();
        User user = Instances.get(User.class);
        assertEquals(user.getName(), "fashare");
        assertEquals(user.getId(), 5);
        assertEquals(user.getPhoneNums().toArray(new String[0]),
                Arrays.asList("10010", "10086").toArray(new String[0]));
    }

    @Test
    public void inner_getter_exist() {
        AssertUtil.assertMethodExist(true, User.Phone.class, "getOs");
        AssertUtil.assertMethodExist(true, User.Phone.class, "getPrice");

//        User.Phone phone = new User.Phone();
        User.Phone phone = Instances.get(User.Phone.class);
        assertEquals(phone.getOs(), "Android");
        assertEquals(phone.getPrice(), 1234);
    }

    @Test
    public void inner_getter_not_exist() {
        AssertUtil.assertMethodExist(false, User.NoAnnotation.class, "getName");
    }
}
