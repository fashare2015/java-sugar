package com.fashare.javasuger;

import com.fashare.javasuger.annotation.designpattern.Subject;


@Subject
//@Singleton
public abstract class MyEventBus implements Subject.Stub {
    public interface Listener {
        void onEvent(Object event);
    }

    public static MyEventBus getInstance() {
        try {
            return MyEventBus.class.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private Set mList = new HashSet();
//
//    public void setA(Object aa){
//        this.mList.add(aa);
//    }

//    List list = new ArrayList();
//
//    private void a() {
//        for (Object item : list) {
//            ((Listener) item).onEvent("AAA");
//        }
//    }
}
