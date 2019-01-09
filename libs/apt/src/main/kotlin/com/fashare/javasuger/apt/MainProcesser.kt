package com.fashare.javasuger.apt

import com.google.auto.service.AutoService
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
internal class MainProcesser : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): Set<String> {
//        types.add(Widget.class.getCanonicalName());
        return LinkedHashSet()
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        return false
    }
}
