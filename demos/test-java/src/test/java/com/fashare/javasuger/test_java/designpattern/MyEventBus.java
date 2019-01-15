package com.fashare.javasuger.test_java.designpattern;

import com.fashare.javasuger.annotation.designpattern.Singleton;
import com.fashare.javasuger.annotation.designpattern.Subject;

/**
 * Created by apple on 2019/1/16.
 */

@Singleton
@Subject
public abstract class MyEventBus implements Subject.Stub {
    public interface Listener {
        void onEvent(Object event);
    }
}
