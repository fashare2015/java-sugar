package com.fashare.javasuger.apt.base;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Created by apple on 2019/1/10.
 * 代理类
 */
public class ProxyProcessor extends AbstractProcessor {
    private AbstractProcessor mRealProcessor;

    public ProxyProcessor(AbstractProcessor realProcessor) {
        mRealProcessor = realProcessor;
    }

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
