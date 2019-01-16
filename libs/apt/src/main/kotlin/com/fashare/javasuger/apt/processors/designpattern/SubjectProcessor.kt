package com.fashare.javasuger.apt.processors.designpattern

import com.fashare.javasuger.annotation.designpattern.Subject
import com.fashare.javasuger.apt.base.SingleAnnotationProcessor
import com.google.auto.service.AutoService
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.TypeTag
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.Name
import javax.annotation.processing.Processor
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
internal class SubjectProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Subject::class.java

    override fun translator(curElement: TypeElement, curTree: JCTree, rootTree: JCTree.JCCompilationUnit) {
        rootTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    inner class MyTreeTranslator(private val rootClazzName: Name) : TreeTranslator() {

        override fun visitTopLevel(cu: JCTree.JCCompilationUnit) {
            cu.defs = cu.defs.prepend(
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("java.util")), names.fromString("*")), false))
            super.visitTopLevel(cu)
        }

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name == rootClazzName) {
                treeMaker.at(jcClassDecl.pos)

                // 添加 private List _mObserverList = new ArrayList();
                makeObserversFieldDecl(jcClassDecl)

                // implement Subject.Stub
                implementSubjectStub(jcClassDecl)

                // 去掉 abstract
                jcClassDecl.mods.flags = jcClassDecl.mods.flags and (Flags.ABSTRACT.toLong().inv())
            }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeObserversFieldDecl(jcClassDecl: JCClassDecl) {
            val observersField = treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PRIVATE.toLong()),
                    names.fromString("_mObserverList"),
                    treeMaker.Ident(names.fromString("List")),
                    treeMaker.NewClass(null, List.nil(), treeMaker.Ident(names.fromString("ArrayList")), List.nil(), null))

            jcClassDecl.defs = jcClassDecl.defs.prepend(observersField)
        }

        // 是否有更简洁的方法
        private fun implementSubjectStub(jcClassDecl: JCClassDecl) {
            // void add(Object observer) {}
            val methodAdd = method("add", "Object", "observer") {
                treeMaker.Block(0, List.of(invoke("_mObserverList", "add", "observer")))
            }

            // void remove(Object observer) {}
            val methodRemove = method("remove", "Object", "observer") {
                treeMaker.Block(0, List.of(invoke("_mObserverList", "remove", "observer")))
            }

            // void notify(Object event) {}
            val methodNotify = method("notify", "Object", "event") {
                val forEachLoop = treeMaker.ForeachLoop(
                        treeMaker.VarDef(treeMaker.Modifiers(0),
                                names.fromString("item"),
                                treeMaker.Ident(names.fromString("Object"))
                                , null),
                        treeMaker.Ident(names.fromString("_mObserverList")),
                        treeMaker.Block(0, List.of(invoke("item", "Listener", "onEvent", "event")))  // TODO 类型不写死 Listener
                )
                treeMaker.Block(0, List.of(forEachLoop))
            }

            jcClassDecl.defs = jcClassDecl.defs
                    .append(methodAdd)
                    .append(methodRemove)
                    .append(methodNotify)
        }

        fun method(methodName: String, paramType: String, param: String, body: () -> JCTree.JCBlock): JCMethodDecl {
            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                    names.fromString(methodName),
                    treeMaker.TypeIdent(TypeTag.VOID),
                    List.nil(),
                    List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),
                            names.fromString(param),
                            treeMaker.Ident(names.fromString(paramType)),
                            null)
                    ),
                    List.nil(),
                    body.invoke(), null)
        }

        fun invoke(field: String, methodName: String, param: String): JCExpressionStatement {
            val _field = treeMaker.Ident(names.fromString(field))
            return treeMaker.Exec(treeMaker.Apply(null,
                    treeMaker.Select(_field, names.fromString(methodName)),
                    List.of(treeMaker.Ident(names.fromString(param)))
            ))
        }

        fun invoke(field: String, castTo: String, methodName: String, param: String): JCExpressionStatement {
            val _field = treeMaker.Parens(treeMaker.TypeCast(
                    treeMaker.Ident(names.fromString(castTo)),
                    treeMaker.Ident(names.fromString(field))
            ))
            return treeMaker.Exec(treeMaker.Apply(null,
                    treeMaker.Select(_field, names.fromString(methodName)),
                    List.of(treeMaker.Ident(names.fromString(param)))
            ))
        }
    }
}
