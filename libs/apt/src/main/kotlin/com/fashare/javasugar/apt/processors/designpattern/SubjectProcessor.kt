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
                    val typeT = TypeVariableName.get("T")
                    val typeISubject = ClassName.get(ISubject::class.java)

                    mObservers.forEach { observer ->
                        val typeFull: Type.ClassType = (observer.getValue() as Type.ClassType)  // Observer
                                .let { it.tsym.type as Type.ClassType }     // Observer<T>
                        val typeObserver = ClassName.get(observer.getValue()) as ClassName

                        val hasTypeParam = typeFull.typeArguments.isNotEmpty()
                        val typeReturn = if (hasTypeParam) {  // ISubject<Observer<? super T>>  PECS
//                            val typeSuperT = WildcardTypeName.supertypeOf(typeT)
//                            val typeObserverSuperT = ParameterizedTypeName.get(typeObserver, typeSuperT)
//                            ParameterizedTypeName.get(typeISubject, typeObserverSuperT)

                            val typeObserverT = ParameterizedTypeName.get(typeObserver, typeT)
                            ParameterizedTypeName.get(typeISubject, typeObserverT)
                        } else {    // ISubject<Observer>
                            ParameterizedTypeName.get(typeISubject, typeObserver)
                        }
                        this.addMethod(MethodSpec.methodBuilder("${observer.name}Subject".asGetter())
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .apply {
                                    if (hasTypeParam)
                                        this.addTypeVariable(typeT)
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

                // 添加 private List _mObserverList = new ArrayList();
                makeISubjectFieldAndGetter(jcClassDecl)

                // implement Subject.Stub
//                implementSubjectStub(jcClassDecl)

                // 去掉 abstract
                jcClassDecl.mods.flags = jcClassDecl.mods.flags and (Flags.ABSTRACT.toLong().inv())
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
