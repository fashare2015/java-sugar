package com.fashare.javasugar.apt.processors.lang

import com.fashare.javasugar.annotation.lang.Getter
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.sun.source.tree.Tree
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import java.io.IOException
import javax.annotation.processing.Processor
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
internal class GetterProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Getter::class.java

    private var mFields: List<JCVariableDecl> = List.nil()

    override fun translator(curElement: TypeElement, curTree: JCTree, rootTree: JCCompilationUnit) {
        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))

        try {
            getJavaFile(curElement).writeTo(filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getJavaFile(curElement: TypeElement): JavaFile {
        val packageName = rootTree?.packageName?.toString() ?: ""

        val curIGetter = TypeSpec.interfaceBuilder("${curElement.qualifiedName.substring(packageName.length + 1).replace(".", "$$")}\$\$IGetter")
                .addModifiers(Modifier.PUBLIC)
                .apply {
                    mFields.forEach {
                        this.addMethod(MethodSpec.methodBuilder(getNewMethodName(it.name).toString())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(ClassName.get(it.sym.asType()))
//                                .addParameter(Array<String>::class.java, "args")
//                .addStatement("\$T.out.println(\$S)", System::class.java, "Hello, JavaPoet!")
                                .build()
                        )
                    }
                }
                .build()

        return JavaFile.builder(packageName, curIGetter)
                .build()
    }

    inner class MyTreeTranslator(private val rootClazzName: Name) : TreeTranslator() {

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name == rootClazzName) {
                mFields = List.nil()
                jcClassDecl.defs
                        .filter { it.kind == Tree.Kind.VARIABLE }
                        .map { it as JCVariableDecl }
                        .forEach {
                            mFields = mFields.append(it)
                            jcClassDecl.defs = jcClassDecl.defs.append(makeGetterMethodDecl(it))
                        }

                // 去掉 abstract
                jcClassDecl.mods.flags = jcClassDecl.mods.flags and (Flags.ABSTRACT.toLong().inv())
            }
            super.visitClassDef(jcClassDecl)
        }

        /**
        public String getName() {
        return this.name;
        }
         */
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
    }

    /**
     * getName
     */
    private fun getNewMethodName(name: Name): Name {
        val str = name.toString()
        return if (str.isNotEmpty()) {
            names.fromString("get" + str.substring(0, 1).toUpperCase() + str.substring(1, str.length))
        } else {
            names.fromString("get")
        }
    }
}
