package com.fashare.javasugar.test_java.designpattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTitleBar {

    boolean onBackClickCalled;
    TitleBar titleBar = new TitleBar();
    TitleBar.OnClickListener onClickListener = new TitleBar.OnClickListener() {
        @Override
        public void onBackClick(TitleBar self) {
            onBackClickCalled = true;
        }
    };

    @Test
    public void register_and_notify() {
        titleBar.mOnClickListeners.clear();
        titleBar.mOnClickListeners.add(onClickListener);

        onBackClickCalled = false;
        titleBar.mOnClickListeners.asNotifier().onBackClick(titleBar);
        assertEquals(onBackClickCalled, true);
    }

    @Test
    public void unRegister_and_notify() {
        titleBar.mOnClickListeners.clear();

        onBackClickCalled = false;
        titleBar.mOnClickListeners.asNotifier().onBackClick(titleBar);
        assertEquals(onBackClickCalled, false);
    }
}
