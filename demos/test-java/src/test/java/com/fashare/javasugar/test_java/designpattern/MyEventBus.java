package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Observer;
import com.fashare.javasugar.annotation.designpattern.Singleton;
import com.fashare.javasugar.annotation.designpattern.Subject;

/**
 * Created by apple on 2019/1/16.
 */

@Singleton
@Subject({
        @Observer(value = MyEventBus.Listener.class, name = "Event"),
})
abstract class MyEventBus implements MyEventBus$$ISubject {
    public interface Listener<T> {
        void onEvent(T event);
    }
}
