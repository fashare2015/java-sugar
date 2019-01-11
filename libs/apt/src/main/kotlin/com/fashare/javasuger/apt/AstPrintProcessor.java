package com.fashare.javasuger.apt;


import com.fashare.javasuger.apt.base.ProxyProcessor;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;

/**
 * 由于 @AutoService 不支持 kotlin, 加一层代理
 */
@AutoService(Processor.class)
public class AstPrintProcessor extends ProxyProcessor {
    public AstPrintProcessor() {
        super(new AstPrintProcessorImpl());
    }
}
