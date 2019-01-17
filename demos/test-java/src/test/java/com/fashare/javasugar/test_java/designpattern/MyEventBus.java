package com.fashare.javasugar.test_java.designpattern;

/**
 * Created by apple on 2019/1/16.
 */

//@Singleton
//@Subject({
//        @Observer(MyEventBus.Listener.class)
//})
public abstract class MyEventBus {
    public interface Listener {
        void onEvent(Object event);
    }
}
