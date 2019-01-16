package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Singleton;
import com.fashare.javasugar.annotation.designpattern.Subject;

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
