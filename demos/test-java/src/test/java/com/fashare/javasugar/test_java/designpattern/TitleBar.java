package com.fashare.javasugar.test_java.designpattern;

import com.fashare.javasugar.annotation.designpattern.Observer;
import com.fashare.javasugar.annotation.designpattern.Subject;

import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.functions.Function9;

/**
 * Created by apple on 2019/1/16.
 */

@Subject({
        @Observer(value = TitleBar.OnClickListener.class, name = "Click"),
        @Observer(value = TitleBar.OnScrollListener.class, name = "Scroll"),
        @Observer(value = Runnable.class, name = "Runnable"),
        @Observer(value = Callable.class, name = "Callable"),
        @Observer(value = Map.class, name = "Map"),
        @Observer(value = Function9.class, name = "Function9"),
})
abstract class TitleBar implements TitleBar$$ISubject {
    public interface OnClickListener {
        void onClick(TitleBar self);
    }

    @SuppressWarnings("unused")
    public interface OnScrollListener {
        void onScrolled(TitleBar self, int dy);

        void onScrollChange(TitleBar self, int scrollState);
    }
}
