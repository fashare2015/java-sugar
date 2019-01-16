package com.fashare.javasuger.apt.util

import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Names
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements

/**
 * Created by apple on 2019/1/10.
 */
internal object EnvUtil {
    private var env: ProcessingEnvironment? = null

    // apt 相关类
    lateinit var filer: Filer              //文件相关的辅助类
    lateinit var elements: Elements    //元素相关的辅助类
    lateinit var messager: Messager        //日志相关的辅助类

    // javac 编译器相关类
    lateinit var trees: Trees
    lateinit var treeMaker: TreeMaker
    lateinit var names: Names

    fun init(env: ProcessingEnvironment) {
        if (this.env != null) {
            return
        }
        this.env = env

        filer = env.filer
        elements = env.elementUtils
        messager = env.messager

        trees = Trees.instance(env)
        val context = (env as JavacProcessingEnvironment).context
        treeMaker = TreeMaker.instance(context)
        names = Names.instance(context)
    }
}
