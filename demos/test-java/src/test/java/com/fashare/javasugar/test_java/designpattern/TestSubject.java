package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singletons;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by apple on 2019/1/16.
 */

public class TestSubject {
    MyEventBus mEventBus;

    MyEventBus.Listener mListener = new MyEventBus.Listener(){
        @Override
        public void onEvent(Object event) {
            data = ((String) event);
        }
    };

    String data = "";

    @Before
    public void init() {
        mEventBus = Singletons.get(MyEventBus.class);
        mEventBus.add(mListener);
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void register_and_notify() {
        mEventBus.notify("data");
        assertEquals(data, "data");

        mEventBus.notify("data 2");
        assertEquals(data, "data 2");
    }

    @Test
    public void unRegister_and_notify() {
        String curData = data;
        mEventBus.remove(mListener);
        mEventBus.notify("data changed");
        assertNotEquals(data, "data changed");
        assertEquals(data, curData);
    }
}
