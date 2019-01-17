package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.ISubject;

/**
 * Created by apple on 2019/1/16.
 */

public class TitleBar {
    public interface OnClickListener {
        void onBackClick(TitleBar self);
    }

    ISubject<OnClickListener> mOnClickListeners = new ISubject.Stub<OnClickListener>() {
    };
}
