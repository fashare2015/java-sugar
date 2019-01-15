package com.fashare.javasuger.apt.processors.lang

import com.fashare.javasuger.annotation.lang.Getter
import com.fashare.javasuger.apt.base.SingleAnnotationProcessor
import com.sun.source.tree.Tree
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.lang.model.element.TypeElement

internal class GetterProcessorImpl : SingleAnnotationProcessor() {
    override val mAnnotation = Getter::class.java

    override fun translator(curElement: TypeElement, curTree: JCTree, rootTree: JCCompilationUnit) {
        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    inner class MyTreeTranslator(val rootClazzName: Name) : TreeTranslator() {
        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name.equals(rootClazzName)) {
                jcClassDecl.defs
                        .filter { it.kind == Tree.Kind.VARIABLE }
                        .map { it as JCVariableDecl }
                        .forEach {
                            jcClassDecl.defs = jcClassDecl.defs.append(makeGetterMethodDecl(it))
                        }
            }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeGetterMethodDecl(jcVariableDecl: JCVariableDecl): JCTree {
            val body = ListBuffer<JCStatement>()
                    .append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names._this), jcVariableDecl.getName())))
                    .toList()
                    .let { treeMaker.Block(0, it) }

            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                    getNewMethodName(jcVariableDecl.getName()),
                    jcVariableDecl.vartype,
                    List.nil(), List.nil(), List.nil(), body, null)
        }

        private fun getNewMethodName(name: Name): Name {
            val str = name.toString()
            if (str.isNotEmpty()) {
                return names.fromString("get" + str.substring(0, 1).toUpperCase() + str.substring(1, str.length))
            } else {
                return names.fromString("get")
            }
        }
    }
}