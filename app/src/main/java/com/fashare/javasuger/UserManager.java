package com.fashare.javasuger;

import android.content.Context;
import android.widget.Toast;

import com.fashare.javasuger.annotation.Singleton;

@Singleton
public class UserManager {

    public void sayHello(Context context) {
        Toast.makeText(context, "hello @Singleton", Toast.LENGTH_LONG).show();
    }
}
