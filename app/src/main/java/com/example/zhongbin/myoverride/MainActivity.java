package com.example.zhongbin.myoverride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.annotation.MyOverride;

public class MainActivity extends AppCompatActivity implements TestInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @MyOverride
    public void setText(){

    }

    @MyOverride
    public void setData() {

    }
}
