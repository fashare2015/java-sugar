package com.fashare.javasugar.apt.processors.lang

import com.fashare.javasugar.annotation.lang.Setter
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.fashare.javasugar.apt.util.appendIf
import com.fashare.javasugar.apt.util.asSetter
import com.fashare.javasugar.apt.util.contains
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
import javax.annotation.processing.Processor
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
internal class SetterProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Setter::class.java

    private var mFields: List<JCVariableDecl> = List.nil()

    private var returnThis = true

    override fun translate(curElement: TypeElement, curTree: JCTree) {
        returnThis = curElement.getAnnotation(mAnnotation).returnThis

        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    override fun generateJavaFile(curElement: TypeElement, curTree: JCTree) {
        getJavaFile(curElement).writeTo(filer)
    }

    private fun getJavaFile(curElement: TypeElement): JavaFile {
        val packageName = rootTree?.packageName?.toString() ?: ""

        val curIGetter = TypeSpec.interfaceBuilder("${curElement.qualifiedName.substring(packageName.length + 1).replace(".", "$$")}\$\$ISetter")
                .addModifiers(Modifier.PUBLIC)
                .apply {
                    mFields.forEach {
                        this.addMethod(MethodSpec.methodBuilder(it.name.toString().asSetter())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .apply {
                                    if (returnThis)
                                        this.returns(ClassName.get(curElement.asType()))
                                    else
                                        this.returns(Void.TYPE)
                                }
                                .addParameter(ClassName.get(it.sym.asType()), it.name.toString())
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

                            jcClassDecl.defs = jcClassDecl.defs
                                    .appendIf(makeSetterMethodDecl(it, jcClassDecl)) {
                                        !jcClassDecl.contains(it as JCMethodDecl)
                                    }
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
        private fun makeSetterMethodDecl(jcVariableDecl: JCVariableDecl, jcClassDecl: JCClassDecl): JCMethodDecl {
            val body = ListBuffer<JCStatement>()
                    .append(treeMaker.Exec(treeMaker.Assign(
                            treeMaker.Select(treeMaker.Ident(names._this), jcVariableDecl.getName()),
                            treeMaker.Ident(jcVariableDecl.name)
                    )))
                    .apply {
                        if (returnThis) {
                            this.append(treeMaker.Return(treeMaker.Ident(names._this)))
                        }
                    }
                    .toList()
                    .let { treeMaker.Block(0, it) }

            return treeMaker.MethodDef(
                    treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                    names.fromString(jcVariableDecl.getName().toString().asSetter()),
                    if (returnThis) {
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
}
