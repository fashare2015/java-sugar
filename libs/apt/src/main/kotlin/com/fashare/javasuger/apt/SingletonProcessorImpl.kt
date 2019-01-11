package com.fashare.javasuger.apt

import com.fashare.javasuger.annotation.Singleton
import com.fashare.javasuger.apt.base.BaseProcessor
import com.fashare.javasuger.apt.util.logd
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

internal class SingletonProcessorImpl : BaseProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Singleton::class.java.canonicalName)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        logd("process begin !!! set = $set")

        roundEnvironment.getElementsAnnotatedWith(Singleton::class.java)
                .filter { it is TypeElement }
                .map { it as TypeElement }
                .forEach {
                    logd("process find class = $it")

                    val tree = trees.getTree(it) as JCTree
                    tree.accept(MyTreeTranslator())
                }

        logd("process end !!!")
        return true
    }

    inner class MyTreeTranslator : TreeTranslator() {

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            val jcClassName = jcClassDecl.name.toString()
            logd("visitClassDef: class name = $jcClassName")
            if (jcClassName != "_InstanceHolder") {     // 防止重复访问生成的 _InstanceHolder
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
            val statements = ListBuffer<JCTree.JCStatement>()
            statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("_InstanceHolder")), names.fromString("_sInstance"))))
            val body = treeMaker.Block(0, statements.toList())
            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong() or Flags.STATIC.toLong()),
                    names.fromString("getInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    List.nil(), List.nil(), List.nil(), body, null)
        }

        private fun makeInstanceHolderDecl(jcClassDecl: JCClassDecl): JCTree {
            val defs = List.of(
                    makeInstanceFieldDecl(jcClassDecl)
            )

            return treeMaker.ClassDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong()),
                    names.fromString("_InstanceHolder"),
                    List.nil(), null, List.nil(), defs)
        }
    }
}


