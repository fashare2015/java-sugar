package com.fashare.javasugar.apt.util

/**
 * Created by apple on 2019/1/17.
 */
fun String.upperFirst(): String {
    val str = this
    return if (!str.isEmpty())
        str.substring(0, 1).toUpperCase() + str.substring(1, str.length)
    else
        str
}

fun String.lowerFirst(): String {
    val str = this
    return if (!str.isEmpty())
        str.substring(0, 1).toLowerCase() + str.substring(1, str.length)
    else
        str
}

fun String.asClass(): String {
    val str = this
    return this.upperFirst()
}

fun String.asField(): String {
    return this.lowerFirst()
}

fun String.asMethod(): String {
    return this.lowerFirst()
}

fun String.asGetter(): String {
    return "get" + this.upperFirst()
}

fun String.asSetter(): String {
    return "set" + this.upperFirst()
}