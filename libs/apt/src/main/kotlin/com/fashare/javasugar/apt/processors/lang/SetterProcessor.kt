package com.fashare.javasugar.apt.processors.lang

import com.fashare.javasugar.annotation.lang.Setter
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.sun.source.tree.Tree
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.TypeTag
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
internal class SetterProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Setter::class.java

    private var mFields: List<JCVariableDecl> = List.nil()

    private val shouldReturnThis = true

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

        val curIGetter = TypeSpec.interfaceBuilder("${curElement.qualifiedName.substring(packageName.length + 1).replace(".", "$$")}\$\$ISetter")
                .addModifiers(Modifier.PUBLIC)
                .apply {
                    mFields.forEach {
                        this.addMethod(MethodSpec.methodBuilder(getNewMethodName(it.name).toString())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
//                                .returns(ClassName.get(it.sym.asType()))
                                .returns(Void.TYPE)
                                .addParameter(ClassName.get(it.sym.asType()), it.name.toString())
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
                treeMaker.at(jcClassDecl.pos)
                mFields = List.nil()
                jcClassDecl.defs
                        .filter { it.kind == Tree.Kind.VARIABLE }
                        .map { it as JCVariableDecl }
                        .forEach {
                            mFields = mFields.append(it)
                            jcClassDecl.defs = jcClassDecl.defs.append(makeSetterMethodDecl(it, jcClassDecl))
                        }

                // 去掉 abstract
                jcClassDecl.mods.flags = jcClassDecl.mods.flags and (Flags.ABSTRACT.toLong().inv())
            }
            super.visitClassDef(jcClassDecl)
        }

        /**
         *  public User setName(String name) {
         *      this.name = name;
         *      return this;
         *  }
         */
        private fun makeSetterMethodDecl(jcVariableDecl: JCVariableDecl, jcClassDecl: JCClassDecl): JCTree {
            val body = ListBuffer<JCStatement>()
                    .append(treeMaker.Exec(treeMaker.Assign(
                            treeMaker.Select(treeMaker.Ident(names._this), jcVariableDecl.getName()),
                            treeMaker.Ident(jcVariableDecl.name)
                    )))
                    .apply {
                        if (shouldReturnThis) {
                            this.append(treeMaker.Return(treeMaker.Ident(names._this)))
                        }
                    }
                    .toList()
                    .let { treeMaker.Block(0, it) }

            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                    getNewMethodName(jcVariableDecl.getName()),
                    if (shouldReturnThis) {
                        treeMaker.Ident(jcClassDecl.name)
                    } else {
                        treeMaker.TypeIdent(TypeTag.VOID)
                    },
                    List.nil(),
                    List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER),
                            jcVariableDecl.name,
                            jcVariableDecl.vartype,
                            null)
                    ),
                    List.nil(),
                    body, null)
        }
    }

    /**
     * setName
     */
    private fun getNewMethodName(name: Name): Name {
        val str = name.toString()
        return if (str.isNotEmpty()) {
            names.fromString("set" + str.substring(0, 1).toUpperCase() + str.substring(1, str.length))
        } else {
            names.fromString("set")
        }
    }
}
