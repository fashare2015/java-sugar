package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Observer;
import com.fashare.javasugar.annotation.designpattern.Subject;

/**
 * Created by apple on 2019/1/16.
 */

@Subject({
        @Observer(value = TitleBar.OnClickListener.class, name = "onClick"),
        @Observer(value = TitleBar.OnScrollListener.class, name = "onScroll"),
})
public class TitleBar {
    public interface OnClickListener {
        void onClick(TitleBar self);
    }

    public interface OnScrollListener {
        void onScroll(TitleBar self, int dy);
    }
}
