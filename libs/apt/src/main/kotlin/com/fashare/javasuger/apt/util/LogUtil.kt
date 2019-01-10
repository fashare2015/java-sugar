package com.fashare.javasuger.apt.util

import javax.tools.Diagnostic

/**
 * Created by apple on 2019/1/10.
 */
//object LogUtil {
    fun Any.logd(msg: String) {
        EnvUtil.mMessager.printMessage(Diagnostic.Kind.NOTE, "${this.javaClass.simpleName}: $msg")
    }

    fun Any.loge(msg: String) {
        EnvUtil.mMessager.printMessage(Diagnostic.Kind.ERROR, "${this.javaClass.simpleName}: $msg")
    }
//}