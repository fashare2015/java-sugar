package com.fashare.javasuger.apt

import com.fashare.javasuger.annotation.Subject
import com.fashare.javasuger.apt.base.BaseProcessor
import com.fashare.javasuger.apt.util.logd
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement

internal class SubjectProcessorImpl : BaseProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Subject::class.java.canonicalName)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        logd("process begin !!! set = $set")

        roundEnvironment.getElementsAnnotatedWith(Subject::class.java)
                .filter { it is TypeElement }
                .map { it as TypeElement }
                .forEach {
//                    val tree = trees.getTree(it) as JCTree
                    val treePath = trees.getPath(it)
                    val tree = treePath.compilationUnit as JCTree
                    logd("process find class = $it, jcTree = ${tree.javaClass.simpleName}")
                    tree.accept(MyTreeTranslator(it.simpleName))
                }

        logd("process end !!!")
        return true
    }

    inner class MyTreeTranslator(val rootClazzName: Name) : TreeTranslator() {

        override fun visitTopLevel(cu: JCTree.JCCompilationUnit) {
            logd("visitTopLevel: ")
            cu.imports.forEach {
                logd("visitTopLevel: import $it, ${it.qualid?.javaClass?.simpleName}")
            }
            cu.defs = cu.defs.prepend(
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("java.util")), names.fromString("*")), false))
            super.visitTopLevel(cu)
        }

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            val jcClassName = jcClassDecl.name.toString()
            logd("visitClassDef: class name = $jcClassName, rootClazzName = $rootClazzName")
            if (jcClassDecl.name.equals(rootClazzName)) {
                jcClassDecl.defs = jcClassDecl.defs
                        .prepend(makeObserversFieldDecl(jcClassDecl))
            }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeObserversFieldDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                    names.fromString("_mObserverList"),
                    treeMaker.Ident(names.fromString("String")),
                    treeMaker.NewClass(null, List.nil(), treeMaker.Ident(names.fromString("String")), List.nil(), null))
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
                    makeObserversFieldDecl(jcClassDecl)
            )

            return treeMaker.ClassDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong()),
                    names.fromString("_InstanceHolder"),
                    List.nil(), null, List.nil(), defs)
        }
    }
}


