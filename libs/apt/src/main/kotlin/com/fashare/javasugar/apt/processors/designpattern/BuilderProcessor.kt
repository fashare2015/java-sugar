package com.fashare.javasugar.apt.processors.designpattern

import com.fashare.javasugar.annotation.designpattern.Builder
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.fashare.javasugar.apt.util.asSetter
import com.fashare.javasugar.apt.util.lowerFirst
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import com.sun.source.tree.Tree
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.JCTree.JCVariableDecl
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.Name
import javax.annotation.processing.Processor
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
internal class BuilderProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Builder::class.java

    private var mFields: List<JCVariableDecl> = List.nil()

    private var returnThis = true

    override fun translate(curElement: TypeElement, curTree: JCTree) {
        curTree.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    override fun generateJavaFile(curElement: TypeElement, curTree: JCTree) {
        getJavaFile(curElement).writeTo(filer)
    }

    private fun getJavaFile(curElement: TypeElement): JavaFile {
        val packageName = rootTree?.packageName?.toString() ?: ""
        val rawTargetName = "${curElement.qualifiedName.substring(packageName.length + 1)}"
        val targetName = "${curElement.qualifiedName.substring(packageName.length + 1).replace(".", "$$")}"
        val builderName = "$targetName\$\$Builder"

        val curBuilder = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC)
                .apply {
                    mFields.forEach {
                        this.addField(FieldSpec.builder(ClassName.get(it.sym.asType()), it.name.toString(), Modifier.PRIVATE).build())

                        this.addMethod(MethodSpec.methodBuilder(it.name.toString().asSetter())
                                .addModifiers(Modifier.PUBLIC)
                                .apply {
                                    if (returnThis)
                                        this.returns(ClassName.get(packageName, builderName))
                                    else
                                        this.returns(Void.TYPE)
                                }
                                .addParameter(ClassName.get(it.sym.asType()), it.name.toString())
                                .addStatement("this.\$L = \$L", it.name.toString(), it.name.toString())
                                .apply {
                                    if(returnThis)
                                        this.addStatement("return this")
                                }
                                .build()
                        )
                    }

                    val typeUser = rawTargetName
                    val varUser = targetName.lowerFirst()
                    this.addMethod(MethodSpec.methodBuilder("build")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.get(packageName, typeUser))
                            .apply {
                                this.addStatement("\$L \$L = new \$L()", typeUser, varUser, typeUser)
                                        .apply {
                                            mFields.forEach {
                                                this.addStatement("\$L.\$L = this.\$L", varUser, it.name.toString(), it.name.toString())
                                            }
                                        }
                                        .addStatement("return \$L", varUser)
                            }
                            .build()
                    )
                }
                .build()

        return JavaFile.builder(packageName, curBuilder)
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

                            // Modifier 变为 package
                            it.mods.flags = it.mods.flags and (Flags.PRIVATE.toLong().inv())
                        }
            }
            super.visitClassDef(jcClassDecl)
        }
    }
}
