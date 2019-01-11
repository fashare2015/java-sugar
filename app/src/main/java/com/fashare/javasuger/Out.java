package com.fashare.javasuger;

import com.fashare.javasuger.annotation.AstPrint;

@AstPrint
public class Out {
    private String mName = "aaa";

    public String getName() {
        return mName;
    }

    public class Inner {

    }
}
