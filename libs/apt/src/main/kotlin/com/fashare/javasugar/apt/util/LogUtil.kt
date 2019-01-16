package com.fashare.javasugar.apt.util

import javax.tools.Diagnostic

/**
 * Created by apple on 2019/1/10.
 */
fun Any.logd(msg: String) {
    EnvUtil.messager.printMessage(Diagnostic.Kind.NOTE, "${this.javaClass.simpleName}: $msg")
}

fun Any.loge(msg: String) {
    EnvUtil.messager.printMessage(Diagnostic.Kind.ERROR, "${this.javaClass.simpleName}: $msg")
}
