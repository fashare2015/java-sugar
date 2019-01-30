package com.fashare.javasugar.apt.processors.designpattern

import com.fashare.javasugar.annotation.designpattern.Singleton
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.fashare.javasugar.apt.util.contains
import com.google.auto.service.AutoService
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.JCTree.JCMethodDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.annotation.processing.Processor
import javax.lang.model.element.TypeElement

@Suppress("unused")
@AutoService(Processor::class)
internal class SingletonProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Singleton::class.java

    override fun translate(curElement: TypeElement, curTree: JCTree) {
        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    inner class MyTreeTranslator(private val rootClazzName: Name) : TreeTranslator() {

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name == rootClazzName) {     // 防止重复访问生成的 _InstanceHolder
                val getInstanceMethod = makeGetInstanceMethodDecl(jcClassDecl)
                if (!jcClassDecl.contains(getInstanceMethod)) {
                    jcClassDecl.defs = jcClassDecl.defs
                            .prepend(getInstanceMethod)
                            .prepend(makeInstanceHolderDecl(jcClassDecl))
                }
            }
            super.visitClassDef(jcClassDecl)
        }

        /**
         *   public static UserManager getInstance() {
         *       return UserManager._InstanceHolder._sInstance;
         *   }
         */
        private fun makeGetInstanceMethodDecl(jcClassDecl: JCClassDecl): JCMethodDecl {
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

        /**
         *   private static class _InstanceHolder {
         *       private static final UserManager _sInstance = new UserManager();
         *   }
         */
        private fun makeInstanceHolderDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.ClassDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong()),
                    names.fromString("_InstanceHolder"),
                    List.nil(), null, List.nil(),
                    List.of(makeInstanceFieldDecl(jcClassDecl)))
        }

        /**
         * private static final UserManager _sInstance = new UserManager();
         */
        private fun makeInstanceFieldDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong() or Flags.FINAL.toLong()),
                    names.fromString("_sInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    treeMaker.NewClass(null, List.nil(), treeMaker.Ident(jcClassDecl.name), List.nil(), null))
        }
    }
}
