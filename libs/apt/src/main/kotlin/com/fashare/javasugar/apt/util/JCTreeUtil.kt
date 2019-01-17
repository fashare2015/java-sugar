package com.fashare.javasugar.apt.util

import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.JCTree.JCMethodDecl

fun JCClassDecl.contains(method: JCMethodDecl): Boolean {
    this.defs.forEach {
        if (it is JCMethodDecl && it.conflict(method)) {
            return true
        }
    }
    return false
}

fun JCMethodDecl.conflict(that: JCMethodDecl): Boolean {
    return this.name == that.name && this.let {
        if (this.params.length() != that.params.length())
            return false

        for (i in this.params.indices) {
            if (this.params[i].vartype.toString() != that.params[i].vartype.toString())
                return false
        }

        return true
    }
}
