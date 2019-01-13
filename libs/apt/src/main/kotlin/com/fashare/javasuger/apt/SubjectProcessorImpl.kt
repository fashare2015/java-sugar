package com.fashare.javasuger.apt

import com.fashare.javasuger.annotation.Subject
import com.fashare.javasuger.apt.base.BaseProcessor
import com.fashare.javasuger.apt.util.logd
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.TypeTag
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
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
                            treeMaker.Ident(names.fromString(paramType))
                            , null)
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


