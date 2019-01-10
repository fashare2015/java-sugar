package com.fashare.javasuger.apt

import com.fashare.javasuger.annotation.Getter
import com.sun.source.tree.Tree
import com.sun.source.util.Trees
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.ListBuffer
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic


//@AutoService(Processor::class)
internal class MainProcesserImpl : AbstractProcessor() {

    private var mFiler: Filer? = null           //文件相关的辅助类
    private var mElementUtils: Elements? = null //元素相关的辅助类
    private var mMessager: Messager? = null     //日志相关的辅助类

    // javac 编译器相关类
    private lateinit var trees: Trees
    private lateinit var treeMaker: TreeMaker
    private lateinit var names: Names

    override fun init(env: ProcessingEnvironment?) {
        super.init(env)

        mFiler = env?.filer
        mElementUtils = env?.elementUtils
        mMessager = env?.messager

        trees = Trees.instance(env)
        val context = (processingEnv as JavacProcessingEnvironment).context
        treeMaker = TreeMaker.instance(context)
        names = Names.instance(context)
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Getter::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
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

    private fun loge(msg: String) {
        mMessager?.printMessage(Diagnostic.Kind.ERROR, msg)
    }

    private fun logd(msg: String) {
        mMessager?.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    inner class MyTreeTranslator : TreeTranslator() {
        override fun visitClassDef(jcClassDecl: JCTree.JCClassDecl) {
            jcClassDecl.defs
                    .filter { it.kind == Tree.Kind.VARIABLE }
                    .map { it as JCTree.JCVariableDecl }
                    .forEach {
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(it))
                    }
            super.visitClassDef(jcClassDecl)
        }

        private fun makeGetterMethodDecl(jcVariableDecl: JCTree.JCVariableDecl?): JCTree? {
            val statements = ListBuffer<JCTree.JCStatement>()
            statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl?.getName())))
            val body = treeMaker.Block(0, statements.toList())
            return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC.toLong()), getNewMethodName(jcVariableDecl?.getName()), jcVariableDecl?.vartype, List.nil(), List.nil(), List.nil(), body, null)
        }

        private fun getNewMethodName(name: Name?): Name? {
            val str = name.toString()
            if (str.isNotEmpty()) {
                return names.fromString("get" + str.substring(0, 1).toUpperCase() + str.substring(1, str.length))
            } else {
                return names.fromString("get")
            }
        }
    }
}


