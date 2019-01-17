package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.lang.Instances;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTitleBar {

    private boolean onClickCalled;
    private TitleBar titleBar = Instances.get(TitleBar.class);
    private TitleBar.OnClickListener onClickListener = new TitleBar.OnClickListener() {
        @Override
        public void onClick(TitleBar self) {
            onClickCalled = true;
        }
    };

    @Test
    public void register_and_notify() {
        titleBar.getClickSubject().clear();
        titleBar.getClickSubject().add(onClickListener);

        onClickCalled = false;
        titleBar.getClickSubject().asNotifier().onClick(titleBar);
        assertEquals(onClickCalled, true);
    }

    @Test
    public void unRegister_and_notify() {
        titleBar.getClickSubject().remove(onClickListener);

        onClickCalled = false;
        titleBar.getClickSubject().asNotifier().onClick(titleBar);
        assertEquals(onClickCalled, false);
    }
}
