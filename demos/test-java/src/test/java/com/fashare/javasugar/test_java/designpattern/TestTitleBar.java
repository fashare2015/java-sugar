package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.lang.Instances;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTitleBar {

    private boolean onBackClickCalled;
    private TitleBar titleBar = Instances.get(TitleBar.class);
    private TitleBar.OnClickListener onClickListener = new TitleBar.OnClickListener() {
        @Override
        public void onClick(TitleBar self) {
            onBackClickCalled = true;
        }
    };

    @Test
    public void register_and_notify() {
        titleBar._mObserverList.clear();
        titleBar._mObserverList.add(onClickListener);

        onBackClickCalled = false;
        titleBar._mObserverList.asNotifier().onClick(titleBar);
        assertEquals(onBackClickCalled, true);
    }

    @Test
    public void unRegister_and_notify() {
        titleBar._mObserverList.clear();

        onBackClickCalled = false;
        titleBar._mObserverList.asNotifier().onClick(titleBar);
        assertEquals(onBackClickCalled, false);
    }
}
