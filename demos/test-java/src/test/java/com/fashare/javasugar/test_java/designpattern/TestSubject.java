package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singletons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by apple on 2019/1/16.
 */

public class TestSubject {

    private String data = "";
    private MyEventBus mEventBus = Singletons.get(MyEventBus.class);
    private MyEventBus.Listener<String> mListener = new MyEventBus.Listener<String>(){
        @Override
        public void onEvent(String event) {
            data = event;
        }
    };

    @Test
    public void register_and_notify() {
        mEventBus.getEventSubject().clear();
        mEventBus.getEventSubject().add(mListener);
        mEventBus.getEventSubject().asNotifier().onEvent("data");
        assertEquals(data, "data");

        mEventBus.getEventSubject().asNotifier().onEvent("data 2");
        assertEquals(data, "data 2");
    }

    @Test
    public void unRegister_and_notify() {
        String curData = data;
        mEventBus.getEventSubject().clear();
        mEventBus.getEventSubject().add(mListener);
        mEventBus.getEventSubject().remove(mListener);
        mEventBus.getEventSubject().asNotifier().onEvent("data changed");
        assertNotEquals(data, "data changed");
        assertEquals(data, curData);
    }
}
