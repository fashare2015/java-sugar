package com.fashare.javasuger.apt.base

import com.fashare.javasuger.apt.util.logd
import com.sun.tools.javac.tree.JCTree
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

abstract class SingleAnnotationProcessor : BaseProcessor() {
    abstract val mAnnotation: Class<out Annotation>

    override final fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(mAnnotation.canonicalName)
    }

    override final fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        logd("process begin !!! set = $set")

        roundEnvironment.getElementsAnnotatedWith(mAnnotation)
                .filter { it is TypeElement }
                .map { it as TypeElement }
                .forEach {
                    val treePath = trees.getPath(it)
                    val cu = treePath.compilationUnit as JCTree.JCCompilationUnit
                    logd("process find class = $it, jcTree = ${cu.javaClass.simpleName}")
                    translator(it, trees.getTree(it) as JCTree, cu)
                }

        logd("process end !!!")
        return true
    }

    abstract fun translator(curElement: TypeElement, curTree: JCTree, rootTree: JCTree.JCCompilationUnit)
}
