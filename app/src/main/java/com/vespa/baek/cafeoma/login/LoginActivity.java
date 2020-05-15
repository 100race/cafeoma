package com.vespa.baek.cafeoma.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.view.MainActivity;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;                                                        //글로벌로 선언 해줘야됨
    private static final String TAG = "Login";                                                      //로그인과 관련된 로그 태그
    private static final int RC_LOGIN = 100;                                                      //ResultCode_LOGIN - google관련코드

    //[View]
    private Button btn_ggLogin;
    private Button btn_fbLogin;
    private Button btn_email_login;
    private EditText emailbox;

    private ActionCodeSettings mActionCodeSettings;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    //뒤로가기변수
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(this);


        // Google login 버튼추가
        btn_ggLogin =  findViewById(R.id.btn_ggLogin);

        // Email login 버튼추가
        btn_email_login = findViewById(R.id.email_login_button);
        btn_email_login.setOnClickListener((v)->onClick(v));
        emailbox = findViewById(R.id.emailbox);
        emailbox.setOnClickListener(v->onClick(v));

        // [Facebook 로그인 초기화]
        btn_fbLogin = findViewById(R.id.btn_fbLogin);
        mCallbackManager = CallbackManager.Factory.create();
        btn_fbLogin.setOnClickListener(v->onClick(v));




        // [구글 로그인 초기화]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))                                          //구글링으로 추가해준라인
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        btn_ggLogin.setOnClickListener(v->onClick(v));

        mAuth = FirebaseAuth.getInstance();


    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_fbLogin:
                Log.d(TAG, "페이스북 로그인시도");
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("email"));
                loginManager.registerCallback(mCallbackManager ,new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);

                    }
                });
                break;
            case R.id.btn_ggLogin:
                signIn();
                break;
            case R.id.email_login_button:
                emailLogin();
                break;

        }
    }

    // [구글 로그인]
    private void signIn() {
        Log.d(TAG,"구글 로그인시도");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_LOGIN);
    }


    //[이메일 입력]
    public void emailLogin() {
        String email = emailbox.getText().toString();

        if(emailbox.getVisibility() == View.GONE) {  //첫 클릭시 입력창 표출
            emailbox.setVisibility(View.VISIBLE);
        }else if(emailbox.getVisibility() == View.VISIBLE && isValidEmail(email)) {  //두번째 클릭시 로그인
            Log.d(TAG,"이메일 로그인시도");
            buildActionCodeSettings();
            sendSignInLink(email,mActionCodeSettings);

        } else {  //이메일이 유효하지 않음
            Toast.makeText(this, "이메일이 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // [이메일링크 전송]
    public void sendSignInLink(String email, ActionCodeSettings actionCodeSettings) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //이메일을 프레퍼런스에 저장해줌
        SharedPreferences pref = getSharedPreferences("email", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user-email", email);
        editor.commit();

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "이메일 보냄");
                            Intent intent = new Intent(getApplicationContext(), NotifyEmailSendActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    // [이메일링크 동적링크 세팅]
    public void buildActionCodeSettings() {
        mActionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // 리다이렉트 되길 원하는 도메인을 Url에 등록
                        // URL을 반드시 승인된 화이트리스트에 등록해줘야함. 이거랑 도메인 등록해줌 @@해결
                        .setUrl("https://www.example.com") // 컴퓨터에서 열릴 코드인듯 설정안했더니 오류나서 다시써봄
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.vespa.baek.cafeoma",
                                true, // 설치안되어있으면 설치페이지로 이동시켜주는곳
                                "22" ) //minVersion
                        .build();
    }

    // [이메일 유효성체크]
    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            isValid = true;
        }
        return isValid;
    }

    // [구글 & 페북 result 받아오는부분]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //facebook 액티비티 result를 facebook sdk로 전달해줌
        mCallbackManager.onActivityResult(requestCode, resultCode, data); //facebook로그인과 필요한부분

        //GoogleSignInApi.getSignInIntent result에 따른 설정
        if (requestCode == RC_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); //얘가문제
                Log.w(TAG, "구글 로그인 성공");
                //finish();

            } catch (ApiException e) {
                Log.w(TAG, "구글 로그인 실패", e);

            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // 시작시 로그인 한 계정있나 확인 - 로그인 계정 확인 후 로그인 돼있으면 메인으로 넘겨줌
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            //finish();
        }
    }

  /**
  인증 파트는 파이어베이스에 해당 계정을 사용자로 등록해주는 과정
   **/
    // [구글 인증]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "파이어베이스인증" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "파이어베이스 인증 성공");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            //finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "파이어베이스 인증 실패", task.getException());
                        }
                    }
                });
    }

    // [페이스북 인증]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "페이스북 인증 성공");
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            //finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "페이스북 인증 실패", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    // [뒤로가기 두번누르면 종료]
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(LoginActivity.this);
            System.exit(0);
            toast.cancel();
        }
    }
}
