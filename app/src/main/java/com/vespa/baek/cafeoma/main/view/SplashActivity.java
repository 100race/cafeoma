package com.vespa.baek.cafeoma.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.vespa.baek.cafeoma.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
