package com.fashare.javasuger.apt.processors.designpattern

import com.fashare.javasuger.annotation.designpattern.Singleton
import com.fashare.javasuger.apt.base.SingleAnnotationProcessor
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.lang.model.element.TypeElement

internal class SingletonProcessorImpl : SingleAnnotationProcessor() {
    override val mAnnotation = Singleton::class.java

    override fun translator(curElement: TypeElement, curTree: JCTree, rootTree: JCTree.JCCompilationUnit) {
        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    inner class MyTreeTranslator(val rootClazzName: Name) : TreeTranslator() {

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name.equals(rootClazzName)) {     // 防止重复访问生成的 _InstanceHolder
                jcClassDecl.defs = jcClassDecl.defs
                        .prepend(makeGetInstanceMethodDecl(jcClassDecl))
                        .prepend(makeInstanceHolderDecl(jcClassDecl))
            }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeInstanceFieldDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong() or Flags.FINAL.toLong()),
                    names.fromString("_sInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    treeMaker.NewClass(null, List.nil(), treeMaker.Ident(jcClassDecl.name), List.nil(), null))
        }

        private fun makeGetInstanceMethodDecl(jcClassDecl: JCClassDecl): JCTree {
            val body = ListBuffer<JCTree.JCStatement>()
                    .append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("_InstanceHolder")), names.fromString("_sInstance"))))
                    .toList()
                    .let { treeMaker.Block(0, it) }

            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong() or Flags.STATIC.toLong()),
                    names.fromString("getInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    List.nil(), List.nil(), List.nil(), body, null)
        }

        private fun makeInstanceHolderDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.ClassDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong()),
                    names.fromString("_InstanceHolder"),
                    List.nil(), null, List.nil(),
                    List.of(makeInstanceFieldDecl(jcClassDecl)))
        }
    }
}