package com.fashare.javasugar.apt.processors.designpattern

import com.fashare.javasugar.annotation.designpattern.Singleton
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.fashare.javasugar.apt.util.contains
import com.google.auto.service.AutoService
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.code.TypeTag
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.JCTree.JCMethodDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.annotation.processing.Processor
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@Suppress("unused")
@AutoService(Processor::class)
internal class SingletonProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Singleton::class.java

    override fun translate(curElement: TypeElement, curTree: JCTree) {
        var useDCL = false
        var executableElement: ExecutableElement? = null
        curElement.enclosedElements
                .filter { it is ExecutableElement }
                .map { it as ExecutableElement }
                .forEach {
                    if (it.getAnnotation(Singleton.Main::class.java) != null && !it.parameters.isEmpty()) {
                        useDCL = true
                        executableElement = it
                        return@forEach
                    }
                }
        if (useDCL) {   // DCL
            curTree.accept(DCLTreeTranslator(curElement.simpleName as Name, executableElement as? Symbol.MethodSymbol))
        } else {        // Instance Holder
            curTree.accept(InstanceHolderTreeTranslator(curElement.simpleName as Name))
        }
    }

    inner class DCLTreeTranslator(private val rootClazzName: Name, private val constructor: Symbol.MethodSymbol?) : TreeTranslator() {

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name == rootClazzName) {     // 防止重复访问生成的 _InstanceHolder
                if (constructor == null)
                    return
                treeMaker.at(jcClassDecl.pos)
                val getInstanceMethod = makeGetInstanceMethodDecl(jcClassDecl)
                if (!jcClassDecl.contains(getInstanceMethod)) {
                    jcClassDecl.defs = jcClassDecl.defs
                            .prepend(getInstanceMethod)
                            .prepend(makeInstanceFieldDecl(jcClassDecl))
                }
            }
            super.visitClassDef(jcClassDecl)
        }

        /**
         *  public static UserManager getInstance(User user) {
         *      if (_sInstance == null) {
         *          synchronized (UserManager.class) {
         *              if (_sInstance == null) {
         *                  _sInstance = new UserManager(user);
         *              }
         *          }
         *       }
         *       return _sInstance;
         *   }
         */
        private fun makeGetInstanceMethodDecl(jcClassDecl: JCClassDecl): JCMethodDecl {
            val body = ListBuffer<JCTree.JCStatement>()
                    .append(makeCheckNullIfDecl(jcClassDecl))
                    .append(treeMaker.Return(treeMaker.Ident(names.fromString("_sInstance"))))
                    .toList()
                    .let { treeMaker.Block(0, it) }

            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong() or Flags.STATIC.toLong()),
                    names.fromString("getInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    List.nil(),
                    constructor?.parameters.let { paramSyms ->
                        var params = List.nil<JCTree.JCVariableDecl>()
                        paramSyms?.forEach {
                            params = params.append(treeMaker.VarDef(it, null))
                        }
                        params
                    },
                    List.nil(), body, null)
        }

        private fun makeCheckNullIfDecl(jcClassDecl: JCClassDecl): JCTree.JCIf {
            return treeMaker.If(treeMaker.Parens(treeMaker.Binary(JCTree.Tag.EQ, treeMaker.Ident(names.fromString("_sInstance")), treeMaker.Literal(TypeTag.BOT, null))),
                    ListBuffer<JCTree.JCStatement>()
                            .append(makeSyncDecl(jcClassDecl))
                            .toList()
                            .let { treeMaker.Block(0, it) },
                    null)
        }

        private fun makeSyncDecl(jcClassDecl: JCClassDecl): JCTree.JCSynchronized? {
            return treeMaker.Synchronized(treeMaker.Parens(treeMaker.Select(treeMaker.Ident(jcClassDecl.name), names._class)),
                    ListBuffer<JCTree.JCStatement>()
                            .append(makeDoubleCheckNullIfDecl(jcClassDecl))
                            .toList()
                            .let { treeMaker.Block(0, it) })
        }

        private fun makeDoubleCheckNullIfDecl(jcClassDecl: JCClassDecl): JCTree.JCIf? {
            return treeMaker.If(treeMaker.Parens(treeMaker.Binary(JCTree.Tag.EQ, treeMaker.Ident(names.fromString("_sInstance")), treeMaker.Literal(TypeTag.BOT, null))),
                    ListBuffer<JCTree.JCStatement>()
                            .append(makeAssignDecl(jcClassDecl))
                            .toList()
                            .let { treeMaker.Block(0, it) },
                    null)
        }

        private fun makeAssignDecl(jcClassDecl: JCClassDecl): JCTree.JCExpressionStatement? {
            return treeMaker.Exec(treeMaker.Assign(
                    treeMaker.Ident(names.fromString("_sInstance")),
                    treeMaker.NewClass(null, List.nil(), treeMaker.Ident(jcClassDecl.name), constructor?.parameters.let { paramSyms ->
                        var params = List.nil<JCTree.JCExpression>()
                        paramSyms?.forEach {
                            params = params.append(treeMaker.Ident(it.name))
                        }
                        params
                    }, null)
            ))
        }

        /**
         * private static volatile UserManager _sInstance;
         */
        private fun makeInstanceFieldDecl(jcClassDecl: JCClassDecl): JCTree {
            return treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong() or Flags.STATIC.toLong() or Flags.VOLATILE.toLong()),
                    names.fromString("_sInstance"),
                    treeMaker.Ident(jcClassDecl.name),
                    null)
        }
    }

    inner class InstanceHolderTreeTranslator(private val rootClazzName: Name) : TreeTranslator() {

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
