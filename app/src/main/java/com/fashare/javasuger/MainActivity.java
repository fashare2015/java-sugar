package com.fashare.javasuger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fashare.javasuger.annotation.Singletons;

public class MainActivity extends AppCompatActivity {

    private MyEventBus mMyEventBus = Singletons.get(MyEventBus.class);

    private MyEventBus.Listener mListener = new MyEventBus.Listener() {
        @Override
        public void onEvent(Object event) {
            Toast.makeText(MainActivity.this, "hello @Subject " + event, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ((TextView) findViewById(R.id.tv_test))
//                .setText(new User().getName());

//        UserManager.getInstance().sayHello(this);
        Singletons.get(UserManager.class).sayHello(this);

        mMyEventBus.add(mListener);

        mMyEventBus.notify("event ~~~");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMyEventBus.remove(mListener);
    }
}
