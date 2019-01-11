package com.fashare.javasuger;


import com.fashare.javasuger.annotation.Singleton;
import com.fashare.javasuger.annotation.Subject;

//@AstPrint
@Singleton
@Subject
public class MyEventBus {
    public interface Listener {
        void onEvent(String event);
    }
}
