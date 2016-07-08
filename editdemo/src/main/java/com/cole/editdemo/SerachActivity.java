package com.cole.editdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SerachActivity extends AppCompatActivity {

    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serach);

        flowLayout = ((FlowLayout) findViewById(R.id.fl));

    }

}
