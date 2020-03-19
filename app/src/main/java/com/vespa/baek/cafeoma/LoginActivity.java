package com.vespa.baek.cafeoma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;                                                        //글로벌로 선언 해줘야됨
    private static final String TAG = "Login";                                                      //로그인과 관련된 로그 태그
    private static final int RC_LOGIN = 100;                                                      //ResultCode_LOGIN - google관련코드였음 아니어도 쓸수있을듯
    LoginButton facebook_login;                                                                     //페이스북 로그인버튼
    SignInButton google_login;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        // Facebook login 버튼추가
        facebook_login = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook_login.setReadPermissions("email");

        // Google login 버튼추가 -구글은 따로 onClick등록
        google_login = (SignInButton) findViewById(R.id.google_login_button);



        // facebook 로그인매니저에 Callback 등록
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code 메인엑티비티로 연결할것
                        Log.d(TAG,"페이스북 로그인성공 LoginResult="+loginResult);
                        Log.d(TAG,"페이스북 토큰 ->"+loginResult.getAccessToken().getToken()); //일단 추가해본라인
                        Log.d(TAG,"페이스북 UserID ->"+loginResult.getAccessToken().getUserId());
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);                      // 다음액티비티 이름 MainActivity로 정함 context에 이렇게 넣어도되던가?
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d(TAG,"페이스북 로그인취소");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(TAG,"페이스북 로그인에러");
                    }
                });

        // [[START 구글로그인 코드 -onCreate 안에 넣어줌]]

        // [Configure 구글 로그인]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))                                          //구글링으로 추가해준라인
                .requestEmail()
                .build();

        // [END config_signin]

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        google_login.setOnClickListener((view)->onClick(view));

        //[START initialize firebase Auth]
        mAuth = FirebaseAuth.getInstance();
        //[END initialize firebase Auth]


        //[[END 구글로그인 코드]]


    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.google_login_button:
                Log.d(TAG,"구글 로그인시도");
                signIn();
                break;
        }

    }
    //구글
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_LOGIN);                                           //ResultCode
    }

    //구글
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data); //facebook로그인과 필요한부분

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); //얘가문제
                Log.w(TAG, "구글 로그인 성공");
//                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                startActivity(intent);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "구글 로그인 실패", e);
                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }
        }


    }

    @Override
    public void onStart() { //일단 시작이 LoginActivity일 때
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "파이어베이스인증" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "파이어베이스 인증 성공");
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "파이어베이스 인증 실패", task.getException());

                        }


                    }
                });
    }


}
