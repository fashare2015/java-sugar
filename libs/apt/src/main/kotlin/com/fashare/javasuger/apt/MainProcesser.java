package com.fashare.javasuger.apt;


import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * 由于 @AutoService 不支持 kotlin, 加一层代理
 */
@AutoService(Processor.class)
public class MainProcesser extends AbstractProcessor {
    private AbstractProcessor mRealProcessor = new MainProcesserImpl();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return mRealProcessor.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return mRealProcessor.getSupportedSourceVersion();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mRealProcessor.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return mRealProcessor.process(set, roundEnvironment);
    }
}
