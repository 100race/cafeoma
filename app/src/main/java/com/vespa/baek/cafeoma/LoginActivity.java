package com.vespa.baek.cafeoma;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;    //03.12 3시 46분 이렇게 글로벌로 선언해줘야만하나? 다른코드에서는 걍 20라인에 callbackmanager선언만해도되는데
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

    }
}
