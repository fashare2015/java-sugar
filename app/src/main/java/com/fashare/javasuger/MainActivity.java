package com.fashare.javasuger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.fashare.javasuger.annotation.Singletons;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.tv_test))
                .setText(new User().getName());

        UserManager.getInstance().sayHello(this);
    }
}
