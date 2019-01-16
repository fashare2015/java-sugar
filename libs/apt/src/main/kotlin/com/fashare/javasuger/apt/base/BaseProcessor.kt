package com.fashare.javasuger.apt.base

import com.fashare.javasuger.apt.util.EnvUtil
import com.sun.source.util.Trees
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Names
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements


abstract class BaseProcessor : AbstractProcessor() {
    // apt 相关类
    protected val filer: Filer get() = EnvUtil.filer
    protected val elements: Elements get() = EnvUtil.elements
    protected val messager: Messager get() = EnvUtil.messager

    // javac 编译器相关类
    protected val trees: Trees get() = EnvUtil.trees
    protected val treeMaker: TreeMaker get() = EnvUtil.treeMaker
    protected val names: Names get() = EnvUtil.names

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        EnvUtil.init(env)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}
