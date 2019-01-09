package com.fashare.javasuger.apt

import com.fashare.javasuger.apt.annotation.Getter
import com.fashare.javasuger.apt.test.TestJavaPoet
import java.io.IOException
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

    override fun init(env: ProcessingEnvironment?) {
        super.init(env)

        mFiler = env?.filer
        mElementUtils = env?.elementUtils
        mMessager = env?.messager
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Getter::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        logd("process begin !!! set = $set")

        val javaFile = TestJavaPoet.getJavaFile()

        roundEnvironment.getElementsAnnotatedWith(Getter::class.java)
                .filter { it is TypeElement }
                .map { it as TypeElement }
                .forEach {
                    logd("process find class = $it")
                    try {
                        javaFile.writeTo(mFiler)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
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
}
