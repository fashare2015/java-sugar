package com.fashare.javasuger.test_java.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.fashare.javasuger.test_java.util.AssertUtil.assertMethodExist;
import static org.junit.Assert.assertEquals;

/**
 * Created by apple on 2019/1/15.
 */

public class TestSetter {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void setter_exist() {
        assertMethodExist(true, User.class, "setName", String.class);
        assertMethodExist(true, User.class, "setId", int.class);
        assertMethodExist(true, User.class, "setPhoneNums", List.class);

        User user = new User()
                .setName("google")
                .setId(50)
                .setPhoneNums(Arrays.asList("10000"));

        assertEquals(user.getName(), "google");
        assertEquals(user.getId(), 50);
        assertEquals(user.getPhoneNums().toArray(new String[0]),
                Arrays.asList("10000").toArray(new String[0]));
    }

    @Test
    public void inner_getter_exist() {
        assertMethodExist(true, User.Phone.class, "setOs", String.class);
        assertMethodExist(true, User.Phone.class, "setPrice", int.class);

        User.Phone phone = new User.Phone()
                .setOs("ios")
                .setPrice(9999);

        assertEquals(phone.getOs(), "ios");
        assertEquals(phone.getPrice(), 9999);
    }

    @Test
    public void inner_getter_not_exist() {
        assertMethodExist(false, User.NoAnnotation.class, "setName", String.class);
    }
}
