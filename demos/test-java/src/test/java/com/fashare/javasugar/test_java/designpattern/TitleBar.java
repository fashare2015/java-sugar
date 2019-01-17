package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Observer;
import com.fashare.javasugar.annotation.designpattern.Subject;

/**
 * Created by apple on 2019/1/16.
 */

@Subject({
        @Observer(value = TitleBar.OnClickListener.class, name = "Click"),
        @Observer(value = TitleBar.OnScrollListener.class, name = "Scroll"),
        @Observer(value = Runnable.class, name = "Runnable"),
})
abstract class TitleBar implements TitleBar$$ISubject {
    public interface OnClickListener {
        void onClick(TitleBar self);
    }

    public interface OnScrollListener {
        void onScrolled(TitleBar self, int dy);

        void onScrollChange(TitleBar self, int scrollState);
    }
}
