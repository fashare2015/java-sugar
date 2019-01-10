package com.fashare.javasuger.apt

import com.fashare.javasuger.annotation.Getter
import com.fashare.javasuger.apt.base.BaseProcessor
import com.fashare.javasuger.apt.util.logd
import com.sun.source.tree.Tree
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

internal class GetterProcessorImpl : BaseProcessor() {

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Getter::class.java.canonicalName)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        logd("process begin !!! set = $set")

        roundEnvironment.getElementsAnnotatedWith(Getter::class.java)
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
            jcClassDecl.defs.forEach {
                logd("visitClassDef: def = $it")
            }

            jcClassDecl.defs
                    .filter { it.kind == Tree.Kind.VARIABLE }
                    .map { it as JCVariableDecl }
                    .forEach {
                        val varType = it.vartype
                        logd("visitClassDef: var type = $varType : ${varType.javaClass.simpleName}")
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(it))
                    }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeGetterMethodDecl(jcVariableDecl: JCVariableDecl): JCTree {
            val statements = ListBuffer<JCStatement>()
            statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())))
            val body = treeMaker.Block(0, statements.toList())
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


