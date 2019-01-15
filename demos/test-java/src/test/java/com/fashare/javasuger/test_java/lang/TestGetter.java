package com.fashare.javasuger.test_java.lang;

import org.junit.Test;

import java.util.Arrays;

import static com.fashare.javasuger.test_java.util.AssertUtil.assertMethodExist;
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
        assertMethodExist(true, User.class, "getName");
        assertMethodExist(true, User.class, "getId");
        assertMethodExist(true, User.class, "getPhoneNums");

        User user = new User();
        assertEquals(user.getName(), "fashare");
        assertEquals(user.getId(), 5);
        assertEquals(user.getPhoneNums().toArray(new String[0]),
                Arrays.asList("10010", "10086").toArray(new String[0]));
    }

    @Test
    public void inner_getter_exist() {
        assertMethodExist(true, User.Phone.class, "getOs");
        assertMethodExist(true, User.Phone.class, "getPrice");

        User.Phone phone = new User.Phone();
        assertEquals(phone.getOs(), "Android");
        assertEquals(phone.getPrice(), 1234);
    }

    @Test
    public void inner_getter_not_exist() {
        assertMethodExist(false, User.NoAnnotation.class, "getName");
    }
}
