package com.fashare.javasugar.apt.processors.designpattern

import com.fashare.javasugar.annotation.designpattern.ISubject
import com.fashare.javasugar.annotation.designpattern.Observer
import com.fashare.javasugar.annotation.designpattern.Subject
import com.fashare.javasugar.apt.base.SingleAnnotationProcessor
import com.fashare.javasugar.apt.util.asField
import com.fashare.javasugar.apt.util.asGetter
import com.fashare.javasugar.apt.util.getValue
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.Type
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.JCTree.JCStatement
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import javax.annotation.processing.Processor
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@Suppress("unused")
@AutoService(Processor::class)
internal class SubjectProcessor : SingleAnnotationProcessor() {
    override val mAnnotation = Subject::class.java

    var mObservers = emptyArray<Observer>()

    override fun translate(curElement: TypeElement, curTree: JCTree) {
        mObservers = curElement.getAnnotation(mAnnotation).value

        rootTree?.accept(MyTreeTranslator(curElement.simpleName as Name))
    }

    override fun generateJavaFile(curElement: TypeElement, curTree: JCTree) {
        getJavaFile(curElement).writeTo(filer)
    }

    private fun getJavaFile(curElement: TypeElement): JavaFile {
        val packageName = rootTree?.packageName?.toString() ?: ""

        val curISubject = TypeSpec.interfaceBuilder("${curElement.qualifiedName.substring(packageName.length + 1).replace(".", "$$")}\$\$ISubject")
                .addModifiers(Modifier.PUBLIC)
                .apply {
                    val typeISubject = ClassName.get(ISubject::class.java)

                    mObservers.forEach { observer ->
                        val typeFull: Type.ClassType = (observer.getValue() as Type.ClassType)  // Observer
                                .let { it.tsym.type as Type.ClassType }     // Observer<T>
                        val typeObserver = ClassName.get(observer.getValue()) as ClassName
                        val typeTs = typeFull.typeArguments.indices.map {
                            TypeVariableName.get("${typeFull.typeArguments[it]}")
                        }

                        val hasTypeParam = typeFull.typeArguments.isNotEmpty()
                        val typeReturn = if (hasTypeParam) {  // ISubject<Observer<T, ..>>
                            val typeObserverT = ParameterizedTypeName.get(typeObserver, *typeTs.toTypedArray())
                            ParameterizedTypeName.get(typeISubject, typeObserverT)
                        } else {    // ISubject<Observer>
                            ParameterizedTypeName.get(typeISubject, typeObserver)
                        }
                        this.addMethod(MethodSpec.methodBuilder("${observer.name}Subject".asGetter())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .apply {
                                    if (hasTypeParam)
                                        this.addTypeVariables(typeTs)
                                }
                                .returns(typeReturn)
                                .build()
                        )
                    }
                }
                .build()

        return JavaFile.builder(packageName, curISubject)
                .build()
    }

    inner class MyTreeTranslator(private val rootClazzName: Name) : TreeTranslator() {

        override fun visitTopLevel(cu: JCTree.JCCompilationUnit) {
            cu.defs = cu.defs
                    .prepend(treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("java.util")), names.fromString("*")), false))
                    .prepend(treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("com.fashare.javasugar.annotation.designpattern")), names.fromString("ISubject")), false))
            super.visitTopLevel(cu)
        }

        override fun visitClassDef(jcClassDecl: JCClassDecl) {
            if (jcClassDecl.name == rootClazzName) {
                treeMaker.at(jcClassDecl.pos)

                makeISubjectFieldAndGetter(jcClassDecl)
            }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeISubjectFieldAndGetter(jcClassDecl: JCClassDecl) {
            mObservers.forEach { observer ->
                val iSubject = treeMaker.Ident(names.fromString("ISubject"))
                val iSubjectStub = treeMaker.Select(treeMaker.Ident(names.fromString("ISubject")), names.fromString("Stub"))
                val typeParam = treeMaker.Ident((observer.getValue() as? Type)?.asElement()?.simpleName)
                val fieldName = names.fromString("_${observer.name.asField()}")

                val iSubjectField = treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PRIVATE.toLong()),
                        fieldName,
                        treeMaker.TypeApply(iSubject, List.of(typeParam)),    // 泛型 ISubject<typeParam>
                        treeMaker.NewClass(null, List.nil(),
                                treeMaker.TypeApply(iSubjectStub, List.of(typeParam)),
                                List.nil(),
                                treeMaker.AnonymousClassDef(treeMaker.Modifiers(0), List.nil())))

                jcClassDecl.defs = jcClassDecl.defs.prepend(iSubjectField)

                // getter method
                val methodName = names.fromString("${observer.name}Subject".asGetter())
                val body = ListBuffer<JCStatement>()
                        .append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names._this), fieldName)))
                        .toList()
                        .let { treeMaker.Block(0, it) }

                val iSubjectGetter = treeMaker.MethodDef(
                        treeMaker.Modifiers(Flags.PUBLIC.toLong()),
                        methodName,
                        iSubjectField.vartype,
                        List.nil(), List.nil(), List.nil(), body, null)

                jcClassDecl.defs = jcClassDecl.defs.prepend(iSubjectGetter)
            }
        }
    }
}
