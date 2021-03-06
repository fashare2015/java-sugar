package com.fashare.javasugar.test_java.lang;

import com.fashare.javasugar.annotation.lang.Instances;
import com.fashare.javasugar.test_java.util.AssertUtil;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by apple on 2019/1/15.
 */

public class TestSetter {

    @Test
    public void setter_exist() {
        AssertUtil.assertMethodExist(true, User.class, "setName", String.class);
        AssertUtil.assertMethodExist(true, User.class, "setId", int.class);
        AssertUtil.assertMethodExist(true, User.class, "setPhoneNums", List.class);

//        User user = new User()
        User user = Instances.get(User.class)
                .setName("google")
                .setId(50)
                .setPhoneNums(Collections.singletonList("10000"));

        assertEquals(user.getName(), "google");
        assertEquals(user.getId(), 50);
        assertArrayEquals(user.getPhoneNums().toArray(new String[0]),
                Collections.singletonList("10000").toArray(new String[0]));
    }

    @Test
    public void inner_setter_exist() {
        AssertUtil.assertMethodExist(true, User.Phone.class, "setOs", String.class);
        AssertUtil.assertMethodExist(true, User.Phone.class, "setPrice", int.class);

//        User.Phone phone = new User.Phone()
        User.Phone phone = Instances.get(User.Phone.class);
        phone.setOs("ios");
        phone.setPrice(9999);

        assertEquals(phone.getOs(), "ios");
        assertEquals(phone.getPrice(), 9999);
    }

    @Test
    public void inner_setter_not_exist() {
        AssertUtil.assertMethodExist(false, User.NoAnnotation.class, "setName", String.class);
    }

    // User2
    @Test
    public void User2_setter_exist() {
        AssertUtil.assertMethodExist(true, User2.class, "setName", String.class);
        AssertUtil.assertMethodExist(true, User2.class, "setId", int.class);

        User2 user = Instances.get(User2.class)
                .setName("google")
                .setId(50);

        assertNotEquals(user.getId(), 50);
        assertEquals(user.getId(), 999);
    }

    // User3
    @Test
    public void User3_setter_exist() {
        AssertUtil.assertMethodExist(true, User3.class, "setName", String.class);
        AssertUtil.assertMethodExist(true, User3.class, "setId", int.class);

        User3 user = Instances.get(User4.class)
                .setName("google")
                .setId(50);

        assertEquals(user.getId(), 50);
    }
}
