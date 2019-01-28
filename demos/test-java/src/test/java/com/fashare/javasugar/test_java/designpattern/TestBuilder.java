package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.test_java.lang.User;
import com.fashare.javasugar.test_java.lang.User$$Builder;
import com.fashare.javasugar.test_java.lang.User$$Phone$$Builder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by apple on 2019/1/15.
 */

public class TestBuilder {

    @Test
    public void builder() {
//        User user = new User();
        User user = new User$$Builder()
                .build();
        assertNull(user.getName());
        assertEquals(user.getId(), 0);
        assertNull(user.getPhoneNums());
        assertNull(user.getMap());

        user = new User$$Builder()
                .setName("name by builder")
                .setId(55)
                .build();
        assertEquals(user.getName(), "name by builder");
        assertEquals(user.getId(), 55);
    }

    @Test
    public void inner_builder() {
//        User.Phone phone = new User.Phone();
        User.Phone phone = new User$$Phone$$Builder()
                .build();
        assertNull(phone.getOs());
        assertEquals(phone.getPrice(), 0);

        phone = new User$$Phone$$Builder()
                .setOs("ios")
                .setPrice(9999)
                .build();
        assertEquals(phone.getOs(), "ios");
        assertEquals(phone.getPrice(), 9999);
    }
}
